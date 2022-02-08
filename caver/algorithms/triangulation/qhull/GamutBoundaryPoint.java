package algorithms.triangulation.qhull;

import java.util.ArrayList;
import javax.vecmath.Vector3f;

public class GamutBoundaryPoint {

    protected static final ArrayList gbps = new ArrayList();
    public float y;
    public float chroma;

    public static float distanseAlongLine(float a0, float a1, float b0, float b1, float p0, float p1, float r0, float r1) {
        float denom = -r1 * b0 + r1 * a0 + r0 * b1 - r0 * a1;

        if (denom == 0.0F) {
            return -1.0F;
        }
        return (r1 * a0 - r1 * p0 - r0 * a1 + r0 * p1) / denom;
    }

    public Vector3f getVector(float hue, float[] scale) {
        Vector3f v3f = new Vector3f();

        v3f.y = (this.y / scale[1]);
        v3f.x = (0.5F + (float) Math.sin(hue) * this.chroma / scale[0]);
        v3f.z = (0.5F + (float) Math.cos(hue) * this.chroma / scale[2]);
        return v3f;
    }

    public static float getCusp(ArrayList al) {
        float largestChroma = 0.0F;
        float lightness = 50.0F;

        for (int i = 0; i < al.size(); i++) {
            GamutBoundaryPoint gbp = (GamutBoundaryPoint) al.get(i);

            if (gbp.chroma > largestChroma) {
                largestChroma = gbp.chroma;
                lightness = gbp.y;
            }
        }
        return lightness;
    }

    public static GamutBoundaryPoint getCuspPoint(ArrayList al) {
        float largestChroma = 0.0F;
        int index = 0;

        for (int i = 0; i < al.size(); i++) {
            GamutBoundaryPoint gbp = (GamutBoundaryPoint) al.get(i);

            if (gbp.chroma > largestChroma) {
                largestChroma = gbp.chroma;
                index = i;
            }
        }
        return (GamutBoundaryPoint) al.get(index);
    }

    public static GamutBoundaryPoint getClosest(GamutBoundaryPoint a, GamutBoundaryPoint b, GamutBoundaryPoint p) {
        float cx = p.chroma - a.chroma;
        float cy = p.y - a.y;
        float vx = b.chroma - a.chroma;
        float vy = b.y - a.y;
        float d = (float) Math.sqrt(vx * vx + vy * vy);

        vx /= d;
        vy /= d;
        float t = vx * cx + vy * cy;

        if (t <= 0.0F) {
            return a;
        }
        if (t >= d) {
            return b;
        }
        GamutBoundaryPoint gbp = getGBP();

        a.chroma += t * vx;
        a.y += t * vy;
        return gbp;
    }

    public static GamutBoundaryPoint getGBP() {
        int last = gbps.size() - 1;

        if (last >= 0) {
            return (GamutBoundaryPoint) gbps.remove(last);
        }
        return new GamutBoundaryPoint();
    }

    public static void release(GamutBoundaryPoint gbp) {
        gbps.add(gbp);
    }

    public static void release(ArrayList al) {
        gbps.addAll(al);
    }
}

