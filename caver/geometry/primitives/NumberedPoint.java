package geometry.primitives;

import java.util.Collection;

public class NumberedPoint implements GeneralPoint {

    private int id_;
    private Point p_;

    private NumberedPoint() {
        throw new UnsupportedOperationException();
    }

    public NumberedPoint(int id, Point p) {
        this.id_ = id;
        this.p_ = p;
    }

    public int getId() {
        return id_;
    }

    @Override
    public int hashCode() {
        return id_;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof NumberedPoint)) {
            return false;
        }
        NumberedPoint p = (NumberedPoint) o;
        return getId() == p.getId();

    }

    public static Point center(Collection<Point> points) {
        return Point.center(points);
    }

    @Override
    public double squaredSize() {
        return p_.squaredDistance(p_);
    }

    @Override
    public double size() {
        return p_.size();
    }

    @Override
    public double distance(Point p) {
        return p.distance(p);
    }

    @Override
    public double squaredDistance(Point p) {
        return p.squaredDistance(p);
    }

    /*
     * Dot product. Skalarni soucin.
     */
    @Override
    public double dot(Point p) {
        return p.dot(p);
    }

    @Override
    public String toString() {
        return p_.toString();
    }

    @Override
    public double getX() {
        return p_.getX();
    }

    @Override
    public double getY() {
        return p_.getY();
    }

    @Override
    public double getZ() {
        return p_.getZ();
    }

    @Override
    public double[] getCoordinates() {
        return p_.getCoordinates();
    }
}
