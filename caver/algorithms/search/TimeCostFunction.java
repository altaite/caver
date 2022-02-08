package algorithms.search;

import caver.Printer;

/*
 * The implementation of cost function described int CAVER 3.0: A Tool for the
 * Analysis of Transport Pathways in Dynamic Protein Structures
 *
 */
public class TimeCostFunction implements CostFunction {

    double exponent_;
    double maxLimitingRadius_;
    double minRadius_ = 0.1;

    public TimeCostFunction(double radiusDecrease,
            double timeIncrease, double maxLimitingRadius, double minRadius) {
        exponent_ = -Math.log(timeIncrease) / Math.log(radiusDecrease);
        maxLimitingRadius_ = maxLimitingRadius;
        minRadius_ = minRadius;
    }

    public TimeCostFunction(double exponent, double maxLimitingRadius,
            double minRadius) {
        exponent_ = exponent;
        maxLimitingRadius_ = maxLimitingRadius;
        minRadius_ = minRadius;
    }

    public double getTime(double radius) {
        if (radius < minRadius_) {
            radius = minRadius_;
        }
        if (radius <= 0) {
            return Double.MAX_VALUE;
        } else if (maxLimitingRadius_ <= radius) {
            return maxLimitingRadius_;
        } else {
            double value = Math.pow(radius, (-1) * exponent_);
            return value;
        }
    }

    @Override
    public double getCost(double x, double y, double z, double radius, double length) {
        return getCost(radius, length);
    }

    @Override
    public double getCost(double radius, double length) {
        double t = getTime(radius);

        return length * t;
    }

    @Override
    public boolean overflow(double radius) {
        if (Double.MAX_VALUE / 1000 < getTime(radius)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void printGraph() {
        Printer.println("Using cost function: length / radius^" + exponent_);
        Printer.println("Table of costs of path segments of length 1 A and variable radius:");
        StringBuilder sb = new StringBuilder("r");
        for (double r = 0.1; r <= 2; r += 0.1) {
            sb.append(" ");
            sb.append(r);
        }
        Printer.println(sb.toString());
        sb = new StringBuilder("cost");
        for (double r = 0.1; r <= 2; r += 0.1) {
            sb.append(" ");
            sb.append(getCost(r, 1));
        }
        Printer.println(sb.toString());
    }

    public static void main(String[] args) {
        double value = Math.pow(0.0009, (-1) * 100);
        System.out.println("value " + value);
        System.out.println("value " + 2000 * (value + value + value));
    }
}
