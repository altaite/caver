package algorithms.clustering;

import geometry.primitives.Point;

public class Line {

    private Point a;
    private Point b;

    public Line(Point a, Point b) {
        this.a = a;
        this.b = b;
    }

    public Point getT() {
        return a.plus(b).divide(2);
    }

    public float getLength() {
        return (float) Math.sqrt(a.minus(b).squaredSize());
    }

    public Point getA() {
        return a;
    }

    public Point getB() {
        return b;
    }
}
