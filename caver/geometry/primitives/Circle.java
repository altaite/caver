package geometry.primitives;

import geometry.GeometryConstants;

public class Circle {

    private float[] m_pPos = {0.f, 0.f, 0.f};

    public Circle(float x, float y, float r) {
        set(x, y, r);
    }

    private void set(float x, float y, float r) {
        m_pPos[0] = x;
        m_pPos[1] = y;
        m_pPos[2] = r;
    }

    public static Vertex4f tangetial(Vertex4f v1, Vertex4f v2, Vertex4f v3) {
        float a = Vertex4f.dist(v1, v2);
        float b = Vertex4f.dist(v2, v3);
        float c = Vertex4f.dist(v3, v1);

        float x = (a * a - b * b + c * c) / (2.f * a);
        float v = (float) Math.sqrt(c * c - x * x);

        Circle c1 = new Circle(0.f, 0.f, v1.getRadius());
        Circle c2 = new Circle(a, 0.f, v2.getRadius());
        Circle c3 = new Circle(x, v, v3.getRadius());

        if (intersect(c1, c2) && intersect(c2, c3) && intersect(c3, c1)) {
            Point[] ps = new Point[3];
            ps[0] = new Point(v1);
            ps[1] = new Point(v2);
            ps[2] = new Point(v3);
            Point t = ps[0].plus(ps[1]).plus(ps[2]).divide(3);

            return new Vertex4f((float) t.getX(), (float) t.getY(),
                    (float) t.getZ(), 0.f);
        } else {
            Circle nw1 = getTangent(c1, c2, c3);

            Vertex3f pt = Vertex3f.normalize(
                    Vertex3f.minus(new Vertex3f(v2), new Vertex3f(v1)));

            Vertex3f p = Vertex3f.plus(new Vertex3f(v1), Vertex3f.multiply(nw1.getX(), pt));
            Vertex3f pk = Vertex3f.plus(new Vertex3f(v1), Vertex3f.multiply(x, pt));
            Vertex3f q = Vertex3f.plus(p, Vertex3f.multiply(nw1.getY(),
                    Vertex3f.normalize(
                    Vertex3f.minus(new Vertex3f(v3), pk))));

            Vertex4f result = new Vertex4f(q.getX(), q.getY(), q.getZ(), nw1.getRadius());

            return result;
        }
    }

    public static Vertex4f tangetialFast(Vertex4f v1, Vertex4f v2, Vertex4f v3) {
        float a0 = Vertex4f.dist(v1, v2);
        float b0 = Vertex4f.dist(v2, v3);
        float c0 = Vertex4f.dist(v3, v1);

        float x0 = (a0 * a0 - b0 * b0 + c0 * c0) / (2.f * a0);
        float x0x0 = x0 * x0;
        float v = (float) Math.sqrt(c0 * c0 - x0x0);
        float vv = v * v;

        float inter1 = v1.getRadius() + v2.getRadius() - a0;
        float inter2 = v2.getRadius() + v3.getRadius() - (float) Math.sqrt((x0 - a0) * (x0 - a0) + vv);
        float inter3 = v3.getRadius() + v1.getRadius() - (float) Math.sqrt(x0x0 + vv);

        if (inter1 > GeometryConstants.EPS && inter2 > GeometryConstants.EPS && inter3 > GeometryConstants.EPS) {
            return new Vertex4f(v1.getX(), v1.getY(), v1.getZ(), 0.f);
        } else {

            float x1 = 0.f;
            float y1 = 0.f;
            float r1 = v1.getRadius();

            float x2 = a0;
            float y2 = 0;
            float r2 = v2.getRadius();

            float x3 = x0;
            float y3 = v;
            float r3 = v3.getRadius();

            int vari_1 = -1;
            int vari_2 = -1;

            float a = 2 * (x1 - x2);
            float b = 2 * (y1 - y2);
            float c = vari_1 * 2 * (r1 - r2);
            float d = (x1 * x1 + y1 * y1 - r1 * r1)
                    - (x2 * x2 + y2 * y2 - r2 * r2);

            // a_ denotes a'
            float a_ = 2 * (x1 - x3);
            float b_ = 2 * (y1 - y3);
            float c_ = vari_2 * 2 * (r1 - r3);
            float d_ = (x1 * x1 + y1 * y1 - r1 * r1)
                    - (x3 * x3 + y3 * y3 - r3 * r3);

            float divisor = (a * b_ - b * a_);
            float k = (b_ * d - b * d_) / divisor;
            float l = (-b_ * c + b * c_) / divisor;
            float m = (-a_ * d + a * d_) / divisor;
            float n = (a_ * c - a * c_) / divisor;

            float o = k - x1;
            float p = m - y1;

            float xx1 = l * l + n * n - 1;
            float xx2 = 2 * o * l + 2 * p * n - vari_1 * 2 * r1;
            float xx3 = o * o + p * p - r1 * r1;

            float diskr = (float) Math.sqrt(xx2 * xx2 - 4 * xx1 * xx3);
            float res0 = (-xx2 + diskr) / (2 * xx1);
            float res1 = (-xx2 - diskr) / (2 * xx1);

            float xa1 = k + l * res0;
            float ya1 = m + n * res0;

            float verified_result = 999;

            float presaved = (xa1 - x1) * (xa1 - x1) + (ya1 - y1) * (ya1 - y1);
            if (Math.abs(presaved - (res0 - r1) * (res0 - r1)) < 0.5) {
                verified_result = res0;
            } else if (Math.abs(presaved - (res1 - r1) * (res1 - r1)) < 0.5) {
                verified_result = res1;
            } else if (Math.abs(presaved - (res0 - r1) * (res0 - r1)) < 0.5) {
                verified_result = res0;
            } else if (Math.abs(presaved - (res1 - r1) * (res1 - r1)) < 0.5) {
                verified_result = res1;
            } else {
            }

            float x = k + l * verified_result;
            float y = m + n * verified_result;

            float corrected_radius = (float) Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1)) - r1;
            if (corrected_radius < 0.f) {
                corrected_radius = 0.f;
            }

            float minusx = v2.getX() - v1.getX();
            float minusy = v2.getY() - v1.getY();
            float minusz = v2.getZ() - v1.getZ();

            float minussize = (float) Math.sqrt(minusx * minusx + minusy
                    * minusy + minusz * minusz);
            minusx /= minussize;
            minusy /= minussize;
            minusz /= minussize;

            float px = v1.getX() + x * minusx;
            float py = v1.getY() + x * minusy;
            float pz = v1.getZ() + x * minusz;

            float pkx = v1.getX() + x0 * minusx;
            float pky = v1.getY() + x0 * minusy;
            float pkz = v1.getZ() + x0 * minusz;

            float v3_pkx = v3.getX() - pkx;
            float v3_pky = v3.getY() - pky;
            float v3_pkz = v3.getZ() - pkz;

            float v3_pksize = (float) Math.sqrt(v3_pkx * v3_pkx + v3_pky * v3_pky + v3_pkz * v3_pkz);
            v3_pkx /= v3_pksize;
            v3_pky /= v3_pksize;
            v3_pkz /= v3_pksize;

            float qx = px + y * v3_pkx;
            float qy = py + y * v3_pky;
            float qz = pz + y * v3_pkz;

            Vertex4f vresult = new Vertex4f(qx, qy, qz, corrected_radius);
            return vresult;
        }
    }

    public static float[] solveQuadratic(float a, float b, float c) {
        float[] result = {0.f, 0.f};

        float diskr = (float) Math.sqrt(b * b - 4 * a * c);
        result[0] = (-b + diskr) / (2 * a);
        result[1] = (-b - diskr) / (2 * a);
        return result;
    }

    public static Circle getTangent(Circle c1, Circle c2, Circle c3) {
        float x1 = c1.getX();
        float y1 = c1.getY();
        float r1 = c1.getRadius();

        float x2 = c2.getX();
        float y2 = c2.getY();
        float r2 = c2.getRadius();

        float x3 = c3.getX();
        float y3 = c3.getY();
        float r3 = c3.getRadius();

        int vari_1 = -1;
        int vari_2 = -1;

        float a = 2 * (x1 - x2);
        float b = 2 * (y1 - y2);
        float c = vari_1 * 2 * (r1 - r2);
        float d = (x1 * x1 + y1 * y1 - r1 * r1) - (x2 * x2 + y2 * y2 - r2 * r2);

        // a_ denotes a'
        float a_ = 2 * (x1 - x3);
        float b_ = 2 * (y1 - y3);
        float c_ = vari_2 * 2 * (r1 - r3);
        float d_ = (x1 * x1 + y1 * y1 - r1 * r1) - (x3 * x3 + y3 * y3 - r3 * r3);

        float k = (b_ * d - b * d_) / (a * b_ - b * a_);
        float l = (-b_ * c + b * c_) / (a * b_ - b * a_);
        float m = (-a_ * d + a * d_) / (a * b_ - b * a_);
        float n = (a_ * c - a * c_) / (a * b_ - b * a_);

        float o = k - x1;
        float p = m - y1;

        float xx1 = l * l + n * n - 1;
        float xx2 = 2 * o * l + 2 * p * n - vari_1 * 2 * r1;
        float xx3 = o * o + p * p - r1 * r1;

        float[] result = solveQuadratic(xx1, xx2, xx3);

        float xa1 = k + l * result[0];
        float xa2 = k + l * result[1];
        float ya1 = m + n * result[0];
        float ya2 = m + n * result[1];


        float verified_result = 999;


        if (Math.abs((xa1 - x1) * (xa1 - x1) + (ya1 - y1) * (ya1 - y1)
                - (result[0] - r1) * (result[0] - r1)) < 0.5) {
            verified_result = result[0];
        } else if (Math.abs((xa1 - x1) * (xa1 - x1) + (ya1 - y1) * (ya1 - y1)
                - (result[1] - r1) * (result[1] - r1)) < 0.5) {
            verified_result = result[1];
        } else if (Math.abs((xa2 - x1) * (xa2 - x1) + (ya2 - y1) * (ya2 - y1)
                - (result[0] - r1) * (result[0] - r1)) < 0.5) {
            verified_result = result[0];
        } else if (Math.abs((xa2 - x1) * (xa2 - x1) + (ya2 - y1) * (ya2 - y1)
                - (result[1] - r1) * (result[1] - r1)) < 0.5) {
            verified_result = result[1];
        } else {
            //!!!!myErrLog.log("Warning: tangent circle not found");
        }

        float x = k + l * verified_result;
        float y = m + n * verified_result;

        float corrected_radius = (float) Math.sqrt((x - x1) * (x - x1)
                + (y - y1) * (y - y1)) - r1;
        if (corrected_radius < 0.f) {
            corrected_radius = 0.f;
        }
        return new Circle(x, y, corrected_radius);
    }

    public static float dist(Circle c1, Circle c2) {
        return (float) Math.sqrt((c1.getX() - c2.getX()) * (c1.getX() - c2.getX())
                + (c1.getY() - c2.getY()) * (c1.getY() - c2.getY()));
    }

    public static float getIntersection(Circle c1, Circle c2) {
        return (c1.getRadius() + c2.getRadius()) - dist(c1, c2);
    }

    public static boolean intersect(Circle c1, Circle c2) {
        return getIntersection(c1, c2) > GeometryConstants.EPS;
    }

    public float getX() {
        return m_pPos[0];
    }

    public float getY() {
        return m_pPos[1];
    }

    public float getRadius() {
        return m_pPos[2];
    }

    @Override
    public String toString() {
        return getX() + " " + getY() + " " + getRadius();
    }
}
