package geometry.primitives;

public class Vertex3f {

    public float[] m_pPos = {0.f, 0.f, 0.f};

    public Vertex3f() {
    }

    public Vertex3f(float x, float y, float z) {
        set(x, y, z);
    }

    public Vertex3f(Vertex4f v) {
        set(v.getX(), v.getY(), v.getZ());
    }

    public void normalize() {
        float s = this.size();
        m_pPos[0] = m_pPos[0] / s;
        m_pPos[1] = m_pPos[1] / s;
        m_pPos[2] = m_pPos[2] / s;
    }

    public final void set(float x, float y, float z) {
        m_pPos[0] = x;
        m_pPos[1] = y;
        m_pPos[2] = z;
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

    public static Vertex3f plus(Vertex3f p1, Vertex3f p2) {
        Vertex3f v = new Vertex3f();
        v.m_pPos[0] = p1.m_pPos[0] + p2.m_pPos[0];
        v.m_pPos[1] = p1.m_pPos[1] + p2.m_pPos[1];
        v.m_pPos[2] = p1.m_pPos[2] + p2.m_pPos[2];
        return v;
    }

    public static Vertex3f minus(Vertex3f p1, Vertex3f p2) {
        Vertex3f v = new Vertex3f();
        v.m_pPos[0] = p1.m_pPos[0] - p2.m_pPos[0];
        v.m_pPos[1] = p1.m_pPos[1] - p2.m_pPos[1];
        v.m_pPos[2] = p1.m_pPos[2] - p2.m_pPos[2];
        return v;
    }

    public float dist(Vertex3f v2) {
        Vertex3f v1 = this;
        return (float) Math.sqrt((v1.getX() - v2.getX()) * (v1.getX() - v2.getX())
                + (v1.getY() - v2.getY()) * (v1.getY() - v2.getY())
                + (v1.getZ() - v2.getZ()) * (v1.getZ() - v2.getZ()));
    }

    public static float dist(Vertex3f v1, Vertex3f v2) {
        return (float) Math.sqrt((v1.getX() - v2.getX()) * (v1.getX() - v2.getX())
                + (v1.getY() - v2.getY()) * (v1.getY() - v2.getY())
                + (v1.getZ() - v2.getZ()) * (v1.getZ() - v2.getZ()));
    }

    public float size() {
        return (float) Math.sqrt(m_pPos[0] * m_pPos[0] + m_pPos[1] * m_pPos[1]
                + m_pPos[2] * m_pPos[2]);
    }

    public static Vertex3f normalize(Vertex3f v) {
        //return multiply(1/v.size(), v);
        float s = v.size();
        Vertex3f V = new Vertex3f();
        V.m_pPos[0] = v.m_pPos[0] / s;
        V.m_pPos[1] = v.m_pPos[1] / s;
        V.m_pPos[2] = v.m_pPos[2] / s;
        return V;
    }

    public static Vertex3f multiply(float f, Vertex3f v) {
        Vertex3f V = new Vertex3f();
        V.m_pPos[0] = v.m_pPos[0] * f;
        V.m_pPos[1] = v.m_pPos[1] * f;
        V.m_pPos[2] = v.m_pPos[2] * f;
        return V;
    }

    public void multiply(float f) {
        m_pPos[0] = m_pPos[0] * f;
        m_pPos[1] = m_pPos[1] * f;
        m_pPos[2] = m_pPos[2] * f;
    }

    public float dot(Vertex3f v) {
        return this.getX() * v.getX() + this.getY() * v.getY()
                + this.getZ() * v.getZ();
    }

    @Override
    public String toString() {
        return getX() + " " + getY() + " " + getZ();
    }

    public static String toString(float x, float y, float z) {
        return x + " " + y + " " + z;
    }

    public static Vertex3f cross(Vertex3f v1, Vertex3f v2) {
        Vertex3f v = new Vertex3f();
        v.m_pPos[0] = v1.m_pPos[1] * v2.m_pPos[2] - v2.m_pPos[1] * v1.m_pPos[2];
        v.m_pPos[1] = v1.m_pPos[2] * v2.m_pPos[0] - v2.m_pPos[2] * v1.m_pPos[0];
        v.m_pPos[2] = v1.m_pPos[0] * v2.m_pPos[1] - v2.m_pPos[0] * v1.m_pPos[1];

        return v;
    }

    public static float dot(Vertex3f v1, Vertex3f v2) {
        return v1.m_pPos[0] * v2.m_pPos[0] + v1.m_pPos[1] * v2.m_pPos[1]
                + v1.m_pPos[2] * v2.m_pPos[2];
    }

    public void setX(float x) {
        m_pPos[0] = x;
    }

    public void setY(float y) {
        m_pPos[1] = y;
    }

    public void setZ(float z) {
        m_pPos[2] = z;
    }

    public Vertex3f multiplyByMatrix(float mat00, float mat01, float mat02,
            float mat10, float mat11, float mat12, float mat20, float mat21,
            float mat22) {
        float x = m_pPos[0];
        float y = m_pPos[1];
        float z = m_pPos[2];

        float newx = x * mat00 + y * mat10 + z * mat20;
        float newy = x * mat01 + y * mat11 + z * mat21;
        float newz = x * mat02 + y * mat12 + z * mat22;


        Vertex3f result = new Vertex3f(newx, newy, newz);
        return result;
    }

    public void add(Vertex3f v) {
        m_pPos[0] += v.getX();
        m_pPos[1] += v.getY();
        m_pPos[2] += v.getZ();
    }

    public void minus(Vertex3f v) {
        m_pPos[0] -= v.getX();
        m_pPos[2] -= v.getY();
        m_pPos[3] -= v.getZ();
    }

    public static Vertex3f average(Vertex3f v1, Vertex3f v2, Vertex3f v3,
            Vertex3f v4) {
        float x = (v1.getX() + v2.getX() + v3.getX() + v4.getX()) / 4.0f;
        float y = (v1.getY() + v2.getY() + v3.getY() + v4.getY()) / 4.0f;
        float z = (v1.getZ() + v2.getZ() + v3.getZ() + v4.getZ()) / 4.0f;

        return new Vertex3f(x, y, z);
    }

    public void dispose() {
        m_pPos = null;
    }
}
