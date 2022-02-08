package algorithms.triangulation.qhull;

public class Vertex implements Comparable {

    public int index;
    public int data;
    boolean isAssigned;
    int dimension;
    public double[] coordinates;

    public Vertex() {
        this.coordinates = new double[3];
        this.dimension = 3;
    }

    public Vertex(double x, double y) {
        this.coordinates = new double[2];
        this.dimension = 2;
        this.coordinates[0] = x;
        this.coordinates[1] = y;
    }

    public double[] get() {
        return this.coordinates;
    }

    public Vertex(double x, double y, double z) {
        this.coordinates = new double[3];
        this.dimension = 3;
        this.coordinates[0] = x;
        this.coordinates[1] = y;
        this.coordinates[2] = z;
    }

    public void set(double x, double y, double z) {
        this.coordinates[0] = x;
        this.coordinates[1] = y;
        this.coordinates[2] = z;
    }

    public Vertex(double x, double y, double z, double w) {
        this.coordinates = new double[4];
        this.dimension = 4;
        this.coordinates[0] = x;
        this.coordinates[1] = y;
        this.coordinates[2] = z;
        this.coordinates[3] = w;
    }

    public Vertex add(Vertex x) {
        Vertex a = new Vertex();

        for (int i = 0; i < this.dimension; i++) {
            this.coordinates[i] += x.coordinates[i];
        }
        return a;
    }

    public Vertex subtract(Vertex x) {
        Vertex a = new Vertex();

        for (int i = 0; i < this.dimension; i++) {
            this.coordinates[i] -= x.coordinates[i];
        }
        return a;
    }

    public Vertex scale(double x) {
        Vertex a = new Vertex();

        for (int i = 0; i < this.dimension; i++) {
            a.coordinates[i] = (x * this.coordinates[i]);
        }
        return a;
    }

    public Vertex scale(double x, double y, double z) {
        return new Vertex(x * this.coordinates[0], y * this.coordinates[1], z * this.coordinates[2]);
    }

    public Vertex scale(double x, double y, double z, double w) {
        return new Vertex(x * this.coordinates[0], y * this.coordinates[1], z * this.coordinates[2], w * this.coordinates[3]);
    }

    public double dot(Vertex x) {
        double d = 0.0D;

        for (int i = 0; i < this.dimension; i++) {
            d += this.coordinates[i] * x.coordinates[i];
        }
        return d;
    }

    public Vertex normalize() {
        return scale(1.0D / length());
    }

    public double length() {
        return Math.sqrt(dot(this));
    }

    public Vertex cross(Vertex x) {
        return new Vertex(this.coordinates[1] * x.coordinates[2] - x.coordinates[1] * this.coordinates[2], this.coordinates[2] * x.coordinates[0] - x.coordinates[2] * this.coordinates[0], this.coordinates[0] * x.coordinates[1] - x.coordinates[0] * this.coordinates[1]);
    }

    public void addDimension() {
        double d = 0.0D;

        for (int i = 0; i < this.dimension; i++) {
            d += this.coordinates[i] * this.coordinates[i];
        }
        addDimension(d);
    }

    public void addDimension(double d) {
        double[] old = this.coordinates;

        this.coordinates = new double[this.dimension + 1];
        System.arraycopy(old, 0, this.coordinates, 0, old.length);
        this.coordinates[this.dimension] = d;
        this.dimension += 1;
    }

    public int compareTo(Object o) {
        Vertex v = (Vertex) o;

        if (v.coordinates[0] > this.coordinates[0]) {
            return -1;
        }
        if (v.coordinates[0] < this.coordinates[0]) {
            return 1;
        }
        double diff = 0.0D;

        for (int i = 1; i < this.dimension; i++) {
            diff += this.coordinates[i] * this.coordinates[i] - v.coordinates[i] * v.coordinates[i];
        }

        if (diff < 0.0D) {
            return 1;
        }
        if (diff > 0.0D) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        String s = new String();

        for (int i = 0; i < this.dimension - 1; i++) {
            s = s + this.coordinates[i] + " ";
        }
        s = s + this.coordinates[(this.dimension - 1)];
        return s;
    }
}
