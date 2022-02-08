package chemistry;

import caver.Printer;
import chemistry.pdb.PdbFileProcessor;
import chemistry.pdb.PdbLine;
import chemistry.pdb.PdbUtil;
import chemistry.pdb.exceptions.AtomNotFoundException;
import geometry.platonic.PlatonicSolid;
import geometry.primitives.AnnotatedPoint;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import geometry.search.SpaceTree;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple model of a molecular system allowing queries about residues, atoms
 * and their positions. Atoms may be removed (filtered, see method remove).
 *
 */
public class MolecularSystem {

    private SortedMap<Integer, Atom> atoms = new TreeMap<Integer, Atom>();
    public static double sphereSimilarity = 0.01;
    private double minRadius = Double.MAX_VALUE;
    private AtomSearchTree atomSearchTree_;
    private PdbFileProcessor pfp_;

    private MolecularSystem() {
    }

    public int size() {
        return atoms.size();
    }

    public boolean isEmpty() {
        return atoms.isEmpty();
    }

    public static MolecularSystem create(PdbFileProcessor pfp) {
        MolecularSystem ms = new MolecularSystem();
        ms.pfp_ = pfp;

        // identify atoms with physicaly impossible distances and log warning
        SpaceTree<Atom> st = new SpaceTree<Atom>();

        for (Atom a : pfp.getAtoms().values()) {

            if (a.getRadius() < ms.minRadius) {
                ms.minRadius = a.getRadius();
            }

            Point c = a.getCenter();
            AnnotatedPoint<Atom> ca = st.close(c, 0.5);

            if (null != ca) { // minimal bond length He 0.64                
                Logger.getLogger("caver ").log(Level.WARNING,
                        "Atom {0} is suspiciously close to {1}",
                        new Object[]{a, ca.getAnnotation()});
            }
            st.add(c, a);
            ms.atoms.put(a.getSerialNumber(), a);

        }

        return ms;
    }

    public Point getCenter() {
        Point sum = new Point(0, 0, 0);
        int mass = 0;
        for (Atom a : atoms.values()) {
            int z = a.getElement().getProtonNumber();
            int m = 1;
            if (2 <= z) {
                m = 2 * z;
            }

            mass += m;
            sum = sum.plus(a.getCenter().multiply(m));
        }
        return sum.divide(mass);
    }

    public Residue getResidue(Atom a) throws AtomNotFoundException {
        return pfp_.getResidueForAtom(a);
    }

    /*
     * Must be called after atom/residue removal and before spatial search is
     * performed.
     */
    public void resetSearchTree() {
        atomSearchTree_ = null;
    }

    public boolean includes(Set<ResidueRange> set, ResidueId ri) {
        for (ResidueRange rr : set) {
            if (rr.within(ri)) {
                return true;
            }
        }
        return false;
    }

    public boolean includes(Set<AtomRange> set, int serial) {
        for (AtomRange ar : set) {
            if (ar.within(serial)) {
                return true;
            }
        }
        return false;
    }

    public int remove(
            Set<String> includeOnlyResiduesNamed,
            Set<ResidueRange> includeOnlyResiduesIded,
            Set<AtomRange> includeOnlyAtomsNumbered,
            Set<String> excludeResiduesNamed,
            Set<ResidueRange> excludeResiduesIded,
            Set<AtomRange> excludeAtomsNumbered,
            boolean includeAll) {

        List<Integer> remove = new ArrayList<Integer>();
        for (int ai : atoms.keySet()) {
            Atom a = atoms.get(ai);
            boolean i = includeOnlyResiduesNamed.contains(
                    a.getResidue().getName().toUpperCase())
                    || includes(includeOnlyResiduesIded, a.getResidue().getId())
                    || includes(includeOnlyAtomsNumbered, a.getSerialNumber());

            boolean e = excludeResiduesNamed.contains(
                    a.getResidue().getName().toUpperCase())
                    || includes(excludeResiduesIded, a.getResidue().getId())
                    || includes(excludeAtomsNumbered, a.getSerialNumber());

            // nothing, then include something and finally remove something
            if ((!includeAll && !i) || e) {
                remove.add(ai);
            }
        }

        SortedSet<String> ris = new TreeSet<String>();
        for (int ai : remove) {
            ris.add(atoms.get(ai).getResidue().getName());
            atoms.remove(ai);
        }
        if (!remove.isEmpty()) {
            StringBuilder sb = new StringBuilder(
                    remove.size() + " atoms removed from these residues: ");
            for (String ri : ris) {
                sb.append(ri).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            sb.append(".");
            Printer.println(sb.toString(), Printer.IMPORTANT);
        }


        resetSearchTree();

        return remove.size();
    }

    /*
     * Returns set of spheres modeling individual atoms.
     */
    public List<Sphere> getSpheres() {
        List<Sphere> spheres = new ArrayList<Sphere>();
        for (Atom a : atoms.values()) {
            spheres.add(new Sphere(a.getCenter(), a.getRadius()));
        }

        assert PdbUtil.areSpheresDissimilar(spheres, sphereSimilarity);

        return spheres;
    }

    public static void evaluateError(Sphere sphere, Sphere[] rose, long iterations) {
        double[] lo = new double[3];
        double[] hi = new double[3];

        double highest = 0;
        double deepest = 0;

        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            lo[i] = sphere.getS().getCoordinates()[i] - sphere.getR() * 1.2;
            hi[i] = sphere.getS().getCoordinates()[i] + sphere.getR() * 1.2;
        }
        for (long i = 0; i < iterations; i++) {
            double[] p = new double[3];
            for (int c = 0; c < 3; c++) {
                double d = hi[c] - lo[c];
                p[c] = lo[c] + random.nextDouble() * (d / 2); // evaluate one quarter of space
            }
            Point point = new Point(p);

            boolean inApproximation = false;
            for (Sphere s : rose) {
                if (s.getS().distance(point) <= s.getR()) {
                    inApproximation = true;
                }
            }
            boolean inSphere = sphere.getS().distance(point) <= sphere.getR();

            double depth = sphere.getR() - sphere.getS().distance(point);
            if (inSphere && !inApproximation) {
                if (deepest < depth) {
                    deepest = depth;
                }
            }

            if (!inSphere && inApproximation) {
                if (depth < highest) {
                    highest = depth;
                }
            }
        }
    }

    public List<Sphere> getSpheresOneRadiiApproximation(
            double tolerableRadiusIncrease, int n, boolean central,
            Random random) {

        List<Sphere> spheres = new ArrayList<Sphere>();
        for (Atom a : atoms.values()) {

            if (minRadius * tolerableRadiusIncrease < a.getRadius()) {

                Sphere as = new Sphere(a.getCenter(), a.getRadius());

                List<Sphere> approximatedSphere =
                        PlatonicSolid.getSphericalApproximation(as,
                        minRadius, a.getRadius(), n, central, random);
                spheres.addAll(approximatedSphere);
            } else {
                spheres.add(new Sphere(a.getCenter(), a.getRadius()));

            }

        }

        assert PdbUtil.areSpheresDissimilar(spheres, sphereSimilarity);

        return spheres;
    }

    public Set<Atom> getAtomsWithinDistanceFromPoint(Point p, double distance) {
        if (null == atomSearchTree_) {
            atomSearchTree_ = new AtomSearchTree(atoms.values());
        }
        return atomSearchTree_.getAtomsWithinDistanceFromPoint(p, distance);
    }

    public double getProteinRadius(Point start) {
        double r = 0;
        for (Atom a : atoms.values()) {
            double dist = start.distance(a.getCenter()) + a.getRadius();
            if (r < dist) {
                r = dist;
            }
        }
        return r;
    }

    public double getGreatestAtomRadius() {
        double max = 0;
        for (Atom a : atoms.values()) {
            double r = a.getRadius();
            if (max < r) {
                max = r;
            }
        }
        return max;
    }

    public void save(File f) {

        try {
            FileOutputStream out = new FileOutputStream(f);
            PrintStream ps = new PrintStream(out);
            for (int serial : atoms.keySet()) {

                Atom a = atoms.get(serial);

                PdbLine pl = new PdbLine(
                        serial,
                        a.getName(),
                        a.getResidue().getName(),
                        a.getResidueId().getPdbString(),
                        a.getResidueId().getChain(),
                        a.getSphere().getS().getX(),
                        a.getSphere().getS().getY(),
                        a.getSphere().getS().getZ());

                ps.println(pl.getPdbString());
            }
            ps.close();
        } catch (IOException e) {
            Logger.getLogger("caver").log(
                    Level.WARNING, f.getAbsolutePath(), e);
        }

    }
}
