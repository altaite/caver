package algorithms.clustering.statistics;

import java.util.ArrayList;
import java.util.List;

/*
 * Class represents histogram range and number of its intervals.
 *
 */
public class Histogram {

    private double left_, right_;
    int n_;

    /*
     * null indicated min and max
     */
    public Histogram(double left, double right, int step) {
        this.left_ = left;
        this.right_ = right;
        this.n_ = step;
    }

    public double getLeft() {
        return left_;
    }

    public double getRight() {
        return right_;
    }

    public int getN() {
        return n_;
    }

    public List<Double> getBorders() {
        List<Double> l = new ArrayList<Double>();
        for (int i = 0; i <= n_; i++) {
            l.add(left_ + ((double) i / n_) * (right_ - left_));
        }
        return l;
    }
}
