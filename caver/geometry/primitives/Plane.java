package geometry.primitives;

public class Plane {

    private double a, b, c, d;

    /*
     * http://local.wasp.uwa.edu.au/~pbourke/geometry/planeeq/
     */
    public Plane(Point p1, Point p2, Point p3) throws CollinearPointsException {

        a = p1.getY() * (p2.getZ() - p3.getZ())
                + p2.getY() * (p3.getZ() - p1.getZ())
                + p3.getY() * (p1.getZ() - p2.getZ());
        b = p1.getZ() * (p2.getX() - p3.getX())
                + p2.getZ() * (p3.getX() - p1.getX())
                + p3.getZ() * (p1.getX() - p2.getX());
        c = p1.getX() * (p2.getY() - p3.getY())
                + p2.getX() * (p3.getY() - p1.getY())
                + p3.getX() * (p1.getY() - p2.getY());
        d = -(p1.getX() * (p2.getY() * p3.getZ() - p3.getY() * p2.getZ())
                + p2.getX() * (p3.getY() * p1.getZ() - p1.getY() * p3.getZ())
                + p3.getX() * (p1.getY() * p2.getZ() - p2.getY() * p1.getZ()));
        if (0 == a * a + b * b + c * c) {
            throw new CollinearPointsException();
        }
    }

    /*
     * http://mathworld.wolfram.com/Plane.html
     *
     * Signed distance - possible to use for detection if plane separates
     * points.
     */
    public double distance(Point p) {
        double dist =
                (a * p.getX() + b * p.getY() + c * p.getZ() + d)
                / Math.sqrt(a * a + b * b + c * c);
        return dist;
    }

    public double distance(Sphere sphere) {
        double dist = Math.abs(distance(sphere.getS()))
                - sphere.getR();
        if (dist < 0) {
            dist = 0;
        }
        return dist;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }

    public double getD() {
        return d;
    }
}
