package algorithms.clustering.layers;

import geometry.primitives.Point;

public class WeightedPoint {

    private Point point_; // point coordinate P
    private Point unit_; // unit vector, normalized SP, where S is averaged 
    //                      starting point of all analyzed tunnels 
    private double weight_;

    public WeightedPoint(Point unit, Point p, double w) {
        unit_ = unit;
        point_ = p;
        weight_ = w;
    }

    public void update(Point p, double weight) {
        weight_ = weight;
        point_ = p;
    }

    public double getWeight() {
        return weight_;
    }

    public Point getPoint() {
        return point_;
    }

    public double[] getKey() {
        return unit_.getCoordinates();
    }

    public Point getUnit() {
        return unit_;
    }
}
