package caver.tunnels;

import algorithms.clustering.Cluster;
import algorithms.triangulation.TVE;
import algorithms.triangulation.VE;
import caver.CalculationSettings;
import caver.Clock;
import caver.Printer;
import chemistry.Atom;
import chemistry.MolecularSystem;
import chemistry.Residue;
import chemistry.pdb.SnapId;
import chemistry.pdb.exceptions.AtomNotFoundException;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a tunnel. Tunnel is represented by Voronoi edges, but those are
 * converted into a sequence of balls, which are then removed from the end in
 * order to define tunnel end more precisely.
 */
public class Tunnel implements Serializable, Comparable<Tunnel> {

    private final static long serialVersionUID = 3L;
    private Point source_; // starting point
    private SnapId snap_;
    private Integer snapPriority_;
    // order of the tunnel within snapshot ordered by cost
    private int id_;
    private int number_ = -1;
    private TVE[] edges_; // oriented from starting point towards outside 
    // solvent is both serialized and saved separately - allows switching 
    // between memory and slower disk processing
    private double cost_;
    private double length_;
    private double curvature_;
    private Sphere bottleneck_; // narrowest site of the tunnel
    private transient Cluster cluster_;
    // transient because
    // 1. profile density can be changed after tunnels are computed
    // 2. save disk space
    // 3. variables are currently computed after tunnels are saved
    private transient double[] profileErrors_;
    private transient double maxError_;
    private transient double errorSum_;
    private transient int errorCounter_;
    private transient double bottleneckError_;
    private transient CalculationSettings cs_;

    public static Tunnel create(Point source, SnapId snap, int id,
            List<VE> edges, List<Integer> nodes,
            double cost,
            double endR,
            double mainSamplingStep, CalculationSettings cs) {
        Tunnel t = new Tunnel();
        t.cs_ = cs;
        t.source_ = source;
        t.snap_ = snap;
        t.id_ = id;
        t.cost_ = cost;

        List<VE> edgesList = new ArrayList<VE>();

        int lastEdge = 0;
        boolean broke = false;

        // remove too wide edges from end
        // approximative, may get inside through extremly long edge
        for (int i = 0; i < edges.size(); i++) {
            VE e = edges.get(i);
            if (e.getBottleneck().getR() <= endR
                    || e.getARadius() <= endR || e.getBRadius() <= endR) {
                lastEdge = i; // this edge has part thin enough
                broke = true;
                break;
            }
        }
        if (!broke) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Whole tunnel is wider than {0}"
                    + ", big end spheres were not removed"
                    + " because nothing would be left. Consider increasing "
                    + " max_end_sphere_radius (" + "{1}).",
                    new Object[]{endR, endR});
        }

        if (2 <= edges.size() && lastEdge < edges.size() - 2) {

            VE e1 = edges.get(edges.size() - 1);
            VE e2 = edges.get(edges.size() - 2);

            // orient first element
            if (e1.getNodeA() == e2.getNodeA()
                    || e1.getNodeA() == e2.getNodeB()) {
                edgesList.add(e1.flip());
            } else {
                edgesList.add(e1);
            }

            for (int i = edges.size() - 2; lastEdge <= i; i--) {
                VE e = edgesList.get(edgesList.size() - 1); // really _, last added
                VE f = edges.get(i);

                if (e.getNodeB() == f.getNodeA()) {
                    edgesList.add(f);
                } else {
                    edgesList.add(f.flip());
                }
            }
        } else {
            return null;
        }

        for (VE e : edgesList) {
            Sphere s = e.getBottleneck();
            if (null == t.bottleneck_
                    || s.getR() < t.bottleneck_.getR()) {
                t.bottleneck_ = s;
            }
        }

        t.edges_ = new TVE[edgesList.size()];

        for (int i = 0; i < t.edges_.length; i++) {
            t.edges_[i] = new TVE(edgesList.get(i));
        }

        List<Sphere> profile = t.computeProfile(cs.getProfileTunnelSamplingStep());
        t.computeProfileRelatedValues(profile);


        if (t.length_ < t.cs_.getMinTunnelLength()) {
            return null;
        } else {
            return t;
        }
    }

    private void computeProfileRelatedValues(List<Sphere> profile) {

        length_ = 0;
        for (int i = 1; i < profile.size(); i++) {
            Sphere a = profile.get(i - 1);
            Sphere b = profile.get(i);
            length_ += a.getS().distance(b.getS());
        }

        if (profile.isEmpty()) {
            curvature_ = 0;
        } else {
            double distance = profile.get(0).getS().distance(
                    profile.get(profile.size() - 1).getS());
            curvature_ = length_ / distance;
        }
    }

    private TVE[] getEdges() {
        if (null == edges_) {
            return loadEdges();
        } else {
            return edges_;
        }
    }

    public int getEdgesCount() {
        return edges_.length;

    }

    public int getEdgesSize() {
        return edges_.length * TVE.bytes;

    }

    /*
     * edges_ will stay null forever and will be loaded from HDD when needed
     */
    public void deleteEdges() {
        edges_ = null;
    }

    public void setCalculationSettings(CalculationSettings cs) {
        cs_ = cs;
    }

    public void writeEdges(DataOutputStream dos) throws IOException {
        for (TVE e : edges_) {
            e.write(dos);
        }
    }

    private TVE[] loadEdges() {
        Clock.start("loading edges");
        try {
            DataInputStream dis =
                    new DataInputStream(
                    new BufferedInputStream(
                    new FileInputStream(cs_.getEdgesFile(snap_))));

            int ne = dis.readInt(); // number of tunnels in file

            int skip = 0; // edge collections to skip
            int count = -1; // edges in collection for this tunnel
            boolean found = false;
            for (int i = 0; i < ne; i++) {


                int id = dis.readInt();
                int n = dis.readInt(); // no of edges

                if (id != id_) {
                    if (!found) {
                        skip += n;
                    }
                } else {
                    found = true;
                    count = n;
                }

            }

            TVE.skip(dis, skip);

            TVE[] edges = new TVE[count];
            for (int i = 0; i < count; i++) {
                edges[i] = new TVE(dis);
            }


            dis.close();

            Clock.stop("loading edges");

            return edges;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void setSnapshotPriority(int i) {
        this.snapPriority_ = i;
    }

    /*
     * Return importance of tunnel in the scope of snapshot, starting from 1.
     * The smaller the number, the more relevant tunnel this is.
     */
    public int getPriority() {
        if (null == snapPriority_) {
            throw new IllegalStateException("Snapshot priority of "
                    + "tunnel was not set yet.");
        }
        return snapPriority_;
    }

    public void setCluster(Cluster cluster) {
        cluster_ = cluster;
    }

    public void clearCluster() {
        cluster_ = null;
    }

    public Cluster getCluster() {
        return cluster_;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.number_;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        Tunnel t = (Tunnel) o;
        return this.id_ == t.id_;
    }

    @Override
    public int compareTo(Tunnel t) {
        int c = Double.compare(getCost(), t.getCost());
        if (0 == c) {
            // switched order, wider is first
            c = Double.compare(t.getBottleneck().getR(), getBottleneck().getR());
        }
        if (0 == c) {
            c = Double.compare(getLength(), t.getLength());
        }
        if (0 == c) {
            c = Double.compare(getId(), t.getId());
        }
        return c;
    }

    public double getCost() {
        return cost_;
    }

    /**
     * I.e. value correlated to number of molecules passing through the tunnel
     * in a time unit.
     */
    public double getThroughput() {
        return Math.pow(Math.E, -cost_);
    }

    public int getId() {
        return id_;
    }

    public int getNumber() {
        if (number_ == -1) {
            throw new IllegalStateException();
        }
        return number_;
    }

    public void setNumber(int number) {
        if (number_ != -1) {
            throw new IllegalStateException("" + number_);
        }
        number_ = number;
    }

    public Point getSource() {
        return source_;
    }

    public SnapId getSnapId() {
        return snap_;
    }

    public Sphere getBottleneck() {
        return bottleneck_;
    }

    public Double getBottleneckError() {
        if (0 != errorCounter_) {
            return bottleneckError_;
        } else {
            return null;
        }
    }

    public double getLength() {
        return length_;

    }

    public double getCurvature() {
        return curvature_;
    }

    public double[] getErrorProfile() {
        return profileErrors_;
    }

    public Double getMaxError() {
        if (0 != errorCounter_) {
            return maxError_;
        } else {
            return null;
        }
    }

    public Double getAverageError() {
        if (0 != errorCounter_) {
            return errorSum_ / errorCounter_;
        } else {
            return null;
        }
    }

    public double getErrorSum() {
        return errorSum_;
    }

    public int getErrorCounter() {
        return errorCounter_;
    }

    public void processErrors(List<Sphere> profile,
            double[] errors, boolean store) {
        maxError_ = 0;
        for (double d : errors) {
            if (maxError_ < d) {
                maxError_ = d;
            }
        }

        errorSum_ = 0;
        errorCounter_ = 0;
        for (double d : errors) {
            errorSum_ += d;
            errorCounter_++;
        }

        if (profile.size() != errors.length) {
            Printer.warn("Suspicious error bound estimation.");
        }
        double br = Double.MAX_VALUE;
        bottleneckError_ = 0;
        for (int i = 0; i < profile.size(); i++) {
            if (profile.get(i).getR() < br) {
                br = profile.get(i).getR();
                bottleneckError_ = errors[i];
            }
        }

        if (store) {
            profileErrors_ = errors;
        }
    }

    public List<Residue> computeBottleneckResidues(
            MolecularSystem ms, double distance) {
        List<Residue> residueList = new ArrayList<Residue>();
        Sphere min = getBottleneck();

        Set<Atom> atoms = new HashSet<Atom>(ms.getAtomsWithinDistanceFromPoint(
                min.getS(), min.getR() + distance));

        Set<Residue> residueSet = new HashSet<Residue>();

        while (!atoms.isEmpty()) {

            double minD = Double.MAX_VALUE;
            Atom minA = null;
            for (Atom a : atoms) {
                double d = a.getCenter().distance(min.getS()) - min.getR();
                if (d < minD) {
                    minD = d;
                    minA = a;
                }
            }
            try {
                atoms.remove(minA);
                Residue r = ms.getResidue(minA);
                if (!residueSet.contains(r)) {
                    residueList.add(r);
                    residueSet.add(r);
                }
            } catch (AtomNotFoundException e) {
                Logger.getLogger("caver").log(Level.SEVERE, null, e);
            }
        }

        return residueList;
    }

    public final List<Sphere> computeProfile(double step) {

        List<Sphere> profile = new ArrayList<Sphere>();
        double phase = 0;
        TVE[] edges = getEdges();
        for (int i = 0; i < edges.length; i++) {
            List<Sphere> ss = new ArrayList<Sphere>();
            phase = edges[i].getSpheres(step, phase, ss);
            profile.addAll(ss);
        }

        List<Sphere> remove = new ArrayList<Sphere>();
        // leave at least one sphere at start, therefore 0 < i
        int index;
        for (index = profile.size() - 1; 0 < index; index--) {
            Sphere s = profile.get(index);
            if (s.getR() <= cs_.getShellRadius()) {
                break;
            }
            remove.add(s);
        }
        Sphere last;

        if (profile.isEmpty()) {
            return profile;
        }

        if (remove.isEmpty()) {
            last = profile.get(profile.size() - 1);
        } else {
            last = remove.get(remove.size() - 1);
        }

        for (int i = index; 0 < i; i--) {
            Sphere s = profile.get(i);
            if (last.contains(s.getS())) {
                remove.add(s);
            }
        }

        profile.removeAll(remove);

        return profile;
    }

    /*
     * shift - maximal change in each coordinate
     */
    public List<Sphere> correctProfile(List<Sphere> profile,
            MolecularSystem ms, double shift, int gridN) {
        List<Sphere> p = new ArrayList<Sphere>();

        double gr = 0;

        for (int i = 0; i < profile.size(); i++) {
            Sphere s = profile.get(i);

            Point n;
            if (0 == i) {
                n = profile.get(1).getS().minus(profile.get(0).getS());
            } else if (profile.size() - 1 == i) {
                n = profile.get(profile.size() - 1).getS().minus(
                        profile.get(profile.size() - 2).getS());
            } else {
                n = profile.get(i + 1).getS().minus(
                        profile.get(i - 1).getS());
            }
            n = n.normalize();

            Point a = new Point(0, 0, 0);
            Random random = new Random();
            while (a.size() < 0.01) {
                // want it to be random perepndicular to n, cross
                // sufficinetly big to prevent underflow error
                a = n.cross(new Point(random.nextDouble(), random.nextDouble(),
                        random.nextDouble()));
            }
            a = a.normalize();

            Set<Atom> atoms = ms.getAtomsWithinDistanceFromPoint(
                    s.getS(), s.getR() + shift);

            Point b = n.cross(a).normalize(); // orthogonal to a and n

            double step = shift / gridN;

            Sphere greatest = s;

            if (a.dot(n) < 0.001 && b.dot(n) < 0.001 && a.dot(b) < 0.001) {
            } else {
                Logger.getLogger("caver").log(Level.WARNING,
                        "Warning: tunnel optimization orthogonality breached.");
                p.add(greatest);
                continue;
            }
            for (int x = 0; x < gridN; x++) {
                for (int y = 0; y < gridN; y++) {
                    Point g = s.getS().
                            plus(a.multiply(x * step)).
                            plus(b.multiply(y * step));
                    double minR = Double.MAX_VALUE;
                    for (Atom atom : atoms) {

                        double r = atom.getSphere().distance(g);

                        if (r < minR) {
                            minR = r;
                        }
                    }

                    if (greatest.getR() < minR) {
                        greatest = new Sphere(g, minR);
                    }
                }
            }
            if (gr < greatest.getR() - s.getR()) {
                gr = greatest.getR() - s.getR();
            }

            p.add(greatest);
        }

        return p;
    }

    public static Point computeAverageOrigin(List<Tunnel> tunnels) {
        Point p = new Point(0, 0, 0);
        int count = 0;
        for (Tunnel t : tunnels) {
            Point source = t.getSource();

            p = p.plus(source);
            count++;
        }
        return p.divide(count);
    }
}
