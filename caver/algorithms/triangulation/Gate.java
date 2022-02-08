package algorithms.triangulation;

import geometry.primitives.*;

/*
 * Represents the interface of two tetrahedrons, which are neighbours in 3D
 * Delaunay triangulation.
 */
public class Gate {

    private Tetrahedron a;
    private Tetrahedron b;
    private Sphere sphere;

    public Gate(Tetrahedron a, Tetrahedron b) {
        assert !a.equals(b);
        this.a = a;
        this.b = b;

        NumberedSphere[] triangle = a.intersection(b);

        sphere = calculateTouchingSphere(
                triangle[0], triangle[1], triangle[2]);
    }

    public NumberedSphere[] getTriangle() {
        NumberedSphere[] triangle = a.intersection(b);

        return triangle;
    }

    private static Sphere getSphere(Vertex4f v) {
        return new Sphere(v.getX(), v.getY(), v.getZ(), v.getRadius());
    }

    private static Vertex4f getVertex4f(GeneralSphere s) {
        return new Vertex4f((float) s.getS().getX(), (float) s.getS().getY(),
                (float) s.getS().getZ(), (float) s.getR());
    }

    /*
     * Calculates smallest sphere which has external touch with all three
     * spheres.
     */
    public static Sphere calculateTouchingSphere(GeneralSphere a,
            GeneralSphere b, GeneralSphere c) {
        Vertex4f v = Circle.tangetial(
                getVertex4f(a), getVertex4f(b), getVertex4f(c));
        return getSphere(v);
    }

    /*
     * @return the one of the two tetrahedron this gates connect which differs
     * from t
     */
    public Tetrahedron other(Tetrahedron t) {
        assert t.equals(a) || t.equals(b);
        if (a.equals(t)) {
            return b;
        } else {
            return a;
        }
    }

    public Tetrahedron getA() {
        return a;
    }

    public Tetrahedron getB() {
        return b;
    }

    public double getDistance() {
        return a.getGravityCenter().distance(b.getGravityCenter());
    }

    /*
     * Removes gate from both tetrahedrons.
     */
    public void destroy() {
        a.removeGate(this);
        b.removeGate(this);
    }

    /*
     * Removes gate from both tetrahedrons, but stores it in them as way to the
     * bulk solvent (intended to use when one of tetrahedrons will be removed
     * from triangulation).
     */
    public void close() {
        a.closeGate(this);
        b.closeGate(this);
    }

    public double getRadius() {
        return sphere.getR();
    }

    public Sphere getSphere() {
        return sphere;
    }

    public Point getCenter() {
        return sphere.getS();
    }

    public boolean connects(Tetrahedron c, Tetrahedron d) {
        if (a.equals(c) && b.equals(d) || a.equals(d) && b.equals(c)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = a.getId() * b.getId();
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        Gate g = (Gate) o;
        if ((a.equals(g.a) && b.equals(g.b))
                || (a.equals(g.b) && b.equals(g.a))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        if (a.getId() < b.getId()) {
            return a.getId() + "<->" + b.getId();
        } else {
            return b.getId() + "<->" + a.getId();
        }
    }
}
