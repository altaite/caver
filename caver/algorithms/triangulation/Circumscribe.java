package algorithms.triangulation;

import geometry.primitives.Sphere;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REWRITTEN TO JAVA from http://paulbourke.net/geometry/spherefrom4/ ... final
 * version from http://www.gamedev.net/topic/147131-4x4-determinants/
 */
public class Circumscribe {

    /**
     * Sphere passing through 4 points in 3d
     *
     * @param p array of 4 points
     * @return Sphere inscribed to four spheres, if exists, if points do not
     * define a sphere, null is returned
     */
    public static Sphere getTangent(double[][] p, double radius) {
        double[] a;
        double m11;
        double m12;
        double m13;
        double m14;
        double m15;
        int i;
        a = new double[16];
        for (i = 0; i < 4; i++) {
            a[i * 4 + 0] = p[i][0];
            a[i * 4 + 1] = p[i][1];
            a[i * 4 + 2] = p[i][2];
            a[i * 4 + 3] = 1;
        }
        m11 = fastDeterminant(a);
        double[] sizes = new double[4];
        for (i = 0; i < 4; i++) {
            sizes[i] =
                    p[i][0] * p[i][0] + p[i][1] * p[i][1] + p[i][2] * p[i][2];
        }
        for (i = 0; i < 4; i++) {
            a[i * 4 + 0] = sizes[i];
            a[i * 4 + 1] = p[i][1];
            a[i * 4 + 2] = p[i][2];
            a[i * 4 + 3] = 1;
        }
        m12 = fastDeterminant(a);
        for (i = 0; i < 4; i++) {
            a[i * 4 + 0] = p[i][0];
            a[i * 4 + 1] = sizes[i];
            a[i * 4 + 2] = p[i][2];
            a[i * 4 + 3] = 1;
        }
        m13 = fastDeterminant(a);
        for (i = 0; i < 4; i++) {
            a[i * 4 + 0] = p[i][0];
            a[i * 4 + 1] = p[i][1];
            a[i * 4 + 2] = sizes[i];
            a[i * 4 + 3] = 1;
        }
        m14 = fastDeterminant(a);
        for (i = 0; i < 4; i++) {
            a[i * 4 + 0] = sizes[i];
            a[i * 4 + 1] = p[i][0];
            a[i * 4 + 2] = p[i][1];
            a[i * 4 + 3] = p[i][2];
        }
        m15 = fastDeterminant(a);
        if (m11 == 0) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "no sphere defined by these points");
            return null;
        }
        double x = m12 / (m11 * 2);
        double y = m13 / (m11 * 2);
        double z = m14 / (m11 * 2);
        double r = (double) Math.sqrt(x * x + y * y + z * z - m15 / m11);

        Sphere sphere = new Sphere(x, y, z, r - radius);
        return sphere;
    }

    private static double fastDeterminant(double[] d) {

        double det1 = d[10] * (d[15] * d[5] - d[7] * d[13]) + d[11]
                * (d[13] * d[6] - d[5] * d[14]) + d[9]
                * (d[14] * d[7] - d[6] * d[15]);

        double det2 = d[1] * (d[10] * d[15] - d[11] * d[14]) + d[2]
                * (d[11] * d[13] - d[9] * d[15]) + d[3]
                * (d[9] * d[14] - d[10] * d[13]);

        double det3 = d[1] * (d[6] * d[15] - d[7] * d[14]) + d[2]
                * (d[7] * d[13] - d[5] * d[15]) + d[3]
                * (d[5] * d[14] - d[6] * d[13]);

        double det4 = d[1] * (d[6] * d[11] - d[7] * d[10]) + d[2]
                * (d[7] * d[9] - d[5] * d[11]) + d[3]
                * (d[5] * d[10] - d[6] * d[9]);

        return (d[0] * det1 - d[4] * det2 + d[8] * det3 - d[12] * det4);
    }
}
