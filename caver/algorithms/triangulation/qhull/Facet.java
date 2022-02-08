package algorithms.triangulation.qhull;

public class Facet {

    public int index;
    boolean discarded;
    int lastVisit;
    public int data;
    public Vertex[] corner;
    public AlphaTriangle[] triangle;
    public Facet[] neighbor;
    OptimizedVector associatedVertices;
    public double distance;
    public double normalX;
    public double normalY;
    public double normalZ;
    public double normalW;
    public double radiusOfSmallestCircumsphere;
    public static int seq;
    static double d10;
    static double d11;
    static double d12;
    static double d13;
    static double d20;
    static double d21;
    static double d22;
    static double len;

    public Facet(Vertex a, Vertex b, Vertex c) {
        this.corner = new Vertex[3];
        this.neighbor = new Facet[3];
        this.corner[0] = a;
        this.corner[1] = b;
        this.corner[2] = c;
        d10 = b.coordinates[0] - a.coordinates[0];
        d11 = b.coordinates[1] - a.coordinates[1];
        d12 = b.coordinates[2] - a.coordinates[2];
        d20 = c.coordinates[0] - a.coordinates[0];
        d21 = c.coordinates[1] - a.coordinates[1];
        d22 = c.coordinates[2] - a.coordinates[2];
        this.normalX = (d11 * d22 - d12 * d21);
        this.normalY = (d12 * d20 - d10 * d22);
        this.normalZ = (d10 * d21 - d11 * d20);
        len = Math.sqrt(this.normalX * this.normalX + this.normalY * this.normalY + this.normalZ * this.normalZ);
        this.normalX /= len;
        this.normalY /= len;
        this.normalZ /= len;
        this.distance = (this.normalX * a.coordinates[0] + this.normalY * a.coordinates[1] + this.normalZ * a.coordinates[2]);

        this.associatedVertices = new OptimizedVector(10);
    }

    public Facet(Vertex a, Vertex b, Vertex c, boolean dummy) {
        this.corner = new Vertex[3];
        this.corner[0] = a;
        this.corner[1] = b;
        this.corner[2] = c;
    }

    public Facet(Vertex a, Vertex b, Vertex c, Vertex d) {
        this.corner = new Vertex[4];
        this.triangle = new AlphaTriangle[4];
        this.neighbor = new Facet[4];
        this.corner[0] = a;
        this.corner[1] = b;
        this.corner[2] = c;
        this.corner[3] = d;
        this.associatedVertices = new OptimizedVector(10);

        double x1 = a.coordinates[0];
        double x2 = b.coordinates[0];
        double x3 = c.coordinates[0];
        double x4 = d.coordinates[0];
        double y1 = a.coordinates[1];
        double y2 = b.coordinates[1];
        double y3 = c.coordinates[1];
        double y4 = d.coordinates[1];
        double z1 = a.coordinates[2];
        double z2 = b.coordinates[2];
        double z3 = c.coordinates[2];
        double z4 = d.coordinates[2];
        double w1 = a.coordinates[3];
        double w2 = b.coordinates[3];
        double w3 = c.coordinates[3];
        double w4 = d.coordinates[3];

        double det = x1 * y2 * z3 * w4 - x1 * y2 * w3 * z4 - x1 * y3 * z2 * w4 + x1 * y3 * w2 * z4 + x1 * y4 * z2 * w3 - x1 * y4 * w2 * z3 - x2 * y1 * z3 * w4 + x2 * y1 * w3 * z4 + x2 * y3 * z1 * w4 - x2 * y3 * w1 * z4 - x2 * y4 * z1 * w3 + x2 * y4 * w1 * z3 + x3 * y1 * z2 * w4 - x3 * y1 * w2 * z4 - x3 * y2 * z1 * w4 + x3 * y2 * w1 * z4 + x3 * y4 * z1 * w2 - x3 * y4 * w1 * z2 - x4 * y1 * z2 * w3 + x4 * y1 * w2 * z3 + x4 * y2 * z1 * w3 - x4 * y2 * w1 * z3 - x4 * y3 * z1 * w2 + x4 * y3 * w1 * z2;

        double denoter = z4 * x3 * y2 * w1 - y3 * z4 * x2 * w1 + z4 * y1 * x2 * w3 + x4 * z1 * y2 * w3 + y4 * z3 * x2 * w1 + x3 * y4 * z1 * w2 - z4 * x3 * y1 * w2 + y1 * x4 * z3 * w2 - x2 * y1 * z3 * w4 + z2 * x3 * y1 * w4 - z2 * x3 * y4 * w1 - z2 * y1 * x4 * w3 - y2 * x3 * z1 * w4 - x2 * z1 * y4 * w3 - y3 * x4 * z1 * w2 - y2 * z3 * x4 * w1 + y3 * x2 * z1 * w4 + y3 * z2 * x4 * w1 + x1 * y3 * z4 * w2 - x1 * y3 * z2 * w4 + x1 * z2 * y4 * w3 - x1 * z4 * y2 * w3 - x1 * y4 * z3 * w2 + x1 * y2 * z3 * w4;

        d10 = -(-z4 * y1 * w3 + y1 * z3 * w4 + y3 * z2 * w4 - y3 * z2 * w1 - y3 * z1 * w4 + y3 * z1 * w2 - y3 * z4 * w2 - y1 * z3 * w2 + z2 * y1 * w3 - z2 * y4 * w3 + z4 * y2 * w3 - z1 * y2 * w3 - y4 * z3 * w1 + y4 * z3 * w2 - y2 * z3 * w4 + y2 * z3 * w1 + z1 * y4 * w3 + y3 * z4 * w1 - z4 * y2 * w1 - y4 * z1 * w2 + z4 * y1 * w2 - z2 * y1 * w4 + z2 * y4 * w1 + y2 * z1 * w4) / denoter;

        d11 = (-x1 * z4 * w3 - x1 * z3 * w2 + x1 * z3 * w4 + x1 * z2 * w3 - x1 * z2 * w4 + x1 * z4 * w2 - z3 * x4 * w1 + z2 * x3 * w4 - z2 * x3 * w1 - x3 * z1 * w4 + z3 * x2 * w1 + x3 * z1 * w2 + x4 * z3 * w2 - z4 * x3 * w2 + x4 * z1 * w3 - z2 * x4 * w3 + z4 * x2 * w3 - x2 * z3 * w4 + z4 * x3 * w1 - x2 * z1 * w3 - z4 * x2 * w1 - x4 * z1 * w2 + x2 * z1 * w4 + z2 * x4 * w1) / denoter;

        d12 = -(x1 * y3 * w4 - x1 * y3 * w2 + x1 * y4 * w2 + x1 * y2 * w3 - x1 * y2 * w4 - y4 * x1 * w3 + y3 * x4 * w2 - y3 * x2 * w4 - x4 * y3 * w1 + y3 * x2 * w1 - y4 * x2 * w1 - x3 * y2 * w1 - y1 * x4 * w2 + y1 * x4 * w3 - y1 * x2 * w3 + y2 * x3 * w4 + y2 * x4 * w1 + x2 * y1 * w4 + x3 * y1 * w2 + x3 * y4 * w1 - x3 * y4 * w2 - x4 * y2 * w3 - x3 * y1 * w4 + x2 * y4 * w3) / denoter;

        d13 = (y1 * x4 * z3 - y4 * x1 * z3 - x4 * y3 * z1 + x1 * y3 * z4 - x3 * y1 * z4 - x1 * y3 * z2 + x1 * y2 * z3 - x2 * z1 * y4 - y2 * x3 * z1 - z2 * y1 * x4 + x4 * z1 * y2 + y4 * z3 * x2 + z4 * y2 * x3 - z2 * x3 * y4 - y2 * z3 * x4 + z4 * x2 * y1 + y3 * x2 * z1 + x1 * z2 * y4 - x1 * z4 * y2 - y3 * z4 * x2 + y3 * z2 * x4 - x2 * y1 * z3 + x3 * y4 * z1 + x3 * y1 * z2) / denoter;

        len = Math.sqrt(d10 * d10 + d11 * d11 + d12 * d12 + d13 * d13);
        this.normalX = (d10 / len);
        this.normalY = (d11 / len);
        this.normalZ = (d12 / len);
        this.normalW = (d13 / len);
        if (det < 0.0D) {
            this.normalX = (-this.normalX);
            this.normalY = (-this.normalY);
            this.normalZ = (-this.normalZ);
            this.normalW = (-this.normalW);
        }
        this.distance = (this.normalX * x1 + this.normalY * y1 + this.normalZ * z1 + this.normalW * w1);
    }

    public Facet(Vertex a, Vertex b, Vertex c, Vertex d, Vertex isntOutside) {
        this.corner = new Vertex[4];
        this.triangle = new AlphaTriangle[4];
        this.neighbor = new Facet[4];
        this.corner[0] = a;
        this.corner[1] = b;
        this.corner[2] = c;
        this.corner[3] = d;
        this.associatedVertices = new OptimizedVector(10);

        double x1 = a.coordinates[0];
        double x2 = b.coordinates[0];
        double x3 = c.coordinates[0];
        double x4 = d.coordinates[0];
        double y1 = a.coordinates[1];
        double y2 = b.coordinates[1];
        double y3 = c.coordinates[1];
        double y4 = d.coordinates[1];
        double z1 = a.coordinates[2];
        double z2 = b.coordinates[2];
        double z3 = c.coordinates[2];
        double z4 = d.coordinates[2];
        double w1 = a.coordinates[3];
        double w2 = b.coordinates[3];
        double w3 = c.coordinates[3];
        double w4 = d.coordinates[3];

        double denoter = z4 * x3 * y2 * w1 - y3 * z4 * x2 * w1 + z4 * y1 * x2 * w3 + x4 * z1 * y2 * w3 + y4 * z3 * x2 * w1 + x3 * y4 * z1 * w2 - z4 * x3 * y1 * w2 + y1 * x4 * z3 * w2 - x2 * y1 * z3 * w4 + z2 * x3 * y1 * w4 - z2 * x3 * y4 * w1 - z2 * y1 * x4 * w3 - y2 * x3 * z1 * w4 - x2 * z1 * y4 * w3 - y3 * x4 * z1 * w2 - y2 * z3 * x4 * w1 + y3 * x2 * z1 * w4 + y3 * z2 * x4 * w1 + x1 * y3 * z4 * w2 - x1 * y3 * z2 * w4 + x1 * z2 * y4 * w3 - x1 * z4 * y2 * w3 - x1 * y4 * z3 * w2 + x1 * y2 * z3 * w4;

        d10 = -(-z4 * y1 * w3 + y1 * z3 * w4 + y3 * z2 * w4 - y3 * z2 * w1 - y3 * z1 * w4 + y3 * z1 * w2 - y3 * z4 * w2 - y1 * z3 * w2 + z2 * y1 * w3 - z2 * y4 * w3 + z4 * y2 * w3 - z1 * y2 * w3 - y4 * z3 * w1 + y4 * z3 * w2 - y2 * z3 * w4 + y2 * z3 * w1 + z1 * y4 * w3 + y3 * z4 * w1 - z4 * y2 * w1 - y4 * z1 * w2 + z4 * y1 * w2 - z2 * y1 * w4 + z2 * y4 * w1 + y2 * z1 * w4) / denoter;

        d11 = (-x1 * z4 * w3 - x1 * z3 * w2 + x1 * z3 * w4 + x1 * z2 * w3 - x1 * z2 * w4 + x1 * z4 * w2 - z3 * x4 * w1 + z2 * x3 * w4 - z2 * x3 * w1 - x3 * z1 * w4 + z3 * x2 * w1 + x3 * z1 * w2 + x4 * z3 * w2 - z4 * x3 * w2 + x4 * z1 * w3 - z2 * x4 * w3 + z4 * x2 * w3 - x2 * z3 * w4 + z4 * x3 * w1 - x2 * z1 * w3 - z4 * x2 * w1 - x4 * z1 * w2 + x2 * z1 * w4 + z2 * x4 * w1) / denoter;

        d12 = -(x1 * y3 * w4 - x1 * y3 * w2 + x1 * y4 * w2 + x1 * y2 * w3 - x1 * y2 * w4 - y4 * x1 * w3 + y3 * x4 * w2 - y3 * x2 * w4 - x4 * y3 * w1 + y3 * x2 * w1 - y4 * x2 * w1 - x3 * y2 * w1 - y1 * x4 * w2 + y1 * x4 * w3 - y1 * x2 * w3 + y2 * x3 * w4 + y2 * x4 * w1 + x2 * y1 * w4 + x3 * y1 * w2 + x3 * y4 * w1 - x3 * y4 * w2 - x4 * y2 * w3 - x3 * y1 * w4 + x2 * y4 * w3) / denoter;

        d13 = (y1 * x4 * z3 - y4 * x1 * z3 - x4 * y3 * z1 + x1 * y3 * z4 - x3 * y1 * z4 - x1 * y3 * z2 + x1 * y2 * z3 - x2 * z1 * y4 - y2 * x3 * z1 - z2 * y1 * x4 + x4 * z1 * y2 + y4 * z3 * x2 + z4 * y2 * x3 - z2 * x3 * y4 - y2 * z3 * x4 + z4 * x2 * y1 + y3 * x2 * z1 + x1 * z2 * y4 - x1 * z4 * y2 - y3 * z4 * x2 + y3 * z2 * x4 - x2 * y1 * z3 + x3 * y4 * z1 + x3 * y1 * z2) / denoter;

        len = Math.sqrt(d10 * d10 + d11 * d11 + d12 * d12 + d13 * d13);
        this.normalX = (d10 / len);
        this.normalY = (d11 / len);
        this.normalZ = (d12 / len);
        this.normalW = (d13 / len);
        this.distance = (this.normalX * x1 + this.normalY * y1 + this.normalZ * z1 + this.normalW * w1);

        if (this.normalX * isntOutside.coordinates[0] + this.normalY * isntOutside.coordinates[1] + this.normalZ * isntOutside.coordinates[2] + this.normalW * isntOutside.coordinates[3] > this.distance) {
            this.normalX = (-this.normalX);
            this.normalY = (-this.normalY);
            this.normalZ = (-this.normalZ);
            this.normalW = (-this.normalW);
            this.distance = (-this.distance);
            Vertex temp = this.corner[1];

            this.corner[1] = this.corner[0];
            this.corner[0] = temp;
        }
    }

    public boolean add(Vertex p) {
        if (this.normalX * p.coordinates[0] + this.normalY * p.coordinates[1] + this.normalZ * p.coordinates[2] > this.distance) {
            this.associatedVertices.addElement(p);
            return true;
        }
        return false;
    }

    public boolean add4D(Vertex p) {
        if (this.normalX * p.coordinates[0] + this.normalY * p.coordinates[1] + this.normalZ * p.coordinates[2] + this.normalW * p.coordinates[3] > this.distance) {
            this.associatedVertices.addElement(p);
            return true;
        }
        return false;
    }

    public Vertex extreme() {
        Vertex res = null;
        double maxd = (-1.0D / 0.0D);

        for (int i = 0; i < this.associatedVertices.elementCount; i++) {
            Vertex p = (Vertex) this.associatedVertices.elementData[i];
            double d = this.normalX * p.coordinates[0] + this.normalY * p.coordinates[1] + this.normalZ * p.coordinates[2];

            if (d > maxd) {
                res = (Vertex) this.associatedVertices.elementData[i];
                maxd = d;
            }
        }
        return res;
    }

    public Vertex extreme4D() {
        Vertex res = null;
        double maxd = (-1.0D / 0.0D);

        for (int i = 0; i < this.associatedVertices.elementCount; i++) {
            Vertex p = (Vertex) this.associatedVertices.elementData[i];
            double d = this.normalX * p.coordinates[0] + this.normalY * p.coordinates[1] + this.normalZ * p.coordinates[2] + this.normalW * p.coordinates[3];

            if (d > maxd) {
                res = (Vertex) this.associatedVertices.elementData[i];
                maxd = d;
            }
        }
        return res;
    }

    public boolean outside(Vertex x) {
        return this.normalX * x.coordinates[0] + this.normalY * x.coordinates[1] + this.normalZ * x.coordinates[2] > this.distance;
    }

    public boolean outside4D(Vertex x) {
        return this.normalX * x.coordinates[0] + this.normalY * x.coordinates[1] + this.normalZ * x.coordinates[2] + this.normalW * x.coordinates[3] > this.distance;
    }

    public boolean isDiscarded() {
        return this.discarded;
    }

    public void discard() {
        this.discarded = true;
    }

    public Vertex[] getVertices() {
        return this.corner;
    }

    public void calculateRadiusOfSmallestCircumsphere() {
        double pi1 = this.corner[0].coordinates[0];
        double pi2 = this.corner[0].coordinates[1];
        double pi3 = this.corner[0].coordinates[2];
        double pi4 = this.corner[0].coordinates[3];
        double pj1 = this.corner[1].coordinates[0];
        double pj2 = this.corner[1].coordinates[1];
        double pj3 = this.corner[1].coordinates[2];
        double pj4 = this.corner[1].coordinates[3];
        double pk1 = this.corner[2].coordinates[0];
        double pk2 = this.corner[2].coordinates[1];
        double pk3 = this.corner[2].coordinates[2];
        double pk4 = this.corner[2].coordinates[3];
        double pu1 = this.corner[3].coordinates[0];
        double pu2 = this.corner[3].coordinates[1];
        double pu3 = this.corner[3].coordinates[2];
        double pu4 = this.corner[3].coordinates[3];

        double a1 = pi2 * pj3 * pk4 - pi2 * pj3 * pu4 - pi2 * pk3 * pj4 + pi2 * pk3 * pu4 + pi2 * pu3 * pj4 - pi2 * pu3 * pk4 - pj2 * pi3 * pk4 + pj2 * pi3 * pu4 + pj2 * pk3 * pi4 - pj2 * pk3 * pu4 - pj2 * pu3 * pi4 + pj2 * pu3 * pk4 + pk2 * pi3 * pj4 - pk2 * pi3 * pu4 - pk2 * pj3 * pi4 + pk2 * pj3 * pu4 + pk2 * pu3 * pi4 - pk2 * pu3 * pj4 - pu2 * pi3 * pj4 + pu2 * pi3 * pk4 + pu2 * pj3 * pi4 - pu2 * pj3 * pk4 - pu2 * pk3 * pi4 + pu2 * pk3 * pj4;

        double a2 = pi1 * pj3 * pk4 - pi1 * pj3 * pu4 - pi1 * pk3 * pj4 + pi1 * pk3 * pu4 + pi1 * pu3 * pj4 - pi1 * pu3 * pk4 - pj1 * pi3 * pk4 + pj1 * pi3 * pu4 + pj1 * pk3 * pi4 - pj1 * pk3 * pu4 - pj1 * pu3 * pi4 + pj1 * pu3 * pk4 + pk1 * pi3 * pj4 - pk1 * pi3 * pu4 - pk1 * pj3 * pi4 + pk1 * pj3 * pu4 + pk1 * pu3 * pi4 - pk1 * pu3 * pj4 - pu1 * pi3 * pj4 + pu1 * pi3 * pk4 + pu1 * pj3 * pi4 - pu1 * pj3 * pk4 - pu1 * pk3 * pi4 + pu1 * pk3 * pj4;

        double a3 = pi1 * pj2 * pk4 - pi1 * pj2 * pu4 - pi1 * pk2 * pj4 + pi1 * pk2 * pu4 + pi1 * pu2 * pj4 - pi1 * pu2 * pk4 - pj1 * pi2 * pk4 + pj1 * pi2 * pu4 + pj1 * pk2 * pi4 - pj1 * pk2 * pu4 - pj1 * pu2 * pi4 + pj1 * pu2 * pk4 + pk1 * pi2 * pj4 - pk1 * pi2 * pu4 - pk1 * pj2 * pi4 + pk1 * pj2 * pu4 + pk1 * pu2 * pi4 - pk1 * pu2 * pj4 - pu1 * pi2 * pj4 + pu1 * pi2 * pk4 + pu1 * pj2 * pi4 - pu1 * pj2 * pk4 - pu1 * pk2 * pi4 + pu1 * pk2 * pj4;

        double a4 = pi1 * pj2 * pk3 - pi1 * pj2 * pu3 - pi1 * pk2 * pj3 + pi1 * pk2 * pu3 + pi1 * pu2 * pj3 - pi1 * pu2 * pk3 - pj1 * pi2 * pk3 + pj1 * pi2 * pu3 + pj1 * pk2 * pi3 - pj1 * pk2 * pu3 - pj1 * pu2 * pi3 + pj1 * pu2 * pk3 + pk1 * pi2 * pj3 - pk1 * pi2 * pu3 - pk1 * pj2 * pi3 + pk1 * pj2 * pu3 + pk1 * pu2 * pi3 - pk1 * pu2 * pj3 - pu1 * pi2 * pj3 + pu1 * pi2 * pk3 + pu1 * pj2 * pi3 - pu1 * pj2 * pk3 - pu1 * pk2 * pi3 + pu1 * pk2 * pj3;

        double a5 = pi1 * pj2 * pk3 * pu4 - pi1 * pj2 * pu3 * pk4 - pi1 * pk2 * pj3 * pu4 + pi1 * pk2 * pu3 * pj4 + pi1 * pu2 * pj3 * pk4 - pi1 * pu2 * pk3 * pj4 - pj1 * pi2 * pk3 * pu4 + pj1 * pi2 * pu3 * pk4 + pj1 * pk2 * pi3 * pu4 - pj1 * pk2 * pu3 * pi4 - pj1 * pu2 * pi3 * pk4 + pj1 * pu2 * pk3 * pi4 + pk1 * pi2 * pj3 * pu4 - pk1 * pi2 * pu3 * pj4 - pk1 * pj2 * pi3 * pu4 + pk1 * pj2 * pu3 * pi4 + pk1 * pu2 * pi3 * pj4 - pk1 * pu2 * pj3 * pi4 - pu1 * pi2 * pj3 * pk4 + pu1 * pi2 * pk3 * pj4 + pu1 * pj2 * pi3 * pk4 - pu1 * pj2 * pk3 * pi4 - pu1 * pk2 * pi3 * pj4 + pu1 * pk2 * pj3 * pi4;

        this.radiusOfSmallestCircumsphere = ((a1 * a1 + a2 * a2 + a3 * a3 + 4.0D * a4 * a5) / (4.0D * a4 * a4));
    }

    public Facet getClosestTetrahedron(Vertex v, int sequence) {
        if (this.lastVisit == sequence) {
            return this;
        }
        this.lastVisit = sequence;

        float px = (float) v.coordinates[0];
        float py = (float) v.coordinates[1];
        float pz = (float) v.coordinates[2];

        float c0c0 = (float) this.corner[0].coordinates[0];
        float c0c1 = (float) this.corner[0].coordinates[1];
        float c0c2 = (float) this.corner[0].coordinates[2];

        float c1c0 = (float) this.corner[1].coordinates[0];
        float c1c1 = (float) this.corner[1].coordinates[1];
        float c1c2 = (float) this.corner[1].coordinates[2];

        float c2c0 = (float) this.corner[2].coordinates[0];
        float c2c1 = (float) this.corner[2].coordinates[1];
        float c2c2 = (float) this.corner[2].coordinates[2];

        float c3c0 = (float) this.corner[3].coordinates[0];
        float c3c1 = (float) this.corner[3].coordinates[1];
        float c3c2 = (float) this.corner[3].coordinates[2];

        Plane plane = new Plane();

        plane.set(c0c0, c0c1, c0c2, c1c0, c1c1, c1c2, c2c0, c2c1, c2c2);

        if (plane.classifyPoint2(c3c0, c3c1, c3c2) != plane.classifyPoint2(px, py, pz)) {
            if (this.neighbor[0] == null) {
                return this;
            }
            return this.neighbor[0].getClosestTetrahedron(v, sequence);
        }
        plane.set(c1c0, c1c1, c1c2, c2c0, c2c1, c2c2, c3c0, c3c1, c3c2);

        if (plane.classifyPoint(c0c0, c0c1, c0c2) != plane.classifyPoint2(px, py, pz)) {
            if (this.neighbor[1] == null) {
                return this;
            }
            return this.neighbor[1].getClosestTetrahedron(v, sequence);
        }
        plane.set(c2c0, c2c1, c2c2, c3c0, c3c1, c3c2, c0c0, c0c1, c0c2);

        if (plane.classifyPoint(c1c0, c1c1, c1c2) != plane.classifyPoint2(px, py, pz)) {
            if (this.neighbor[2] == null) {
                return this;
            }
            return this.neighbor[2].getClosestTetrahedron(v, sequence);
        }
        plane.set(c3c0, c3c1, c3c2, c0c0, c0c1, c0c2, c1c0, c1c1, c1c2);

        if (plane.classifyPoint(c2c0, c2c1, c2c2) != plane.classifyPoint2(px, py, pz)) {
            if (this.neighbor[3] == null) {
                return this;
            }
            return this.neighbor[3].getClosestTetrahedron(v, sequence);
        }

        return this;
    }

    public boolean contains(Vertex v) {
        boolean isInside = true;

        float px = (float) v.coordinates[0];
        float py = (float) v.coordinates[1];
        float pz = (float) v.coordinates[2];

        float c0c0 = (float) this.corner[0].coordinates[0];
        float c0c1 = (float) this.corner[0].coordinates[1];
        float c0c2 = (float) this.corner[0].coordinates[2];

        float c1c0 = (float) this.corner[1].coordinates[0];
        float c1c1 = (float) this.corner[1].coordinates[1];
        float c1c2 = (float) this.corner[1].coordinates[2];

        float c2c0 = (float) this.corner[2].coordinates[0];
        float c2c1 = (float) this.corner[2].coordinates[1];
        float c2c2 = (float) this.corner[2].coordinates[2];

        float c3c0 = (float) this.corner[3].coordinates[0];
        float c3c1 = (float) this.corner[3].coordinates[1];
        float c3c2 = (float) this.corner[3].coordinates[2];

        Plane plane = new Plane();

        plane.set(c0c0, c0c1, c0c2, c1c0, c1c1, c1c2, c2c0, c2c1, c2c2);

        if (plane.classifyPoint2(c3c0, c3c1, c3c2) != plane.classifyPoint2(px, py, pz)) {
            isInside = false;
        }
        plane.set(c1c0, c1c1, c1c2, c2c0, c2c1, c2c2, c3c0, c3c1, c3c2);

        if (plane.classifyPoint(c0c0, c0c1, c0c2) != plane.classifyPoint2(px, py, pz)) {
            isInside = false;
        }
        plane.set(c2c0, c2c1, c2c2, c3c0, c3c1, c3c2, c0c0, c0c1, c0c2);

        if (plane.classifyPoint(c1c0, c1c1, c1c2) != plane.classifyPoint2(px, py, pz)) {
            isInside = false;
        }
        plane.set(c3c0, c3c1, c3c2, c0c0, c0c1, c0c2, c1c0, c1c1, c1c2);

        if (plane.classifyPoint(c2c0, c2c1, c2c2) != plane.classifyPoint2(px, py, pz)) {
            isInside = false;
        }
        return isInside;
    }

    public boolean vertexInTriangle(Vertex v) {
        float delta = 1.0E-005F;

        float max = (float) Math.abs(this.normalX);
        int i = 1;
        int j = 2;
        float a = (float) Math.abs(this.normalY);
        if (a > max) {
            max = a;
            i = 0;
        }
        a = (float) Math.abs(this.normalZ);
        if (a > max) {
            max = a;
            i = 0;
            j = 1;
        }
        float u0 = (float) (v.coordinates[i] - this.corner[0].coordinates[i]);
        float v0 = (float) (v.coordinates[j] - this.corner[0].coordinates[j]);
        float u1 = (float) (this.corner[1].coordinates[i] - this.corner[0].coordinates[i]);
        float v1 = (float) (this.corner[1].coordinates[j] - this.corner[0].coordinates[j]);
        float u2 = (float) (this.corner[2].coordinates[i] - this.corner[0].coordinates[i]);
        float v2 = (float) (this.corner[2].coordinates[j] - this.corner[0].coordinates[j]);

        if ((u1 > -1.0E-005F) && (u1 < 1.0E-005F)) {
            float b = u0 / u2;
            if ((0.0F <= b) && (b <= 1.0F)) {
                a = (v0 - b * v2) / v1;
                if ((a >= 0.0F) && (a + b <= 1.0F)) {
                    return true;
                }
            }
        } else {
            float b = (v0 * u1 - u0 * v1) / (v2 * u1 - u2 * v1);
            if ((0.0F <= b) && (b <= 1.0F)) {
                a = (u0 - b * u2) / u1;
                if ((a >= 0.0F) && (a + b <= 1.0F)) {
                    return true;
                }
            }
        }
        return false;
    }
}
