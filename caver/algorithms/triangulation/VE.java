package algorithms.triangulation;

import algorithms.search.CostFunction;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.Serializable;
import java.util.List;

/**
 *
 * Represents a Voronoi Edge. It is used as an atomic part of a tunnel or void -
 * a line and the sphere, which defines max. radius of a probe traveling along
 * the line.
 *
 */
public class VE implements Serializable {

    private static VE blockedEdge;
    private final static long serialVersionUID = 1L;
    private int nodeA_;
    private int nodeB_;
    private Sphere s_; // limiting sphere, defines max probe radius in 
    //                    every point on AB
    private Point a_; // point A
    private Point b_; // point B
    private double weight_;
    private Sphere bottleneck_ = null;

    static {
        blockedEdge = new VE();
        blockedEdge.weight_ = Double.MAX_VALUE;
        blockedEdge.bottleneck_ = new Sphere(0, 0, 0, Double.NEGATIVE_INFINITY);
    }

    private VE() {
    }

    public VE(int nodeA, int nodeB, Point a, Point b, Sphere s,
            CostFunction f) {
        this.nodeA_ = nodeA;
        this.nodeB_ = nodeB;
        this.s_ = s;
        this.a_ = a;
        this.b_ = b;
        computeWeightAndBottleneck(f);
    }

    private VE(int nodeA, int nodeB, Point a, Point b, Sphere s,
            double weight, Sphere bottleneck) {
        this.nodeA_ = nodeA;
        this.nodeB_ = nodeB;
        this.s_ = s;
        this.a_ = a;
        this.b_ = b;
        weight_ = weight;
        bottleneck_ = bottleneck;
    }

    public VE flip() {
        VE ve = new VE(nodeB_, nodeA_, b_, a_, s_, weight_,
                bottleneck_);
        return ve;
    }

    public static VE getBlockedEdge() {
        return blockedEdge;

    }

    @Override
    public boolean equals(Object o) {
        VE e = (VE) o;
        return (e.nodeA_ == nodeA_ && e.nodeB_ == nodeB_)
                || (e.nodeA_ == nodeB_ && e.nodeB_ == nodeA_);
    }

    @Override
    public int hashCode() {
        return nodeA_ * nodeB_;
    }

    public Sphere getS() {
        return s_;
    }

    public int getNodeA() {
        return nodeA_;
    }

    public int getNodeB() {
        return nodeB_;
    }

    public Point getA() {
        return a_;
    }

    public Point getB() {
        return b_;
    }

    public double getARadius() {
        return s_.distance(getA());
    }

    public double getBRadius() {
        return s_.distance(getB());
    }

    public double getRadius(Point p) {
        return s_.distance(p);
    }

    public double getLength() {
        return a_.distance(b_);
    }

    public double getWeight() {
        return weight_;
    }

    public Sphere getBottleneck() {
        return bottleneck_;
    }

    private void computeWeightAndBottleneck(CostFunction f) {
        double e = 0.1;
        int n = (int) Math.ceil(b_.minus(a_).size() / e);
        if (n < 8) {
            n = 8;
        }
        if (1000 < n) {
            n = 1000;
        }
        Point v = b_.minus(a_).divide(n);
        // length of segment, i.e. voronoi edge
        double length = v.size();

        double sum = 0;
        double minR = Double.MAX_VALUE;
        Point minP = a_;
        Point p;
        for (int i = 0; i <= n; i++) {
            p = a_.plus(v.multiply(i));
            // radius of greatest sphere placeble at point p
            double r = s_.distance(p);
            if (r < 0) {
                r = 0;
            }
            if (r < minR) {
                minR = r;
                minP = p;
            }
            double c = f.getCost(r, length);

            sum += c;
        }

        weight_ = sum * b_.minus(a_).divide(n).size();
        bottleneck_ = new Sphere(minP, minR);
    }

    public String cut(double d) {
        return String.format("%.4f", d);
    }

    /*
     * step - distance of sphere centers phase - distance of first center to
     * point A returns - next phase
     */
    public double getSpheres(double step, double phase, List<Sphere> spheres) {

        double size = a_.distance(b_);
        int n;

        if (size <= phase) {
            return phase - size;
        } else {
            n = (int) Math.floor((size - phase) / step);
        }


        Point v = b_.minus(a_).normalize(); // unit vector
        assert Math.abs(v.size() - 1) < 0.001;

        for (int i = 0; i <= n; i++) {
            Point p = a_.plus(v.multiply(phase + i * step));

            double r = s_.distance(p);
            if (r < 0) {
                r = 0;
            }
            Sphere s = new Sphere(p, r);
            spheres.add(s);
        }

        double rest = size - phase - n * step;
        assert rest < step;
        assert 0 <= rest;

        return step - rest;
    }

    public Sphere getASphere() {
        return new Sphere(a_, a_.distance(s_.getS()) - s_.getR());
    }

    public Sphere getBSphere() {
        return new Sphere(b_, b_.distance(s_.getS()) - s_.getR());
    }
}
