package algorithms.triangulation;

import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 *
 * Smaller version of VE.
 *
 * For use in tunnels, to save memory and serialization cost.
 *
 */
public class TVE implements Serializable {
    // limiting sphere, defines max probe radius in every point on AB
    private double sx, sy, sz, sr;
    private double ax, ay, az; // point A
    private double bx, by, bz; // point B
    public static final int doubles = 10;
    public static final int bytes = 8 * doubles;

    private TVE() {
    }

    public TVE(VE e) {
        sx = e.getS().getS().getX();
        sy = e.getS().getS().getY();
        sz = e.getS().getS().getZ();
        sr = e.getS().getR();

        ax = e.getA().getX();
        ay = e.getA().getY();
        az = e.getA().getZ();

        bx = e.getB().getX();
        by = e.getB().getY();
        bz = e.getB().getZ();
    }

    public double length() {
        Point a = new Point(ax, ay, az);
        Point b = new Point(bx, by, bz);
        return a.distance(b);
    }

    public TVE(DataInputStream dis) {
        try {

            sx = dis.readDouble();
            sy = dis.readDouble();
            sz = dis.readDouble();
            sr = dis.readDouble();

            ax = dis.readDouble();
            ay = dis.readDouble();
            az = dis.readDouble();

            bx = dis.readDouble();
            by = dis.readDouble();
            bz = dis.readDouble();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void skip(DataInputStream dis, int numberOfEdges) {
        try {
            for (int i = 0; i < doubles * numberOfEdges; i++) {
                dis.readDouble();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(DataOutputStream dos) {
        try {

            dos.writeDouble(sx);
            dos.writeDouble(sy);
            dos.writeDouble(sz);
            dos.writeDouble(sr);

            dos.writeDouble(ax);
            dos.writeDouble(ay);
            dos.writeDouble(az);

            dos.writeDouble(bx);
            dos.writeDouble(by);
            dos.writeDouble(bz);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * step - distance of sphere centers
     * phase - distance of first center to point A
     * returns - next phase
     */
    public double getSpheres(double step, double phase, List<Sphere> spheres) {

        Point a = new Point(ax, ay, az);
        Point b = new Point(bx, by, bz);
        Point c = new Point(sx, sy, sz);

        double size = a.distance(b);
        int n = 0;
        if (size <= phase) {
            return phase - size;
        } else {
            n = (int) Math.floor((size - phase) / step);
        }

        Point v = b.minus(a).normalize(); // unit vector

        assert Math.abs(v.size() - 1) < 0.001;

        for (int i = 0; i <= n; i++) {
            Point p = a.plus(v.multiply(phase + i * step));

            double w = c.distance(p) - sr;
            if (w < 0) {
                w = 0;
            }
            Sphere s = new Sphere(p, w);
            spheres.add(s);
        }

        double rest = size - phase - n * step;

        assert rest < step;
        assert 0 <= rest;

        return step - rest;
    }
}
