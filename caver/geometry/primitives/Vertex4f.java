package geometry.primitives;

import geometry.GeometryConstants;

public class Vertex4f {

    public float[] m_pPos = {0.f, 0.f, 0.f, 0.f};
    public int hc = Integer.MIN_VALUE;

    /**
     * Creates a new instance of Vertex4f
     */
    public Vertex4f(float x, float y, float z, float r) {
        set(x, y, z, r);
    }

    public Vertex4f() {
        set(0.0f, 0.0f, 0.0f, 0.0f);
    }

    private void set(float x, float y, float z, float r) {
        m_pPos[0] = x;
        m_pPos[1] = y;
        m_pPos[2] = z;
        setRadius(r);
    }

    public float getX() {
        return m_pPos[0];
    }

    public float getY() {
        return m_pPos[1];
    }

    public float getZ() {
        return m_pPos[2];
    }

    public float getRadius() {
        return m_pPos[3];
    }

    public float[] getCoord() {
        return m_pPos;
    }

    public void setRadius(float r) {
        m_pPos[3] = r;
    }

    public void changeRadius(float r) {
        m_pPos[3] += r;
    }

    public static float dist(Vertex4f v1, Vertex4f v2) {
        return (float) Math.sqrt((v1.getX() - v2.getX()) * (v1.getX() - v2.getX())
                + (v1.getY() - v2.getY()) * (v1.getY() - v2.getY())
                + (v1.getZ() - v2.getZ()) * (v1.getZ() - v2.getZ()));
    }

    public static float getIntersection(Vertex4f v1, Vertex4f v2) {
        return (v1.getRadius() + v2.getRadius()) - dist(v1, v2);
    }

    @Override
    public String toString() {
        return "[" + getX() + ", " + getY() + ", " + getZ() + ", " + getRadius() + "]";
    }

    public static String toString(float x, float y, float z, float r) {
        return "(" + x + ", " + y + ", " + z + ", " + r + ")";
    }

    public static boolean intersect(Vertex4f v1, Vertex4f v2) {
        return getIntersection(v1, v2) > GeometryConstants.EPS;
    }

    public Vertex3f toVertex3f() {
        Vertex3f v = new Vertex3f();
        v.m_pPos[0] = m_pPos[0];
        v.m_pPos[1] = m_pPos[1];
        v.m_pPos[2] = m_pPos[2];
        return v;
    }

    public static Vertex4f average(Vertex4f v1, Vertex4f v2, Vertex4f v3) {
        float x = (v1.getX() + v2.getX() + v3.getX()) / 3.0f;
        float y = (v1.getY() + v2.getY() + v3.getY()) / 3.0f;
        float z = (v1.getZ() + v2.getZ() + v3.getZ()) / 3.0f;
        return new Vertex4f(x, y, z, 1.0f);
    }

    public static Vertex4f average(Vertex4f v1, Vertex4f v2, Vertex4f v3,
            Vertex4f v4) {
        float x = (v1.getX() + v2.getX() + v3.getX() + v4.getX()) / 4.0f;
        float y = (v1.getY() + v2.getY() + v3.getY() + v4.getY()) / 4.0f;
        float z = (v1.getZ() + v2.getZ() + v3.getZ() + v4.getZ()) / 4.0f;
        return new Vertex4f(x, y, z, 1.0f);

    }

    public static Vertex4f interpolate(Vertex4f v1, Vertex4f v2,
            float percentage) {
        Vertex4f dir = Vertex4f.minus(v2, v1);
        dir.multiply(percentage);
        Vertex4f interpolated = Vertex4f.plus(v1, dir);
        return interpolated;

    }

    public static Vertex4f minus(Vertex4f p1, Vertex4f p2) {
        Vertex4f v = new Vertex4f();
        v.m_pPos[0] = p1.m_pPos[0] - p2.m_pPos[0];
        v.m_pPos[1] = p1.m_pPos[1] - p2.m_pPos[1];
        v.m_pPos[2] = p1.m_pPos[2] - p2.m_pPos[2];
        v.m_pPos[3] = p1.m_pPos[3] - p2.m_pPos[3];

        return v;
    }

    public static Vertex4f plus(Vertex4f p1, Vertex4f p2) {
        Vertex4f v = new Vertex4f();
        v.m_pPos[0] = p1.m_pPos[0] + p2.m_pPos[0];
        v.m_pPos[1] = p1.m_pPos[1] + p2.m_pPos[1];
        v.m_pPos[2] = p1.m_pPos[2] + p2.m_pPos[2];
        v.m_pPos[3] = p1.m_pPos[3] + p2.m_pPos[3];

        return v;
    }

    public void multiply(float constant) {

        m_pPos[0] = m_pPos[0] * constant;
        m_pPos[1] = m_pPos[1] * constant;
        m_pPos[2] = m_pPos[2] * constant;
        m_pPos[3] = m_pPos[3] * constant;

    }

    public static Vertex4f multiply(Vertex4f p1, float constant) {
        Vertex4f v = new Vertex4f();
        v.m_pPos[0] = p1.m_pPos[0] * constant;
        v.m_pPos[1] = p1.m_pPos[1] * constant;
        v.m_pPos[2] = p1.m_pPos[2] * constant;
        v.m_pPos[3] = p1.m_pPos[3] * constant;

        return v;
    }

    public static Vertex4f normalize(Vertex4f v) {
        float size = (float) Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY()
                + v.getZ() * v.getZ());

        return new Vertex4f(v.getX() / size, v.getY() / size,
                v.getZ() / size, 0.0f);
    }

    public void normalize() {
        float s = (float) Math.sqrt(m_pPos[0] * m_pPos[0]
                + m_pPos[1] * m_pPos[1] + m_pPos[2] * m_pPos[2]);

        m_pPos[0] = m_pPos[0] / s;
        m_pPos[1] = m_pPos[1] / s;
        m_pPos[2] = m_pPos[2] / s;
    }

    public void add(Vertex4f v) {
        m_pPos[0] += v.getX();
        m_pPos[1] += v.getY();
        m_pPos[2] += v.getZ();
        m_pPos[3] += v.getRadius();
    }

    public static boolean intersectSpheres(Vertex4f v1, Vertex4f v2) {
        // intersection of SPHERES
        float dist = Vertex4f.dist(v1, v2);
        float sumRadii = v1.getRadius() + v2.getRadius();

        if (dist < sumRadii) {
            return true;
        } else {
            return false;
        }


    }

    public static Vertex4f half(Vertex4f v1, Vertex4f v2) {
        return new Vertex4f(
                (v1.getX() + v2.getX()) / 2.f,
                (v1.getY() + v2.getY()) / 2.f,
                (v1.getZ() + v2.getZ()) / 2.f,
                0.f);
    }

    public void minus(Vertex4f v) {
        m_pPos[0] -= v.getX();
        m_pPos[1] -= v.getY();
        m_pPos[2] -= v.getZ();
        m_pPos[3] -= v.getRadius();
    }

    public float dot(Vertex4f v) {
        return this.getX() * v.getX() + this.getY() * v.getY()
                + this.getZ() * v.getZ();
    }

    @Override
    public int hashCode() {
        // was set before
        if (hc != Integer.MIN_VALUE) {
            return hc;
        } else {
            // compute from three floats - not so unique
            String s =
                    String.valueOf(m_pPos[0])
                    + String.valueOf(m_pPos[1])
                    + String.valueOf(m_pPos[2]);
            return s.hashCode();
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vertex4f other = (Vertex4f) obj;
        if (this.m_pPos[0] == other.m_pPos[0]
                && this.m_pPos[1] == other.m_pPos[1]
                && this.m_pPos[2] == other.m_pPos[2]) {
            return true;
        } else {
            return false;
        }
    }

    public void dispose() {
        m_pPos = null;
    }
}
