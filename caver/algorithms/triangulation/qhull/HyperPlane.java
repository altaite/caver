package algorithms.triangulation.qhull;

public class HyperPlane {

    public static final Vertex zVector = new Vertex(0.0D, 0.0D, 1.0D);
    Vertex normal;
    double d;

    public HyperPlane(Vertex a, Vertex b, Vertex c) {
        this.normal = b.subtract(a).cross(c.subtract(a)).normalize();
        this.d = this.normal.dot(a);
    }

    public HyperPlane(Vertex a, Vertex b) {
        this.normal = b.subtract(a).cross(zVector).normalize();
        this.d = this.normal.dot(a);
    }

    public HyperPlane(Vertex a, Vertex b, int n) {
        this.normal = b.subtract(a).cross(zVector).normalize();
        this.normal = new Vertex(this.normal.coordinates[0],
                this.normal.coordinates[1], this.normal.coordinates[2], 0.0D);

        this.d = this.normal.dot(a);
    }

    public boolean outside(Vertex x) {
        return this.normal.dot(x) > this.d;
    }

    public void assign(Vertex a, Vertex b, Vertex c) {
        this.normal = b.subtract(a).cross(c.subtract(a)).normalize();
        this.d = this.normal.dot(a);
    }
}
