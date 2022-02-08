package algorithms.triangulation.qhull;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class Triangle {

    private Vector3f[] vertex;

    public Triangle(Vector3f v1, Vector3f v2, Vector3f v3) {
        this.vertex = new Vector3f[3];
        this.vertex[0] = v1;
        this.vertex[1] = v2;
        this.vertex[2] = v3;
    }

    public Point3f closestPointOnTriangle(Point3f pointP) {
        Vector3f point = new Vector3f(pointP);

        Vector3f rab = closestPointOnLine(point, this.vertex[0], this.vertex[2]);
        Vector3f rbc = closestPointOnLine(point, this.vertex[2], this.vertex[1]);
        Vector3f rca = closestPointOnLine(point, this.vertex[1], this.vertex[0]);
        Vector3f tmp = new Vector3f();

        tmp.sub(point, rab);
        double dab = tmp.length();

        tmp.sub(point, rbc);
        double dbc = tmp.length();

        tmp.sub(point, rca);
        double dca = tmp.length();

        double min = dab;
        Vector3f result = rab;

        if (dbc < min) {
            min = dbc;
            result = rbc;
        }
        if (dca < min) {
            result = rca;
        }
        return new Point3f(result);
    }

    private Vector3f closestPointOnLine(Vector3f point, Vector3f lineStart, Vector3f lineEnd) {
        Vector3f p = new Vector3f(point);

        Vector3f c = new Vector3f();
        c.sub(p, lineStart);
        Vector3f v = new Vector3f();
        v.sub(lineEnd, lineStart);
        float d = v.length();

        v.normalize();
        float t = v.dot(c);

        if (t < 0.0F) {
            return lineStart;
        }
        if (t > d) {
            return lineEnd;
        }
        v.scale(t);
        Vector3f result = new Vector3f();
        result.add(lineStart, v);
        return result;
    }

    boolean pointInTriangle(Point3f pointP) {
        Vector3f point = new Vector3f(pointP);
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();

        v1.sub(point, this.vertex[0]);
        v2.sub(point, this.vertex[2]);
        v3.sub(point, this.vertex[1]);
        v1.normalize();
        v2.normalize();
        v3.normalize();
        float totalangle = (float) Math.acos(v1.dot(v2));

        totalangle += (float) Math.acos(v2.dot(v3));
        totalangle += (float) Math.acos(v3.dot(v1));

        return (float) Math.abs(totalangle - 6.283185307179586D) <= 0.005F;
    }
}

