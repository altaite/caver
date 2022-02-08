package algorithms.search;

import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.util.HashSet;
import java.util.Set;

/**
 * The class void represents any space not occupied by atoms. Typically 
 * a void or a pocket.
 */
public class Void implements Comparable<Void> {

    private double rMax = Double.NEGATIVE_INFINITY;
    private Point pMax;
    private Double volume = null;
    private Set<Sphere> spheres = new HashSet<Sphere>(); 
    // spheres making up the volume of the void

    public void add(Sphere s) {
        double r = s.getR();
        if (rMax < r) {
            rMax = r;
            pMax = s.getS();
        }
        spheres.add(s);
    }

    public Point getMaxPoint() {
        return pMax;
    }

    @Override
    public int compareTo(Void v) {
        return -new Double(guessVolume()).compareTo(v.guessVolume());
    }

    public double getR() {
        return rMax;
    }

    public Set<Sphere> getSpheres() {
        return spheres;
    }

    public double guessVolume() {
        if (null == volume) {
            volume = 0.0;
            for (Sphere s : spheres) {
                double r = s.getR();
                volume += r * r * r;
            }
        }
        return volume;
    }

    public Point getP() {
        return pMax;
    }

    public Point getClosest(Point p, double minR) {
        Point closest = null;
        double dist = Double.MAX_VALUE;
        for (Sphere s : spheres) {
            if (minR <= s.getR()) {

                double d = p.distance(s.getS());
                if (d < dist) {
                    dist = d;
                    closest = s.getS();
                }
            }
        }
        if (null == closest) {
            dist = Double.MAX_VALUE;
            for (Sphere s : spheres) {
                double d = p.distance(s.getS());
                if (d < dist) {
                    dist = d;
                    closest = s.getS();
                }
            }
        }

        return closest;
    }
}
