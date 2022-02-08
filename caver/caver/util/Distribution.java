package caver.util;

/*
 * Processes observed numerical values and computes their statistical
 * properties.
 */
public class Distribution {

    private double average_;
    private double standardDeviation_;
    private double max_;
    private int size_;

    public Distribution(double[] distribution) {
        average_ = average(distribution);
        standardDeviation_ = standardDeviation(distribution);
        max_ = max(distribution);
        size_ = distribution.length;
    }

    public double getAverage() {
        return average_;
    }

    public int size() {
        return size_;
    }

    public double getStandardDeviation() {
        return standardDeviation_;
    }

    public double getMax() {
        return max_;
    }

    private static double average(double[] a) {
        double sum = 0;
        int count = 0;
        for (double d : a) {
            sum += d;
            count++;
        }
        return sum / count;
    }

    private static double standardDeviation(double[] a) {
        double sum = 0;
        int count = 0;
        double average = average(a);
        for (double d : a) {
            sum += (d - average) * (d - average);
            count++;
        }
        return Math.sqrt(sum / count);
    }

    private static double max(double[] a) {
        double max = Double.NEGATIVE_INFINITY;
        for (double d : a) {
            if (max < d) {
                max = d;
            }
        }
        return max;
    }
}
