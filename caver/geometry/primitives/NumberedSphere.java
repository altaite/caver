package geometry.primitives;

public class NumberedSphere implements GeneralSphere {

    private int id_;
    private Point s_;
    private double r_;

    private NumberedSphere() {
    }

    public NumberedSphere(int id, Point s, double r) {
        this.id_ = id;
        this.s_ = s;
        this.r_ = r;
    }

    public Sphere getSphere() {
        Sphere sphere = new Sphere(new Point(s_), r_);
        return sphere;
    }

    @Override
    public Point getS() {
        return s_;
    }

    public int getId() {
        return id_;
    }

    @Override
    public double getR() {
        return r_;
    }

    @Override
    public String toString() {
        return "S" + getId() + ": " + s_.toString() + " r=" + r_;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof NumberedSphere)) {
            return false;
        }
        NumberedSphere sphere = (NumberedSphere) o;
        return getId() == sphere.getId();
    }

    @Override
    public int hashCode() {
        return id_;
    }
}
