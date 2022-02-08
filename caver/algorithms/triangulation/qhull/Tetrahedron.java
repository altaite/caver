package algorithms.triangulation.qhull;

import java.util.ArrayList;
import javax.vecmath.Vector3f;

public class Tetrahedron {

    public Plane[] plane;
    private Tetrahedron[] neighbor;
    private PickableObject[] cornerObj;
    public float cornerpos0_0;
    public float cornerpos0_1;
    public float cornerpos0_2;
    public float cornerpos1_0;
    public float cornerpos1_1;
    public float cornerpos1_2;
    public float cornerpos2_0;
    public float cornerpos2_1;
    public float cornerpos2_2;
    public float cornerpos3_0;
    public float cornerpos3_1;
    public float cornerpos3_2;
    private boolean searched;
    private int indicesUsed;
    private float[] position;
    private int[] colorIndices;
    private int totalIndices;
    private float[][] weight;
    private float myVolume;
    private static final Vector3f cVector = new Vector3f();

    public Tetrahedron(PickableObject p0, PickableObject p1, PickableObject p2,
            PickableObject p3) {
        this.cornerObj = new PickableObject[4];
        this.plane = new Plane[4];
        this.neighbor = new Tetrahedron[4];
        this.cornerObj[0] = p0;
        float[] f = p0.getPosition();

        this.cornerpos0_0 = f[0];
        this.cornerpos0_1 = f[1];
        this.cornerpos0_2 = f[2];
        this.cornerObj[1] = p1;
        f = p1.getPosition();
        this.cornerpos1_0 = f[0];
        this.cornerpos1_1 = f[1];
        this.cornerpos1_2 = f[2];
        this.cornerObj[2] = p2;
        f = p2.getPosition();
        this.cornerpos2_0 = f[0];
        this.cornerpos2_1 = f[1];
        this.cornerpos2_2 = f[2];
        this.cornerObj[3] = p3;
        f = p3.getPosition();
        this.cornerpos3_0 = f[0];
        this.cornerpos3_1 = f[1];
        this.cornerpos3_2 = f[2];

        this.myVolume = volume(this.cornerpos0_0, this.cornerpos0_1,
                this.cornerpos0_2, this.cornerpos1_0, this.cornerpos1_1,
                this.cornerpos1_2, this.cornerpos2_0, this.cornerpos2_1,
                this.cornerpos2_2, this.cornerpos3_0, this.cornerpos3_1,
                this.cornerpos3_2);

        this.plane[0] = new Plane(new Vector3f(p0.getPosition()),
                new Vector3f(p1.getPosition()), new Vector3f(p2.getPosition()));

        this.plane[1] = new Plane(new Vector3f(p0.getPosition()),
                new Vector3f(p2.getPosition()), new Vector3f(p3.getPosition()));

        this.plane[2] = new Plane(new Vector3f(p0.getPosition()),
                new Vector3f(p3.getPosition()), new Vector3f(p1.getPosition()));

        this.plane[3] = new Plane(new Vector3f(p1.getPosition()),
                new Vector3f(p3.getPosition()), new Vector3f(p2.getPosition()));
    }

    public void updatePositionFromCorners() {
        float[] f = this.cornerObj[0].getPositionReference();

        this.cornerpos0_0 = f[0];
        this.cornerpos0_1 = f[1];
        this.cornerpos0_2 = f[2];

        f = this.cornerObj[1].getPositionReference();
        this.cornerpos1_0 = f[0];
        this.cornerpos1_1 = f[1];
        this.cornerpos1_2 = f[2];
        f = this.cornerObj[2].getPositionReference();
        this.cornerpos2_0 = f[0];
        this.cornerpos2_1 = f[1];
        this.cornerpos2_2 = f[2];
        f = this.cornerObj[3].getPositionReference();
        this.cornerpos3_0 = f[0];
        this.cornerpos3_1 = f[1];
        this.cornerpos3_2 = f[2];

        this.myVolume = volume(this.cornerpos0_0, this.cornerpos0_1,
                this.cornerpos0_2, this.cornerpos1_0, this.cornerpos1_1,
                this.cornerpos1_2, this.cornerpos2_0, this.cornerpos2_1,
                this.cornerpos2_2, this.cornerpos3_0, this.cornerpos3_1,
                this.cornerpos3_2);


        this.plane[0].set(this.cornerpos0_0, this.cornerpos0_1,
                this.cornerpos0_2, this.cornerpos1_0, this.cornerpos1_1,
                this.cornerpos1_2, this.cornerpos2_0, this.cornerpos2_1,
                this.cornerpos2_2);

        this.plane[1].set(this.cornerpos0_0, this.cornerpos0_1,
                this.cornerpos0_2, this.cornerpos2_0, this.cornerpos2_1,
                this.cornerpos2_2, this.cornerpos3_0, this.cornerpos3_1,
                this.cornerpos3_2);

        this.plane[2].set(this.cornerpos0_0, this.cornerpos0_1,
                this.cornerpos0_2, this.cornerpos3_0, this.cornerpos3_1,
                this.cornerpos3_2, this.cornerpos1_0, this.cornerpos1_1,
                this.cornerpos1_2);

        this.plane[3].set(this.cornerpos1_0, this.cornerpos1_1,
                this.cornerpos1_2, this.cornerpos3_0, this.cornerpos3_1,
                this.cornerpos3_2, this.cornerpos2_0, this.cornerpos2_1,
                this.cornerpos2_2);
    }

    public void setNeighbor(int i, Tetrahedron tetra) {
        this.neighbor[i] = tetra;
    }

    public final Tetrahedron getNeighbor(int i) {
        return this.neighbor[i];
    }

    public Tetrahedron findSegmentTetrahedron(float pointx, float pointy,
            float pointz) {
        for (int i = 0; i < 4; i++) {
            if ((this.plane[i].classifyPoint(pointx, pointy, pointz) == 1)
                    && (this.neighbor[i] != null)) {
                return this.neighbor[i].findSegmentTetrahedron(pointx, pointy,
                        pointz);
            }
        }

        return this;
    }

    public Tetrahedron findSegmentTetrahedron(Tetrahedron from, float pointx,
            float pointy, float pointz) {
        for (int i = 0; i < 3; i++) {
            if ((this.neighbor[i] != from) && (this.plane[i].classifyPoint(
                    pointx, pointy, pointz) == 1)) {
                return this.neighbor[i].findSegmentTetrahedron(this, pointx,
                        pointy, pointz);
            }
        }

        return this;
    }

    public Tetrahedron findSegmentTetrahedron2(Tetrahedron from, float pointx,
            float pointy, float pointz) {
        for (int i = 0; i < 3; i++) {
            if ((this.neighbor[i] != from) && (this.plane[i].classifyPoint2(
                    pointx, pointy, pointz) == 1)) {
                return this.neighbor[i].findSegmentTetrahedron(this, pointx,
                        pointy, pointz);
            }
        }

        return this;
    }

    public boolean isDefinitelyInSegment(float x, float y, float z) {
        for (int i = 0; i < 3; i++) {
            if (this.plane[i].classifyPoint2(x, y, z) == 1) {
                return false;
            }
        }
        return true;
    }

    public boolean test(boolean fix) {
        if (this.plane[3].classifyPoint2(this.cornerpos0_0, this.cornerpos0_1,
                this.cornerpos0_2) != -1) {
            System.out.println("Tetrahedron test failed ");
            if (fix) {
                if (this.neighbor[0].isDefinitelyInSegment(this.cornerpos3_0,
                        this.cornerpos3_1, this.cornerpos3_2)) {
                    float epsilon = 1.0E-005F;

                    while (this.plane[0].classifyPoint2(this.cornerpos3_0,
                            this.cornerpos3_1, this.cornerpos3_2) != -1) {
                        this.cornerpos3_0 -= this.plane[0].normalX * epsilon;
                        this.cornerpos3_1 -= this.plane[0].normalY * epsilon;
                        this.cornerpos3_2 -= this.plane[0].normalZ * epsilon;
                        epsilon *= 2.0F;
                    }
                    float[] f = this.cornerObj[3].getPositionReference();

                    f[0] = this.cornerpos3_0;
                    f[1] = this.cornerpos3_1;
                    f[2] = this.cornerpos3_2;

                    boolean success = true;
                    try {
                        PickableTetrahedronObject pto = (PickableTetrahedronObject) this.cornerObj[3];
                        Tetrahedron[] t = pto.getTetrahedra();

                        if (t != null) {
                            for (int i = 0; i < t.length; i++) {
                                t[i].updatePositionFromCorners();
                                success &= t[i].test(false);
                            }
                        }
                    } catch (Exception e) {
                    }
                    System.out.println("Corner 3 in neighbor 0");
                    success &= test(false);
                    System.out.println("Tetrahedron fix success=" + success);
                    return success;
                }
                if (this.neighbor[1].isDefinitelyInSegment(this.cornerpos1_0,
                        this.cornerpos1_1, this.cornerpos1_2)) {
                    float epsilon = 1.0E-005F;

                    while (this.plane[1].classifyPoint2(this.cornerpos1_0,
                            this.cornerpos1_1, this.cornerpos1_2) != -1) {
                        this.cornerpos1_0 -= this.plane[1].normalX * epsilon;
                        this.cornerpos1_1 -= this.plane[1].normalY * epsilon;
                        this.cornerpos1_2 -= this.plane[1].normalZ * epsilon;
                        epsilon *= 2.0F;
                    }
                    float[] f = this.cornerObj[1].getPositionReference();

                    f[0] = this.cornerpos1_0;
                    f[1] = this.cornerpos1_1;
                    f[2] = this.cornerpos1_2;

                    boolean success = true;
                    try {
                        PickableTetrahedronObject pto =
                                (PickableTetrahedronObject) this.cornerObj[1];
                        Tetrahedron[] t = pto.getTetrahedra();

                        if (t != null) {
                            for (int i = 0; i < t.length; i++) {
                                t[i].updatePositionFromCorners();
                                success &= t[i].test(false);
                            }
                        }
                    } catch (Exception e) {
                    }
                    System.out.println("Corner 1 in neighbor 1");
                    success &= test(false);
                    System.out.println("Tetrahedron fix success=" + success);
                    return success;
                }
                if (this.neighbor[2].isDefinitelyInSegment(this.cornerpos2_0,
                        this.cornerpos2_1, this.cornerpos2_2)) {
                    float epsilon = 1.0E-005F;

                    while (this.plane[2].classifyPoint2(this.cornerpos2_0,
                            this.cornerpos2_1, this.cornerpos2_2) != -1) {
                        this.cornerpos2_0 -= this.plane[2].normalX * epsilon;
                        this.cornerpos2_1 -= this.plane[2].normalY * epsilon;
                        this.cornerpos2_2 -= this.plane[2].normalZ * epsilon;
                        epsilon *= 2.0F;
                    }
                    float[] f = this.cornerObj[2].getPositionReference();

                    f[0] = this.cornerpos2_0;
                    f[1] = this.cornerpos2_1;
                    f[2] = this.cornerpos2_2;

                    boolean success = true;
                    try {
                        PickableTetrahedronObject pto = (PickableTetrahedronObject) this.cornerObj[2];
                        Tetrahedron[] t = pto.getTetrahedra();

                        if (t != null) {
                            for (int i = 0; i < t.length; i++) {
                                t[i].updatePositionFromCorners();
                                success &= t[i].test(false);
                            }
                        }
                    } catch (Exception e) {
                    }
                    System.out.println("Corner 2 in neighbor 2");
                    success &= test(false);
                    System.out.println("Tetrahedron fix success=" + success);
                    return success;
                }
            }

            return false;
        }
        return true;
    }

    public boolean isInSegment(float pointx, float pointy, float pointz) {
        for (int i = 0; i < 3; i++) {
            if (this.plane[i].classifyPoint(pointx, pointy, pointz) == 1) {
                return false;
            }
        }

        return true;
    }

    public final boolean isOutsideSegmentPlane(
            float pointx, float pointy, float pointz) {
        return this.plane[3].classifyPoint(pointx, pointy, pointz) == 1;
    }

    public void disconnect() {
    }

    public void addColor() {
        this.totalIndices += 1;
    }

    public void finalizeColorIndices() {
        this.colorIndices = new int[this.totalIndices];
        this.weight = new float[this.totalIndices][4];
        this.position = new float[this.totalIndices * 3];
    }

    public void addColor(int addColor, float[] point) {
        this.colorIndices[this.indicesUsed] = addColor;

        float w1 = volume(this.cornerpos1_0, this.cornerpos1_1,
                this.cornerpos1_2, this.cornerpos2_0, this.cornerpos2_1,
                this.cornerpos2_2, this.cornerpos3_0, this.cornerpos3_1,
                this.cornerpos3_2, point[0], point[1], point[2]) / this.myVolume;

        float w2 = volume(this.cornerpos0_0, this.cornerpos0_1,
                this.cornerpos0_2, this.cornerpos2_0, this.cornerpos2_1,
                this.cornerpos2_2, this.cornerpos3_0, this.cornerpos3_1,
                this.cornerpos3_2, point[0], point[1], point[2]) / this.myVolume;

        float w3 = volume(this.cornerpos1_0, this.cornerpos1_1,
                this.cornerpos1_2, this.cornerpos0_0, this.cornerpos0_1,
                this.cornerpos0_2, this.cornerpos3_0, this.cornerpos3_1,
                this.cornerpos3_2, point[0], point[1], point[2]) / this.myVolume;

        float w4 = volume(this.cornerpos1_0, this.cornerpos1_1,
                this.cornerpos1_2, this.cornerpos2_0, this.cornerpos2_1,
                this.cornerpos2_2, this.cornerpos0_0, this.cornerpos0_1,
                this.cornerpos0_2, point[0], point[1], point[2]) / this.myVolume;

        this.weight[this.indicesUsed][0] = w1;
        this.weight[this.indicesUsed][1] = w2;
        this.weight[this.indicesUsed][2] = w3;
        this.weight[this.indicesUsed][3] = w4;
        this.position[(this.indicesUsed * 3)] = point[0];
        this.position[(this.indicesUsed * 3 + 1)] = point[1];
        this.position[(this.indicesUsed * 3 + 2)] = point[2];
        this.indicesUsed += 1;
    }

    public float[] getPositions() {
        return this.position;
    }

    public final float volume(float x1, float y1, float z1, float x2, float y2,
            float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        float vol = 0.1666667F * (x1 * y2 * (z3 - z4) + x1 * y3 * (z4 - z2) + x1 * y4 * (z2 - z3) + x2 * y1 * (z4 - z3) + x2 * y3 * (z1 - z4) + x2 * y4 * (z3 - z1) + x3 * y1 * (z2 - z4) + x3 * y2 * (z4 - z1) + x3 * y4 * (z1 - z2) + x4 * y1 * (z3 - z2) + x4 * y2 * (z1 - z3) + x4 * y3 * (z2 - z1));

        if (vol < 0.0F) {
            vol = -vol;
        }
        return vol;
    }

    public int[] getColorIndices() {
        return this.colorIndices;
    }

    public float[][] getWeights() {
        return this.weight;
    }

    public float[][] getCorners() {
        float[][] corny = new float[4][];

        for (int i = 0; i < 4; i++) {
            corny[i] = this.cornerObj[i].getPosition();
        }
        return corny;
    }

    public float[][] getStartCorners() {
        return new float[][]{{this.cornerpos0_0, this.cornerpos0_1, this.cornerpos0_2}, {this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2}, {this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2}, {this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2}};
    }

    public int findIntersection(Plane p, ArrayList al, Tetrahedron from, Tetrahedron start, float[] scale, boolean posDiffX, boolean posDiffZ, float hue) {
        if (start == this) {
            return 0;
        }

        int numOnInside = 0;

        int k = p.classifyPoint2(this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2);
        int l = p.classifyPoint2(this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2);
        int m = p.classifyPoint2(this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2);

        if ((k == 0) || (l == 0) || (m == 0)) {
            return 1;
        }
        if (k == -1) {
            numOnInside++;
        }
        if (l == -1) {
            numOnInside++;
        }
        if (m == -1) {
            numOnInside++;
        }

        if ((numOnInside == 0) || (numOnInside == 3)) {
            if (from == null) {
                return 4;
            }
            return 2;
        }

        if (from == null) {
            if (k != l) {
                float t = p.intersectionTimeAsWeight(this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2, this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2);

                float oneMinusT = 1.0F - t;
                float v3x = t * this.cornerpos1_0 + oneMinusT * this.cornerpos2_0;
                float v3y = t * this.cornerpos1_1 + oneMinusT * this.cornerpos2_1;
                float v3z = t * this.cornerpos1_2 + oneMinusT * this.cornerpos2_2;

                GamutBoundaryPoint gbp = GamutBoundaryPoint.getGBP();

                gbp.y = (v3y * scale[1]);
                float xDiff = (v3x - 0.5F) * scale[0];
                float zDiff = (v3z - 0.5F) * scale[2];

                gbp.chroma = (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff);
                if (Math.abs(v3x - 0.5F) > Math.abs(v3z - 0.5F)) {
                    if (v3x > 0.5F != posDiffX) {
                        gbp.chroma = (-gbp.chroma);
                    }
                } else if (v3z > 0.5F != posDiffZ) {
                    gbp.chroma = (-gbp.chroma);
                }

                al.add(gbp);
                return this.neighbor[0].findIntersection(p, al, this, this, scale, posDiffX, posDiffZ, hue);
            }

            float t = p.intersectionTimeAsWeight(this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2, this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2);

            float oneMinusT = 1.0F - t;
            float v3x = t * this.cornerpos1_0 + oneMinusT * this.cornerpos3_0;
            float v3y = t * this.cornerpos1_1 + oneMinusT * this.cornerpos3_1;
            float v3z = t * this.cornerpos1_2 + oneMinusT * this.cornerpos3_2;

            GamutBoundaryPoint gbp = GamutBoundaryPoint.getGBP();

            gbp.y = (v3y * scale[1]);
            float xDiff = (v3x - 0.5F) * scale[0];
            float zDiff = (v3z - 0.5F) * scale[2];

            gbp.chroma = (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            if (Math.abs(v3x - 0.5F) > Math.abs(v3z - 0.5F)) {
                if (v3x > 0.5F != posDiffX) {
                    gbp.chroma = (-gbp.chroma);
                }
            } else if (v3z > 0.5F != posDiffZ) {
                gbp.chroma = (-gbp.chroma);
            }

            al.add(gbp);
            return this.neighbor[2].findIntersection(p, al, this, this, scale, posDiffX, posDiffZ, hue);
        }

        if (this.neighbor[0] == from) {
            if (l != m) {
                float t = p.intersectionTimeAsWeight(this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2, this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2);

                float oneMinusT = 1.0F - t;
                float v3x = t * this.cornerpos2_0 + oneMinusT * this.cornerpos3_0;
                float v3y = t * this.cornerpos2_1 + oneMinusT * this.cornerpos3_1;
                float v3z = t * this.cornerpos2_2 + oneMinusT * this.cornerpos3_2;

                GamutBoundaryPoint gbp = GamutBoundaryPoint.getGBP();

                gbp.y = (v3y * scale[1]);
                float xDiff = (v3x - 0.5F) * scale[0];
                float zDiff = (v3z - 0.5F) * scale[2];

                gbp.chroma = (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff);
                if (Math.abs(v3x - 0.5F) > Math.abs(v3z - 0.5F)) {
                    if (v3x > 0.5F != posDiffX) {
                        gbp.chroma = (-gbp.chroma);
                    }
                } else if (v3z > 0.5F != posDiffZ) {
                    gbp.chroma = (-gbp.chroma);
                }

                al.add(gbp);
                return this.neighbor[1].findIntersection(p, al, this, start, scale, posDiffX, posDiffZ, hue);
            }

            float t = p.intersectionTimeAsWeight(this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2, this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2);

            float oneMinusT = 1.0F - t;
            float v3x = t * this.cornerpos3_0 + oneMinusT * this.cornerpos1_0;
            float v3y = t * this.cornerpos3_1 + oneMinusT * this.cornerpos1_1;
            float v3z = t * this.cornerpos3_2 + oneMinusT * this.cornerpos1_2;

            GamutBoundaryPoint gbp = GamutBoundaryPoint.getGBP();

            gbp.y = (v3y * scale[1]);
            float xDiff = (v3x - 0.5F) * scale[0];
            float zDiff = (v3z - 0.5F) * scale[2];

            gbp.chroma = (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            if (Math.abs(v3x - 0.5F) > Math.abs(v3z - 0.5F)) {
                if (v3x > 0.5F != posDiffX) {
                    gbp.chroma = (-gbp.chroma);
                }
            } else if (v3z > 0.5F != posDiffZ) {
                gbp.chroma = (-gbp.chroma);
            }

            al.add(gbp);
            return this.neighbor[2].findIntersection(p, al, this, start, scale, posDiffX, posDiffZ, hue);
        }

        if (this.neighbor[1] == from) {
            if (m != k) {
                float t = p.intersectionTimeAsWeight(this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2, this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2);

                float oneMinusT = 1.0F - t;
                float v3x = t * this.cornerpos3_0 + oneMinusT * this.cornerpos1_0;
                float v3y = t * this.cornerpos3_1 + oneMinusT * this.cornerpos1_1;
                float v3z = t * this.cornerpos3_2 + oneMinusT * this.cornerpos1_2;

                GamutBoundaryPoint gbp = GamutBoundaryPoint.getGBP();

                gbp.y = (v3y * scale[1]);
                float xDiff = (v3x - 0.5F) * scale[0];
                float zDiff = (v3z - 0.5F) * scale[2];

                gbp.chroma = (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff);
                if (Math.abs(v3x - 0.5F) > Math.abs(v3z - 0.5F)) {
                    if (v3x > 0.5F != posDiffX) {
                        gbp.chroma = (-gbp.chroma);
                    }
                } else if (v3z > 0.5F != posDiffZ) {
                    gbp.chroma = (-gbp.chroma);
                }

                al.add(gbp);
                return this.neighbor[2].findIntersection(p, al, this, start, scale, posDiffX, posDiffZ, hue);
            }

            float t = p.intersectionTimeAsWeight(this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2, this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2);

            float oneMinusT = 1.0F - t;
            float v3x = t * this.cornerpos1_0 + oneMinusT * this.cornerpos2_0;
            float v3y = t * this.cornerpos1_1 + oneMinusT * this.cornerpos2_1;
            float v3z = t * this.cornerpos1_2 + oneMinusT * this.cornerpos2_2;

            GamutBoundaryPoint gbp = GamutBoundaryPoint.getGBP();

            gbp.y = (v3y * scale[1]);
            float xDiff = (v3x - 0.5F) * scale[0];
            float zDiff = (v3z - 0.5F) * scale[2];

            gbp.chroma = (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            if (Math.abs(v3x - 0.5F) > Math.abs(v3z - 0.5F)) {
                if (v3x > 0.5F != posDiffX) {
                    gbp.chroma = (-gbp.chroma);
                }
            } else if (v3z > 0.5F != posDiffZ) {
                gbp.chroma = (-gbp.chroma);
            }

            al.add(gbp);
            return this.neighbor[0].findIntersection(p, al, this, start, scale, posDiffX, posDiffZ, hue);
        }

        if (this.neighbor[2] == from) {
            if (k != l) {
                float t = p.intersectionTimeAsWeight(this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2, this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2);

                float oneMinusT = 1.0F - t;
                float v3x = t * this.cornerpos1_0 + oneMinusT * this.cornerpos2_0;
                float v3y = t * this.cornerpos1_1 + oneMinusT * this.cornerpos2_1;
                float v3z = t * this.cornerpos1_2 + oneMinusT * this.cornerpos2_2;

                GamutBoundaryPoint gbp = GamutBoundaryPoint.getGBP();

                gbp.y = (v3y * scale[1]);
                float xDiff = (v3x - 0.5F) * scale[0];
                float zDiff = (v3z - 0.5F) * scale[2];

                gbp.chroma = (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff);
                if (Math.abs(v3x - 0.5F) > Math.abs(v3z - 0.5F)) {
                    if (v3x > 0.5F != posDiffX) {
                        gbp.chroma = (-gbp.chroma);
                    }
                } else if (v3z > 0.5F != posDiffZ) {
                    gbp.chroma = (-gbp.chroma);
                }

                al.add(gbp);
                return this.neighbor[0].findIntersection(p, al, this, start, scale, posDiffX, posDiffZ, hue);
            }

            float t = p.intersectionTimeAsWeight(this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2, this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2);

            float oneMinusT = 1.0F - t;
            float v3x = t * this.cornerpos2_0 + oneMinusT * this.cornerpos3_0;
            float v3y = t * this.cornerpos2_1 + oneMinusT * this.cornerpos3_1;
            float v3z = t * this.cornerpos2_2 + oneMinusT * this.cornerpos3_2;

            GamutBoundaryPoint gbp = GamutBoundaryPoint.getGBP();

            gbp.y = (v3y * scale[1]);
            float xDiff = (v3x - 0.5F) * scale[0];
            float zDiff = (v3z - 0.5F) * scale[2];

            gbp.chroma = (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            if (Math.abs(v3x - 0.5F) > Math.abs(v3z - 0.5F)) {
                if (v3x > 0.5F != posDiffX) {
                    gbp.chroma = (-gbp.chroma);
                }
            } else if (v3z > 0.5F != posDiffZ) {
                gbp.chroma = (-gbp.chroma);
            }

            al.add(gbp);
            return this.neighbor[1].findIntersection(p, al, this, start, scale, posDiffX, posDiffZ, hue);
        }

        return 3;
    }

    public int findIntersection(Plane p, ArrayList al, Tetrahedron from, Tetrahedron start) {
        if (start == this) {
            return 0;
        }

        int numOnInside = 0;

        int k = p.classifyPoint2(this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2);
        int l = p.classifyPoint2(this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2);
        int m = p.classifyPoint2(this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2);

        if ((k == 0) || (l == 0) || (m == 0)) {
            return 1;
        }
        if (k == -1) {
            numOnInside++;
        }
        if (l == -1) {
            numOnInside++;
        }
        if (m == -1) {
            numOnInside++;
        }

        if ((numOnInside == 0) || (numOnInside == 3)) {
            if (from == null) {
                return 4;
            }
            return 2;
        }

        if (from == null) {
            if (k != l) {
                float t = p.intersectionTimeAsWeight(this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2, this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2);

                float oneMinusT = 1.0F - t;
                float v3x = t * this.cornerpos1_0 + oneMinusT * this.cornerpos2_0;
                float v3y = t * this.cornerpos1_1 + oneMinusT * this.cornerpos2_1;
                float v3z = t * this.cornerpos1_2 + oneMinusT * this.cornerpos2_2;

                al.add(new Vector3f(v3x, v3y, v3z));
                return this.neighbor[0].findIntersection(p, al, this, this);
            }

            float t = p.intersectionTimeAsWeight(this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2, this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2);

            float oneMinusT = 1.0F - t;
            float v3x = t * this.cornerpos1_0 + oneMinusT * this.cornerpos3_0;
            float v3y = t * this.cornerpos1_1 + oneMinusT * this.cornerpos3_1;
            float v3z = t * this.cornerpos1_2 + oneMinusT * this.cornerpos3_2;

            al.add(new Vector3f(v3x, v3y, v3z));
            return this.neighbor[2].findIntersection(p, al, this, this);
        }

        if (this.neighbor[0] == from) {
            if (l != m) {
                float t = p.intersectionTimeAsWeight(this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2, this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2);

                float oneMinusT = 1.0F - t;
                float v3x = t * this.cornerpos2_0 + oneMinusT * this.cornerpos3_0;
                float v3y = t * this.cornerpos2_1 + oneMinusT * this.cornerpos3_1;
                float v3z = t * this.cornerpos2_2 + oneMinusT * this.cornerpos3_2;

                al.add(new Vector3f(v3x, v3y, v3z));
                return this.neighbor[1].findIntersection(p, al, this, start);
            }

            float t = p.intersectionTimeAsWeight(this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2, this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2);

            float oneMinusT = 1.0F - t;
            float v3x = t * this.cornerpos3_0 + oneMinusT * this.cornerpos1_0;
            float v3y = t * this.cornerpos3_1 + oneMinusT * this.cornerpos1_1;
            float v3z = t * this.cornerpos3_2 + oneMinusT * this.cornerpos1_2;

            al.add(new Vector3f(v3x, v3y, v3z));
            return this.neighbor[2].findIntersection(p, al, this, start);
        }

        if (this.neighbor[1] == from) {
            if (m != k) {
                float t = p.intersectionTimeAsWeight(this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2, this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2);

                float oneMinusT = 1.0F - t;
                float v3x = t * this.cornerpos3_0 + oneMinusT * this.cornerpos1_0;
                float v3y = t * this.cornerpos3_1 + oneMinusT * this.cornerpos1_1;
                float v3z = t * this.cornerpos3_2 + oneMinusT * this.cornerpos1_2;

                al.add(new Vector3f(v3x, v3y, v3z));
                return this.neighbor[2].findIntersection(p, al, this, start);
            }

            float t = p.intersectionTimeAsWeight(this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2, this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2);

            float oneMinusT = 1.0F - t;
            float v3x = t * this.cornerpos1_0 + oneMinusT * this.cornerpos2_0;
            float v3y = t * this.cornerpos1_1 + oneMinusT * this.cornerpos2_1;
            float v3z = t * this.cornerpos1_2 + oneMinusT * this.cornerpos2_2;

            al.add(new Vector3f(v3x, v3y, v3z));
            return this.neighbor[0].findIntersection(p, al, this, start);
        }

        if (this.neighbor[2] == from) {
            if (k != l) {
                float t = p.intersectionTimeAsWeight(this.cornerpos1_0, this.cornerpos1_1, this.cornerpos1_2, this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2);

                float oneMinusT = 1.0F - t;
                float v3x = t * this.cornerpos1_0 + oneMinusT * this.cornerpos2_0;
                float v3y = t * this.cornerpos1_1 + oneMinusT * this.cornerpos2_1;
                float v3z = t * this.cornerpos1_2 + oneMinusT * this.cornerpos2_2;

                al.add(new Vector3f(v3x, v3y, v3z));
                return this.neighbor[0].findIntersection(p, al, this, start);
            }

            float t = p.intersectionTimeAsWeight(this.cornerpos2_0, this.cornerpos2_1, this.cornerpos2_2, this.cornerpos3_0, this.cornerpos3_1, this.cornerpos3_2);

            float oneMinusT = 1.0F - t;
            float v3x = t * this.cornerpos2_0 + oneMinusT * this.cornerpos3_0;
            float v3y = t * this.cornerpos2_1 + oneMinusT * this.cornerpos3_1;
            float v3z = t * this.cornerpos2_2 + oneMinusT * this.cornerpos3_2;

            al.add(new Vector3f(v3x, v3y, v3z));
            return this.neighbor[1].findIntersection(p, al, this, start);
        }

        return 3;
    }

    public static Vector3f closestPointOnLine(Vector3f lineStart, Vector3f lineEnd, Vector3f point) {
        cVector.sub(point, lineStart);

        Vector3f v = new Vector3f();

        v.sub(lineEnd, lineStart);
        float d = v.length();

        v.x /= d;
        v.y /= d;
        v.z /= d;

        float t = v.dot(cVector);

        if (t < 0.0F) {
            return lineStart;
        }
        if (t > d) {
            return lineEnd;
        }
        v.scale(t);

        v.add(lineStart);
        return v;
    }

    public static boolean findBoundary(Plane p, Tetrahedron[] tetra, Tetrahedron startSearch, ArrayList al) {
        int res;
        do {
            res = startSearch.findIntersection(p, al, null, null);
            if (res == 0) {
                return true;
            }
            p.incDistance();
            al.clear();
        } while (res != 4);

        int c = 0;

        while ((res != 0) && (c++ < 20)) {
            for (int i = 0; i < tetra.length; i++) {
                res = tetra[i].findIntersection(p, al, null, null);
                if (res != 4) {
                    break;
                }
            }
            if (res != 0) {
                p.incDistance();
                al.clear();
            }
        }
        return res == 0;
    }

    public static boolean findBoundary(Plane p, Tetrahedron[] tetra, Tetrahedron startSearch, ArrayList al, float[] scale, boolean posDiffX, boolean posDiffZ, float hue) {
        int res;
        do {
            res = startSearch.findIntersection(p, al, null, null, scale, posDiffX, posDiffZ, hue);

            if (res == 0) {
                return true;
            }
            p.incDistance();
            al.clear();
        } while (res != 4);

        int c = 0;

        while ((res != 0) && (c++ < 20)) {
            for (int i = 0; i < tetra.length; i++) {
                res = tetra[i].findIntersection(p, al, null, null, scale, posDiffX, posDiffZ, hue);

                if (res != 4) {
                    break;
                }
            }
            if (res != 0) {
                p.incDistance();
                al.clear();
            }
        }
        return res == 0;
    }

    public static GamutBoundaryPoint findClosestIntersection(ArrayList al, float pointx, float pointy, float rayx, float rayy) {
        GamutBoundaryPoint gbp = GamutBoundaryPoint.getGBP();
        float closestIntersection = 3.4028235E+38F;

        int num = al.size();

        for (int i = 0; i < num; i++) {
            GamutBoundaryPoint a = (GamutBoundaryPoint) al.get(i);
            GamutBoundaryPoint b = (GamutBoundaryPoint) al.get((i + 1) % num);
            float t = GamutBoundaryPoint.distanseAlongLine(a.chroma, a.y, b.chroma, b.y, pointx, pointy, rayx, rayy);

            if ((t >= -1.0E-004F) && (t <= 1.0001F)) {
                float chroma = a.chroma + t * (b.chroma - a.chroma);

                if (chroma >= 0.0F) {
                    float y = a.y + t * (b.y - a.y);
                    float dist = (chroma - pointx) * (chroma - pointx) + (y - pointy) * (y - pointy);

                    if (dist < closestIntersection) {
                        closestIntersection = dist;
                        gbp.chroma = chroma;
                        gbp.y = y;
                    }

                }

            }

        }

        if (closestIntersection == 3.4028235E+38F) {
            GamutBoundaryPoint.release(gbp);
            return null;
        }
        return gbp;
    }

    public static GamutBoundaryPoint findExtremeIntersection(ArrayList al, float pointx, float pointy, float rayx, float rayy) {
        GamutBoundaryPoint gbp = GamutBoundaryPoint.getGBP();
        float closestIntersection = -1000.0F;
        boolean found = false;
        int num = al.size();

        for (int i = 0; i < num; i++) {
            GamutBoundaryPoint a = (GamutBoundaryPoint) al.get(i);
            GamutBoundaryPoint b = (GamutBoundaryPoint) al.get((i + 1) % num);
            float t = GamutBoundaryPoint.distanseAlongLine(a.chroma, a.y, b.chroma, b.y, pointx, pointy, rayx, rayy);

            if ((t >= -1.0E-004F) && (t <= 1.0001F)) {
                float chroma = a.chroma + t * (b.chroma - a.chroma);

                if (chroma >= closestIntersection) {
                    closestIntersection = chroma;
                    found = true;
                    float y = a.y + t * (b.y - a.y);

                    gbp.chroma = chroma;
                    gbp.y = y;
                }
            }
        }
        if (!found) {
            for (int i = 0; i < num; i++) {
                GamutBoundaryPoint a = (GamutBoundaryPoint) al.get(i);
                GamutBoundaryPoint b = (GamutBoundaryPoint) al.get((i + 1) % num);
                float t = GamutBoundaryPoint.distanseAlongLine(a.chroma, a.y, b.chroma, b.y, pointx, pointy, rayx, rayy);

                float chroma = a.chroma + t * (b.chroma - a.chroma);
                float y = a.y + t * (b.y - a.y);

                if (t < 0.0F) {
                    float distSquared = (a.y - y) * (a.y - y) + (a.chroma - chroma) * (a.chroma - chroma);

                    if ((distSquared < 1.0F)
                            && (chroma >= closestIntersection)) {
                        closestIntersection = chroma;
                        found = true;
                        gbp.chroma = chroma;
                        gbp.y = y;
                    }
                } else {
                    float distSquared = (b.y - y) * (b.y - y) + (b.chroma - chroma) * (b.chroma - chroma);

                    if ((distSquared >= 1.0F)
                            || (chroma < closestIntersection)) {
                        continue;
                    }
                    closestIntersection = chroma;
                    found = true;
                    gbp.chroma = chroma;
                    gbp.y = y;
                }

            }

            if (!found) {
                System.out.println("");
                System.out.println("Num " + num);

                System.out.println("Point: " + pointx + "," + pointy);
                System.out.println("Ray: " + rayx + "," + rayy);
                for (int i = 0; i < num; i++) {
                    GamutBoundaryPoint a = (GamutBoundaryPoint) al.get(i);

                    System.out.println("gbp: " + a.chroma + "," + a.y);
                }
                GamutBoundaryPoint.release(gbp);
                return null;
            }
        }
        return gbp;
    }
}
