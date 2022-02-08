package algorithms.triangulation;

import caver.Printer;
import chemistry.pdb.PdbLine;
import geometry.primitives.GeneralSphere;
import geometry.primitives.NumberedSphere;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * Represents a tetrahedron in 3D Delaunay triangulation.
 */
public class Tetrahedron {

    public double distance = Double.MAX_VALUE;
    public Gate previousGate = null;
    public Tetrahedron previousTetrahedron = null;
    private Point gravityCenter;
    NumberedSphere[] vertices_;
    Set<Gate> gates = new HashSet<Gate>();
    int id_;
    private List<GeneralSphere> spheres_ = new ArrayList<GeneralSphere>();
    private Set<Gate> gatesOut = new HashSet<Gate>();
    private double greatestRadius;

    public Tetrahedron(int id, NumberedSphere[] spheres) {
        id_ = id;
        vertices_ = spheres;

        for (NumberedSphere s : spheres) {
            if (greatestRadius < s.getR()) {
                greatestRadius = s.getR();
            }
        }
    }

    public Point getGravityCenter() {
        if (null == gravityCenter) {
            gravityCenter = new Point(0, 0, 0);
            assert vertices_.length == 4;
            for (NumberedSphere s : vertices_) {
                gravityCenter = gravityCenter.plus(s.getS());
            }
            gravityCenter = gravityCenter.divide(vertices_.length);
        }
        return gravityCenter;
    }

    /* Returns sphere inside touching all four spheres.
     * The spheres MUST have equal radii.
     * WARNING: sphere center might be outside tetrahedron
     */
    public Sphere getGreatestSphere() {
        double[][] vs = new double[4][3];
        for (int i = 0; i < 4; i++) {
            Point p = vertices_[i].getS();
            vs[i][0] = p.getX();
            vs[i][1] = p.getY();
            vs[i][2] = p.getZ();
        }
        Sphere c = Circumscribe.getTangent(vs, greatestRadius);


        return c;
    }

    public boolean inside(Point v) {

        Point p0 = vertices_[0].getS();
        Point p1 = vertices_[1].getS();
        Point p2 = vertices_[2].getS();
        Point p3 = vertices_[3].getS();

        int sideSign = sign(p0, p1, p2, p3);

        boolean rslt = (sideSign == sign(v, p1, p2, p3)
                && sideSign == sign(p0, v, p2, p3)
                && sideSign == sign(p0, p1, v, p3)
                && sideSign == sign(p0, p1, p2, v));
        return rslt;
    }

    private static int sign(Point a, Point b, Point c, Point d) {
        double dax = a.getX() - d.getX();
        double day = a.getY() - d.getY();
        double daz = a.getZ() - d.getZ();
        double dbx = b.getX() - d.getX();
        double dby = b.getY() - d.getY();
        double dbz = b.getZ() - d.getZ();
        double dcx = c.getX() - d.getX();
        double dcy = c.getY() - d.getY();
        double dcz = c.getZ() - d.getZ();

        double crossx = dby * dcz - dcy * dbz;
        double crossy = dbz * dcx - dcz * dbx;
        double crossz = dbx * dcy - dcx * dby;
        double result = dax * crossx + day * crossy + daz * crossz;

        if (0 <= result) {
            return 1;
        } else {
            return -1;
        }
    }

    public boolean isSurface() {
        return !gatesOut.isEmpty();
    }

    public int getId() {
        return id_;
    }

    public int countNeighbours() {
        return gates.size();
    }

    public Set<Gate> getGates() {
        return new HashSet(gates);
    }

    public Set<Gate> getGatesShallow() {
        return gates;
    }

    public boolean isConnected(Tetrahedron t) {
        assert !this.equals(t);
        Gate g = new Gate(this, t);
        if (gates.contains(g)) {
            return true;
        } else {
            return false;
        }
    }

    public void removeGate(Gate g) {
        assert gates.contains(g);
        gates.remove(g);
    }

    public void closeGate(Gate g) {
        assert gates.contains(g);
        gates.remove(g);
        if (!gatesOut.contains(g)) {
            gatesOut.add(g);
        }
    }

    public Set<Gate> getGatesOut() {
        return gatesOut;
    }

    public NumberedSphere[] intersection(Tetrahedron t) {
        Set<NumberedSphere> spheres = new HashSet(this.getSphereSet());
        spheres.retainAll(t.getSphereSet());
        NumberedSphere[] a = new NumberedSphere[0];
        a = spheres.toArray(a);

        assert a[0].getR() < 4;
        assert a[1].getR() < 4;
        assert a[2].getR() < 4;

        return a;
    }

    private void printGates() {
        Printer.println("Gates " + gates.size());
        for (Gate g : gates) {
            Printer.println(g.getA().getId() + " - " + g.getB().getId());
        }
    }

    private void checkGates() {
        if (!(0 < gates.size() && gates.size() <= 4)) {
            printGates();
        }
    }

    public List<GeneralSphere> getSpheres() {
        return spheres_;
    }

    public void addGate(Gate g) {
        spheres_.add(g.getSphere());
        gates.add(g);
    }

    /*
     * Returns null if gate already exists, newly created gate otherwise.
     */
    public Gate makeNeighbours(Tetrahedron n) {
        Gate g = null;
        if (!isConnected(n)) {
            assert !this.equals(n);
            g = new Gate(this, n);
            addGate(g);
            n.addGate(g);
        }
        return g;
    }

    private Set<NumberedSphere> getSphereSet() {
        Set<NumberedSphere> s = new HashSet(Arrays.asList(vertices_));
        assert s.size() == 4;
        return s;
    }

    public NumberedSphere[][] getSpheresByFaces() {

        NumberedSphere[][] a = new NumberedSphere[4][3];

        a[0][0] = vertices_[0];
        a[0][1] = vertices_[1];
        a[0][2] = vertices_[2];

        a[1][0] = vertices_[0];
        a[1][1] = vertices_[1];
        a[1][2] = vertices_[3];

        a[2][0] = vertices_[0];
        a[2][1] = vertices_[2];
        a[2][2] = vertices_[3];

        a[3][0] = vertices_[1];
        a[3][1] = vertices_[2];
        a[3][2] = vertices_[3];

        return a;
    }
    public List<Set<NumberedSphere>> getSpheresBySets() {

        NumberedSphere[][] a = getSpheresByFaces();
        
        List<Set<NumberedSphere>> l = new ArrayList<Set<NumberedSphere>>();

        l.add(as(a[0]));
        l.add(as(a[1]));
        l.add(as(a[2]));
        l.add(as(a[3]));

        return l;
    }

    Set<NumberedSphere> as(NumberedSphere[] a) {
        Set<NumberedSphere> set = new HashSet<NumberedSphere>();
        set.addAll(Arrays.asList(a));
        return set;
    }

    private int[] getVertexIds() {
        int[] ids = new int[vertices_.length];
        for (int i = 0; i < vertices_.length; i++) {
            ids[i] = vertices_[i].getId();
        }
        Arrays.sort(ids);
        return ids;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Arrays.deepHashCode(this.vertices_);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        Tetrahedron t = (Tetrahedron) o;
        return getSphereSet().equals(t.getSphereSet());
    }

    @Override
    public String toString() {
        String s = "T" + getId() + ":";
        for (int i : getVertexIds()) {
            s += " " + i;
        }

        return s;
    }

    public int toPdb(BufferedWriter bwPdb, int startSerial, String residue) throws IOException {
        int serial = startSerial;
        for (NumberedSphere s : vertices_) {
            Point a = s.getS();
            PdbLine pl = new PdbLine(serial, "H", "TUN", residue, 'T',
                    a.getX(), a.getY(), a.getZ());
            bwPdb.write(pl.getPdbString() + "\n");
            serial++;
        }
        for (int x = startSerial; x < serial; x++) {
            for (int y = startSerial; y < x; y++) {
                String s = String.format("CONECT%5d%5d", x, y);
                bwPdb.write(s + "\n");
            }
        }
        serial++;
        return serial;
    }
}
