package algorithms.triangulation.qhull;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

public final class Plane {

    public static final int PLANE_INSIDE = -1;
    public static final int IN_PLANE = 0;
    public static final int PLANE_OUTSIDE = 1;
    protected float normalX;
    protected float normalY;
    protected float normalZ;
    protected float distance;

    public Plane() {
    }

    public Plane(Vector3f a, Vector3f b, Vector3f c) {
        set(a, b, c);
    }

    public void set(Tuple3f a, Tuple3f b, Tuple3f c) {
        float v1x = b.x - a.x;
        float v1y = b.y - a.y;
        float v1z = b.z - a.z;

        float v2x = c.x - a.x;
        float v2y = c.y - a.y;
        float v2z = c.z - a.z;

        this.normalX = (v1y * v2z - v1z * v2y);
        this.normalY = (v1z * v2x - v1x * v2z);
        this.normalZ = (v1x * v2y - v1y * v2x);

        float len = (float) Math.sqrt(this.normalX * this.normalX + 
                this.normalY * this.normalY + this.normalZ * this.normalZ);

        if (len == 0.0F) {
            this.normalX = 1.0F;
            this.normalY = 0.0F;
            this.normalZ = 0.0F;
        } else {
            this.normalX /= len;
            this.normalY /= len;
            this.normalZ /= len;
        }
        this.distance = (this.normalX * a.x + this.normalY * a.y + 
                this.normalZ * a.z);
    }

    public Plane(Plane p) {
        this.normalX = p.normalX;
        this.normalY = p.normalY;
        this.normalZ = p.normalZ;
        this.distance = p.distance;
    }

    public Plane(float nX, float nY, float nZ, float d) {
        this.normalX = nX;
        this.normalY = nY;
        this.normalZ = nZ;
        this.distance = d;
    }

    public Plane(float p1x, float p1y, float p1z, float p2x, float p2y, float
            p2z, float p3x, float p3y, float p3z) {
        set(p1x, p1y, p1z, p2x, p2y, p2z, p3x, p3y, p3z);
    }

    public final void set(float p1x, float p1y, float p1z, float p2x, 
            float p2y, float p2z, float p3x, float p3y, float p3z) {
        float v1x = p2x - p1x;
        float v1y = p2y - p1y;
        float v1z = p2z - p1z;

        float v2x = p3x - p1x;
        float v2y = p3y - p1y;
        float v2z = p3z - p1z;

        this.normalX = (v1y * v2z - v1z * v2y);
        this.normalY = (v1z * v2x - v1x * v2z);
        this.normalZ = (v1x * v2y - v1y * v2x);

        float len = (float) Math.sqrt(this.normalX * this.normalX +
                this.normalY * this.normalY + this.normalZ * this.normalZ);

        if (len == 0.0F) {
            this.normalX = 1.0F;
            this.normalY = 0.0F;
            this.normalZ = 0.0F;
        } else {
            this.normalX /= len;
            this.normalY /= len;
            this.normalZ /= len;
        }
        this.distance = (this.normalX * p1x + this.normalY * p1y + 
                this.normalZ * p1z);
    }

    public final int classifyPoint(float pointX, float pointY, float pointZ) {
        float f = this.normalX * pointX + this.normalY * pointY +
                this.normalZ * pointZ;

        if (f > this.distance + 1.0E-004F) {
            return 1;
        }
        if (f < this.distance - 1.0E-004F) {
            return -1;
        }
        return 0;
    }

    public final int classifyPoint2(float pointX, float pointY, float pointZ) {
        float f = this.normalX * pointX + this.normalY * pointY + 
                this.normalZ * pointZ;

        if (f > this.distance) {
            return 1;
        }
        if (f < this.distance) {
            return -1;
        }
        return 0;
    }

    public Point3f projectPointToPlane(float px, float py, float pz) {
        int side = classifyPoint(px, py, pz);

        if (side != 0) {
            float dist = this.normalX * px + this.normalY * py + 
                    this.normalZ * pz - this.distance;

            Point3f retPoint = new Point3f(px, py, pz);

            retPoint.x -= dist * this.normalX;
            retPoint.y -= dist * this.normalY;
            retPoint.z -= dist * this.normalZ;
            return retPoint;
        }

        return new Point3f(px, py, pz);
    }

    public Vector3f intersectionPoint(Tuple3f start, Tuple3f end) {
        Vector3f ray = new Vector3f();

        ray.sub(end, start);
        float t = -(this.normalX * start.x + this.normalY * start.y + 
                this.normalZ * start.z - this.distance) / 
                (this.normalX * ray.x + this.normalY * ray.y + 
                this.normalZ * ray.z);

        if (t > 1.0F) {
            t = 1.0F;
        } else if (t < 0.0F) {
            t = 0.0F;
        }
        ray.scale(t);
        ray.add(start);
        return ray;
    }

    public final float intersectionTimeAsWeight(float p1x, float p1y, 
            float p1z, float p2x, float p2y, float p2z) {
        float dist1 = p1x * this.normalX + p1y * this.normalY + p1z *
                this.normalZ - this.distance;
        float dist2 = p2x * this.normalX + p2y * this.normalY + p2z *
                this.normalZ - this.distance;

        if (dist1 < 0.0F) {
            dist1 = -dist1;
        }
        if (dist2 < 0.0F) {
            dist2 = -dist2;
        }
        if (dist1 == 0.0F) {
            if (dist2 == 0.0F) {
                return 0.5F;
            }
            return 0.0F;
        }
        return dist2 / (dist1 + dist2);
    }

    public final float distanceTo(float x, float y, float z) {
        return x * this.normalX + y * this.normalY + z * this.normalZ -
                this.distance;
    }

    public void incDistance() {
        float epsilon = 1.0E-005F;
        float oldDistance = this.distance;

        while (this.distance == oldDistance) {
            if (Math.random() < 0.5D) {
                this.distance += epsilon;
            } else {
                this.distance -= epsilon;
            }
            epsilon *= 2.0F;
        }
    }
}
