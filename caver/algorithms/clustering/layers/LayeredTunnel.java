package algorithms.clustering.layers;

import algorithms.clustering.Line;
import caver.CalculationSettings;
import caver.Clock;
import caver.LayersSettings;
import caver.Printer;
import caver.tunnels.Tunnel;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Class for representing a tunnel by a fixed number of points. For more details
 * about representation and related algorithms see the section about tunnel
 * clustering in: CAVER 3.0: A Tool for the Analysis of Transport Pathways in
 * Dynamic Protein Structures
 */
public class LayeredTunnel {

    private Tunnel tunnel_;
    private int n_; // number of points to represent of the tunnel
    public double[] x;
    public double[] y;
    public double[] z;
    public double[] mass;
    public boolean[] exists; // indicates if the point could be derived for
    private Point source_;
    private static SpaceTransformation spaceTransformation_ =
            SpaceTransformation.linear;

    public enum SpaceTransformation {

        linear,
        exponential
    }

    public LayeredTunnel(Point source,
            AverageSurface surface,
            int n,
            Tunnel tunnel,
            boolean averageSurface,
            LayersSettings ls,
            CalculationSettings cs) {
        n_ = n;
        tunnel_ = tunnel;
        source_ = source;
        x = new double[n];
        y = new double[n];
        z = new double[n];
        mass = new double[n];
        exists = new boolean[n];
        boolean something = buildLayers(surface, averageSurface, ls, cs);
        if (!something) {
            cs.logNoLayers();
        }
        mass = null;
    }

    private void save(CalculationSettings cs) {
        if (cs.saveLayerPoints && cs.producePoints()) {
            if (null != tunnel_.getCluster()
                    && null != tunnel_.getCluster().getPriority()) {
                cs.setLayerResidue(tunnel_.getCluster().getPriority());
            }

            for (int i = 0; i < n_; i++) {
                Point p = new Point(x[i], y[i], z[i]);
                cs.saveLayerPoint(p);
            }

            cs.increaseLayerResidue();
        }

    }

    public final boolean buildLayers(
            AverageSurface surface,
            boolean averageSurface,
            LayersSettings ls,
            CalculationSettings cs) {
        Clock.start("layers 1");

        List<Sphere> spheres = tunnel_.computeProfile(
                cs.getProfileTunnelSamplingStep());
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.MAX_VALUE;

        Sphere farthest = null;
        int closest = 0;
        Sphere nearest = null;
        Point average = new Point(0, 0, 0);


        for (int i = 0; i < spheres.size(); i++) {
            Sphere s = spheres.get(i);

            average = average.plus(s.getS());
            double d = source_.distance(s.getS());
            if (max < d) {
                max = d;
                farthest = s;
            }
            if (d < min) {
                min = d;
                nearest = s;
                closest = i;
            }
        }

        average = average.divide(spheres.size());

        Point surfacePoint = null;
        if (averageSurface && null != surface) {
            Clock.start("average surface usage");
            Point p = farthest.getS();

            surfacePoint = surface.getSurfacePoint(p);
            Clock.stop("average surface usage");
            max = source_.distance(surfacePoint);
        }


        double ds = ls.getExcludeStartZone();
        double de = ls.getExcludeEndZone();
        double mz = ls.getMinMiddleZone();
        if (max < mz) {
            mz = max;
        }
        if (ds + de + mz < max) { // do not elongate start and end
            mz = max - ds - de;
        }

        double localDeadStart;
        double localDeadEnd;

        if (0 == ds + de) {
            localDeadStart = 0;
            localDeadEnd = 0;
        } else {
            localDeadStart = (ds * (max - mz)) / (ds + de);
            localDeadEnd = (de * (max - mz)) / (ds + de);

        }

        Clock.stop("layers 1");
        Clock.start("layers 3");

        if (averageSurface && null != surface) {
            cs.saveDeadEnd(surfacePoint.plus(
                    source_.minus(
                    surfacePoint).normalize().multiply(
                    localDeadEnd)));

            cs.saveDeadStart(source_.plus(
                    surfacePoint.minus(
                    source_).normalize().multiply(
                    localDeadStart)));

        }
        double width = (max - localDeadStart - localDeadEnd) / n_;

        if (spheres.size() < 2) {
            Logger.getLogger("caver").log(Level.SEVERE,
                    "Tunnel too short for layers!");
        }

        if (!cs.protect()) {
            closest = 0;
        }
        for (int i = closest; i < spheres.size() - 1; i++) {
            Point a = spheres.get(i).getS();
            Point b = spheres.get(i + 1).getS();
            for (int layer = 0; layer < n_; layer++) {

                double r1 = layer * width + localDeadStart;
                double r2 = (layer + 1) * width + localDeadStart;

                Line[] lines = SphereSegmentIntersection.layerCut(
                        a, b, source_, r1, r2);
                for (Line line : lines) {

                    Point center = line.getT().multiply(line.getLength());
                    addLayer(layer, center.getX(), center.getY(), center.getZ(),
                            line.getLength());
                }
            }
        }

        for (int i = 0; i < n_; i++) {

            // protection against underflow
            if (cs.protect()) {
                if (getPoint(i).minus(source_).size() < 0.1) {
                    exists[i] = false;
                }
            }

            if (exists[i]) {
                divideByMass(i);
            }

            // protection against underflow
            if (cs.protect()) {
                if (getPoint(i).minus(source_).size() < 0.1) {
                    exists[i] = false;
                }
            }
        }

        boolean something = false;
        for (int i = 0; i < n_; i++) {
            something |= exists[i];
        }
        if (!something) {
            for (int i = 0; i < n_; i++) {
                x[i] = average.getX();
                y[i] = average.getY();
                z[i] = average.getZ();
            }
        }

        check();

        Clock.stop("layers 3");
        Clock.start("layers 4");

        for (int layer = 0; layer < n_; layer++) {
            if (!exists[layer]) {

                Point below = null;
                Point above = null;
                for (int i = layer; 0 <= i; i--) {
                    if (exists[i]) {
                        below = getPoint(i);
                        break;
                    }
                }

                for (int i = layer; i < size(); i++) {
                    if (exists[i]) {
                        above = getPoint(i);
                        break;
                    }
                }

                Point common = null;
                double d = localDeadStart + width * (layer + 0.5);

                if (null != below && null != above) {
                    Logger.getLogger("caver").log(Level.SEVERE,
                            "Suspicious tunnel geometry for tunnel ID {0} and "
                            + "snapshot {1}.",
                            new Object[]{tunnel_.getId(),
                                tunnel_.getSnapId().toString()});



                    common = average.minus(source_).normalize().multiply(
                            d).plus(source_);
                    assert common.check();
                } else if (null == below && null == above) {
                    common = average.minus(source_).normalize().multiply(
                            d).plus(source_);
                    assert common.check();
                } else {
                    if (null == above) {
                        above = farthest.getS();
                        Point v = above.minus(source_);
                        if (0 == v.size()) {
                            common = source_;
                        } else {
                            common = v.normalize().multiply(
                                    d).plus(source_);
                        }
                        assert above.check();
                        assert common.check();
                    }
                    if (null == below) {
                        below = nearest.getS();
                        Point v = below.minus(source_);
                        if (0 == v.size()) {
                            common = source_;
                        } else {
                            common = v.normalize().multiply(
                                    d).plus(source_);
                        }

                        assert below.check();
                        assert common.check();
                    }
                }

                setExtrapolatedLayer(layer, common);

            }
        }

        int count = 0;
        for (int layer = 0; layer < n_; layer++) {
            if (!exists[layer]) {
                count++;
            }
        }

        check();
        transform(ls.getWeightingCoefficient());
        save(cs);
        Clock.stop("layers 4");
        return something;
    }

    public Point multiplyHalfLine(Point a, Point b, double k) {
        return b.minus(a).multiply(k).plus(a);
    }

    public void check() {
        int l = 1000000;
        for (int i = 0; i < size(); i++) {
            if (l < x[i] || x[i] < -l) {
                throw new RuntimeException();
            }
            if (l < y[i] || y[i] < -l) {
                throw new RuntimeException();
            }
            if (l < z[i] || z[i] < -l) {
                throw new RuntimeException();
            }

            if (Double.isNaN(x[i])) {
                throw new RuntimeException();
            }
            if (Double.isNaN(y[i])) {
                throw new RuntimeException();
            }
            if (Double.isNaN(z[i])) {
                throw new RuntimeException();
            }

        }
    }

    private static double linearTransformation(double percent, double q) {
        return 2 * (1 - percent * (1 - q)) / (1 + q);
    }

    private static double exponentialTransformation(double percent, double q) {
        return 1 / Math.pow(10, q * percent);
    }

    public static double transformation(double percent, double q) {
        if (spaceTransformation_ == SpaceTransformation.linear) {
            return linearTransformation(percent, q);
            // relative shortening, point = rs * point
        } else {
            return exponentialTransformation(percent, q);
        }
    }

    /*
     * Moves more distant points towards center to give them less importance.
     * end - f(1.0) = end, f(0) = 1, linear decreasing function
     */
    private void transform(double q) {
        for (int i = 0; i < n_; i++) {

            double rd = (double) i / (n_ - 1); // relative distance

            double rs;

            rs = transformation(rd, q);
            // relative shortening, point = rs * point

            Point v = new Point(x[i], y[i], z[i]).minus(source_);

            Point p = source_.plus(v.multiply(rs));

            x[i] = p.getX();
            y[i] = p.getY();
            z[i] = p.getZ();


        }
    }

    private void divideByMass(int i) {
        if (mass[i] == 0) {
            Printer.warn("Zero mass for layer " + i);
        }
        double min = 0.00000001;
        if (mass[i] < min) {
            System.out.println(mass[i] + " quite low mass in layer " + i + ".");
        }

        x[i] /= mass[i];
        y[i] /= mass[i];
        z[i] /= mass[i];

    }

    private void addLayer(int index, double x, double y, double z, double mass) {
        exists[index] = true;
        this.x[index] += x;
        this.y[index] += y;
        this.z[index] += z;
        this.mass[index] += mass;
    }

    private void setExtrapolatedLayer(int index, Point p) {
        exists[index] = false;
        this.x[index] = p.getX();
        this.y[index] = p.getY();
        this.z[index] = p.getZ();
        this.mass[index] = 1;
    }

    public double getX(int index) {
        return x[index];
    }

    public double getY(int index) {
        return y[index];
    }

    public double getZ(int index) {
        return z[index];
    }

    public int size() {
        return n_;
    }

    public Tunnel getTunnel() {
        return tunnel_;
    }

    private Point getPoint(int layer) {
        return new Point(x[layer], y[layer], z[layer]);
    }
}
