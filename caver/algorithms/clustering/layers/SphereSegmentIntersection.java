package algorithms.clustering.layers;

import algorithms.clustering.GeometryException;
import algorithms.clustering.Line;
import geometry.primitives.Point;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Utility class for computation of intersection of a sphere and a line segment.
 *
 * Based on the this C++ code: atelier iebele abel - 2001 atelier@iebele.nl
 * http://www.iebele.nl
 *
 * sphere_line_intersection function adapted from:
 * http://astronomy.swin.edu.au/pbourke/geometry/sphereline Paul Bourke
 * pbourke@swin.edu.au
 */
public class SphereSegmentIntersection {

    private static double square(double f) {
        return (f * f);
    }

    /*
     * Returns array whose length is equal to the number of intersection of
     * (infinite, not segment) line segment defined by p1 and p2 and sphere of
     * center s and radius r.
     */
    public static Point[] sphereLineIntersection(
            Point p1, Point p2, Point s, double r) {
        Point[] result;

        double x1 = p1.getX();
        double y1 = p1.getY();
        double z1 = p1.getZ();
        double x2 = p2.getX();
        double y2 = p2.getY();
        double z2 = p2.getZ();
        double x3 = s.getX();
        double y3 = s.getY();
        double z3 = s.getZ();
        double a, b, c, mu, i;
        Point[] p;
        a = square(x2 - x1) + square(y2 - y1) + square(z2 - z1);
        b = 2 * ((x2 - x1) * (x1 - x3)
                + (y2 - y1) * (y1 - y3)
                + (z2 - z1) * (z1 - z3));
        c = square(x3) + square(y3)
                + square(z3) + square(x1)
                + square(y1) + square(z1)
                - 2 * (x3 * x1 + y3 * y1 + z3 * z1) - square(r);
        i = b * b - 4 * a * c;
        if (i < 0.0) {
            // no intersection            
            result = new Point[0];
        } else if (i == 0.0) {
            // one intersection
            p = new Point[1];
            mu = -b / (2 * a);
            p[0] = new Point(
                    x1 + mu * (x2 - x1),
                    y1 + mu * (y2 - y1),
                    z1 + mu * (z2 - z1));
            result = p;
        } else {
            // two intersections
            p = new Point[2];
            // first intersection
            mu = (-b + (double) Math.sqrt(square(b) - 4 * a * c)) / (2 * a);
            p[0] = new Point(
                    x1 + mu * (x2 - x1),
                    y1 + mu * (y2 - y1),
                    z1 + mu * (z2 - z1));
            // second intersection
            mu = (-b - (double) Math.sqrt(square(b) - 4 * a * c)) / (2 * a);
            p[1] = new Point(
                    x1 + mu * (x2 - x1),
                    y1 + mu * (y2 - y1),
                    z1 + mu * (z2 - z1));
            result = p;
        }

        return result;

    }

    /*
     * Returns array whose length is equal to the number of intersection of a
     * (finite, with end points p1 and p2) segment and sphere of center s and
     * radius r.
     */
    public static Point[] sphereSegmentIntersection(
            Point p1, Point p2, Point s, double r) {

        Point[] result;
        Point[] ps = sphereLineIntersection(p1, p2, s, r);
        if (ps.length == 0) {
            result = new Point[0];
        } else if (ps.length == 1) {
            double dist1 = ps[0].minus(p1).squaredSize();
            double dist2 = ps[0].minus(p2).squaredSize();
            double dist3 = p1.minus(p2).squaredSize();
            if (dist1 <= dist3 && dist2 <= dist3) {
                result = new Point[1];
                result[0] = ps[0];
                try {
                    checkTolerantBetween(result[0], p1, p2);
                } catch (GeometryException e) {
                    Logger.getLogger("caver").log(Level.WARNING, "", e);
                }
            } else {
                result = new Point[0];
            }
        } else {
            if (!liesInside(p1, s, r) && !liesInside(p2, s, r)) {
                if (between(ps[0], p1, p2) && between(ps[1], p1, p2)) {
                    result = ps;
                } else {
                    result = new Point[0];

                }
            } else if (liesInside(p1, s, r) && liesInside(p2, s, r)) {
                result = new Point[0];
            } else if (liesInside(p1, s, r)) {
                result = new Point[1];
                result[0] = closer(p2, ps[0], ps[1]);
                try {
                    checkTolerantBetween(result[0], p1, p2);
                } catch (GeometryException e) {
                    Logger.getLogger("caver").log(Level.WARNING, "", e);
                }
            } else {
                result = new Point[1];
                result[0] = closer(p1, ps[0], ps[1]);
                try {
                    checkTolerantBetween(result[0], p1, p2);
                } catch (GeometryException e) {
                    Logger.getLogger("caver").log(Level.WARNING, "", e);
                }

            }
        }
        for (Point p : result) {
            try {
                checkTolerantBetween(p, p1, p2);
            } catch (GeometryException e) {
                Logger.getLogger("caver").log(Level.WARNING, "", e);
            }
        }

        return result;
    }

    private static boolean liesInside(Point p, Point s, double r) {
        if (p.minus(s).squaredSize() <= r * r) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean deepInside(Point p, Point s, double r) {
        r -= 0.0001;
        if (p.minus(s).squaredSize() <= r * r) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Removes the parts of segment defined by points p1 and p2 which are
     * outside of the sphere of center s and radius r.
     */
    public static Line outerCut(
            Point p1, Point p2, Point s, double r) {
        Line line;
        if (p1.close(p2)) {
            return null;
        }
        Point[] ps = sphereSegmentIntersection(p1, p2, s, r);
        if (0 == ps.length) { // outside both spheres            
            if (liesInside(p1, s, r) && liesInside(p2, s, r)) {
                line = new Line(p1, p2);
            } else {
                line = null;
            }
        } else if (1 == ps.length) {

            line = new Line(closer(s, p1, p2), ps[0]);
        } else {

            if (liesInside(p1, s, r) && liesInside(p2, s, r)) {
                line = new Line(p1, p2);
            } else if (!liesInside(p1, s, r) && !liesInside(p2, s, r)) {
                line = new Line(ps[0], ps[1]);
            } else if (liesInside(p1, s, r) && liesInside(ps[0], s, r)) {
                line = new Line(p1, ps[0]);
            } else if (liesInside(p1, s, r) && liesInside(ps[1], s, r)) {
                line = new Line(p1, ps[1]);
            } else if (liesInside(p2, s, r) && liesInside(ps[0], s, r)) {
                line = new Line(p2, ps[0]);
            } else if (liesInside(p2, s, r) && liesInside(ps[1], s, r)) {
                line = new Line(p2, ps[1]);
            } else {
                Logger.getLogger("caver").log(Level.WARNING,
                        "Warning, unexpected situation "
                        + "occured during segmentation of tunnel. "
                        + "Please report this to the authors of Caver.");
                line = new Line(p1, p2); // wrong, hopefully causes only small imprecision
            }

        }
        return line;
    }

    /*
     * Removes the part of segment defined by points p1 and p2 which is inside
     * the sphere of center s and radius r.
     */
    public static Line[] innerCut(
            Point p1, Point p2, Point s, double r) {
        Line[] lines;
        if (p1.close(p2)) {
            return new Line[0];
        }
        Point[] ps = sphereSegmentIntersection(p1, p2, s, r);
        if (0 == ps.length) {

            if (liesInside(p1, s, r) && liesInside(p2, s, r)) {
                lines = new Line[0];
            } else {
                lines = new Line[1];
                lines[0] = new Line(p1, p2);
            }
        } else if (1 == ps.length) {
            if (liesInside(p1, s, r) && liesInside(p2, s, r)) {
                lines = new Line[0];
            } else {
                lines = new Line[1];
                double dist1 = p1.minus(s).squaredSize();
                double dist2 = p2.minus(s).squaredSize();
                if (dist1 < dist2) {
                    lines[0] = new Line(ps[0], p2);
                } else {
                    lines[0] = new Line(ps[0], p1);
                }
            }
        } else {
            if (deepInside(p1, s, r)) {
                Logger.getLogger("caver").log(Level.WARNING,
                        "Warning: unexpected situation deep_inside_1");
            }
            if (deepInside(p2, s, r)) {
                Logger.getLogger("caver").log(Level.WARNING,
                        "Warning: unexpected situation deep_inside_2");
            }
            lines = new Line[2];
            double dist1 = p1.minus(ps[0]).squaredSize();
            double dist2 = p2.minus(ps[0]).squaredSize();
            if (dist1 < dist2) {
                lines[0] = new Line(p1, ps[0]);
                lines[1] = new Line(p2, ps[1]);
            } else {
                lines[0] = new Line(p1, ps[1]);
                lines[1] = new Line(p2, ps[0]);
            }
            for (Line li : lines) {
                Point p = li.getA();
                if (deepInside(p, s, r)) {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "Warning: unexpected situation deep_inside_3");
                }

            }

        }
        return lines;
    }

    public static Point closer(Point x, Point a, Point b) {
        double dist1 = x.minus(a).squaredSize();
        double dist2 = x.minus(b).squaredSize();
        if (dist1 < dist2) {
            return a;
        } else {
            return b;
        }
    }

    private static double r() {
        return (double) (random.nextInt(10000) - 5000) / 10000;
    }

    private static double s() {
        return (double) (random.nextInt(5000) + 20) / 10000;
    }
    private static Random random;

    public static Line[] layerCut(Point a, Point b, Point s, double r1, double r2) {
        Line line = outerCut(a, b, s, r2);
        Line[] lines = new Line[0];
        if (null != line) {
            try {
                checkTolerantBetween(line.getA(), a, b);
            } catch (GeometryException e) {
                Logger.getLogger("caver").log(Level.WARNING, "", e);

            }
            try {
                checkTolerantBetween(line.getB(), a, b);
            } catch (GeometryException e) {
                Logger.getLogger("caver").log(Level.WARNING, "", e);
            }


            if (0 < r1) {
                lines = innerCut(line.getA(), line.getB(), s, r1);
            } else {
                lines = new Line[1];
                lines[0] = line;
            }
        }
        return lines;
    }

    public static boolean between(Point p, Point a, Point b) {
        if (a.distance(b) < p.distance(a) || a.distance(b) < p.distance(b)) {
            return false;
        } else {
            return true;
        }
    }

    public static void checkTolerantBetween(Point p, Point a, Point b)
            throws GeometryException {
        double ab = a.distance(b) + 0.001;
        if (ab < p.distance(a)) {
            throw new GeometryException(ab + " < " + p.distance(a));

        }
        if (ab < p.distance(b)) {
            throw new GeometryException(ab + " < " + p.distance(b));
        }
    }
}
