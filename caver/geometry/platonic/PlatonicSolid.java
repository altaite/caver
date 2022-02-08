package geometry.platonic;

import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * Provides approximation of a ball by a set of balls placed into vertices of a
 * platonic solid polyhedral.
 */
public class PlatonicSolid {

    public static Point[] getOneSpherePoint() {
        Point[] tetrahedron = {
            new Point(0, 0, 0)
        };
        return tetrahedron;
    }

    public static Point[] getTetrahedronPoints() {
        Point[] tetrahedron = {
            new Point(1, 1, 1),
            new Point(-1, -1, 1),
            new Point(-1, 1, -1),
            new Point(1, -1, -1)
        };
        return tetrahedron;
    }

    public static Point[] getOctahedronPoints() {
        Point[] octahedron = {
            new Point(1, 0, 0),
            new Point(-1, 0, 0),
            new Point(0, 1, 0),
            new Point(0, -1, 0),
            new Point(0, 0, 1),
            new Point(0, 0, -1)
        };
        return octahedron;
    }

    public static Point[] getHexahedronPoints() {
        Point[] hexahedron = {
            new Point(1, 1, 1),
            new Point(-1, 1, 1),
            new Point(-1, -1, 1),
            new Point(1, -1, 1),
            new Point(1, 1, -1),
            new Point(-1, 1, -1),
            new Point(-1, -1, -1),
            new Point(1, -1, -1)
        };
        return hexahedron;
    }

    public static Point[] getIcosahedronPoints() {
        double _ = 0.525731;
        double __ = 0.850650;
        Point[] icosahedron = {
            new Point(-_, 0, __),
            new Point(_, 0, __),
            new Point(-_, 0, -__),
            new Point(_, 0, -__),
            new Point(0, __, _),
            new Point(0, __, -_),
            new Point(0, -__, _),
            new Point(0, -__, -_),
            new Point(__, _, 0),
            new Point(-__, _, 0),
            new Point(__, -_, 0),
            new Point(-__, -_, 0)
        };
        return icosahedron;
    }

    public static Point[] getDodecahedronPoints() {
        double _ = 1.618033; //golden mean
        double __ = 0.618033;
        Point[] dodecahedron = {
            new Point(0, __, _),
            new Point(0, __, -_),
            new Point(0, -__, _),
            new Point(0, -__, -_),
            new Point(_, 0, __),
            new Point(_, 0, -__),
            new Point(-_, 0, __),
            new Point(-_, 0, -__),
            new Point(__, _, 0),
            new Point(__, -_, 0),
            new Point(-__, _, 0),
            new Point(-__, -_, 0),
            new Point(1, 1, 1),
            new Point(1, 1, -1),
            new Point(1, -1, 1),
            new Point(1, -1, -1),
            new Point(-1, 1, 1),
            new Point(-1, 1, -1),
            new Point(-1, -1, 1),
            new Point(-1, -1, -1)
        };
        return dodecahedron;
    }

    public void PlatonicSolid(double r) throws IOException {
        InputStream is = this.getClass().getResourceAsStream("icosahedron.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while (null != (line = br.readLine())) {
            if (0 == line.trim().length()) {
                continue;
            }
        }
    }

    public static Point rotate(Point p, int axis, double a) {

        double x = p.getX();
        double y = p.getY();
        double z = p.getZ();


        double xr;
        double yr;
        double zr;

        switch (axis) {
            case 0:
                xr = x;
                yr = y * Math.cos(a) - z * Math.sin(a);
                zr = y * Math.sin(a) + z * Math.cos(a);
                break;
            case 1:
                xr = x * Math.cos(a) - z * Math.sin(a);
                yr = y;
                zr = x * Math.sin(a) + z * Math.cos(a);
                break;
            case 2:
                xr = x * Math.cos(a) - y * Math.sin(a);
                yr = x * Math.sin(a) + y * Math.cos(a);
                zr = z;
                break;
            default:
                throw new RuntimeException();
        }
        Point cr = new Point(xr, yr, zr);

        return cr;
    }

    public static List<Sphere> getSphericalApproximation(Sphere sphere,
            double smallRadius, double bigRadius, int n, boolean central,
            Random random) {

        Sphere[] ss = getRose(smallRadius,
                bigRadius, n, random);

        List<Sphere> spheres = new ArrayList<Sphere>();
        for (int i = 0; i < ss.length; i++) {
            spheres.add(new Sphere(
                    ss[i].getS().plus(sphere.getS()), ss[i].getR()));
        }

        // add one to center to avoid cospherical degenerate cases
        if (central && 1 < spheres.size()) {
            spheres.add(new Sphere(sphere.getS(), smallRadius));
        }

        return spheres;
    }

    private static Sphere[] getRose(double smallRadius,
            double bigRadius, int n, Random random) {

        Point[] points;
        if (1 == n) {
            points = getOneSpherePoint();
        } else if (4 == n) {
            points = getTetrahedronPoints();
        } else if (6 == n) {
            points = getOctahedronPoints();
        } else if (8 == n) {
            points = getHexahedronPoints();
        } else if (12 == n) {
            points = getIcosahedronPoints();
        } else if (20 == n) {
            points = getDodecahedronPoints();
        } else {

            throw new RuntimeException("Illegal number of approximating "
                    + "spheres: " + n + ". Use 4, 6, 8, 12 or 20.");
        }

        Sphere[] spheres = new Sphere[points.length];

        // random angle for rotation around each axis
        double[] angles = new double[3];
        for (int i = 0; i < 3; i++) {
            angles[i] = random.nextDouble() * Math.PI * 2;
        }

        int i = 0;
        for (Point p : points) {
            Point c;
            if (1 == n) {
                c = p;
            } else {
                Point radiusVector = p.multiply(smallRadius);
                c = p.multiply(bigRadius).minus(radiusVector);
                c = rotate(c, 0, angles[0]);
                c = rotate(c, 1, angles[1]);
                c = rotate(c, 2, angles[2]);
            }
            Sphere s = new Sphere(c,
                    smallRadius);
            spheres[i++] = s;
        }
        return spheres;

    }
}
