package algorithms.clustering.layers;

import caver.CalculationSettings;
import caver.tunnels.Tunnel;
import caver.tunnels.TunnelCostComparator;
import chemistry.pdb.PdbLine;
import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Class represents the smoothened surface, which is computed by averaging
 * positions of tunnel endings. For more details see section about clustering
 * in: CAVER 3.0: A Tool for the Analysis of Transport Pathways in Dynamic
 * Protein Structures
 *
 */
public class AverageSurface {

    private KDTree<WeightedPoint> kdTree_;
    private String warning1_ = "Key duplicate in average surface "
            + "for layers.";
    private String warning2_ = "Key size wrong in average surface "
            + "for layers.";
    private CalculationSettings cs_;
    private Point source_;

    public AverageSurface(List<Tunnel> tunnels,
            Point source, CalculationSettings cs) {

        Collections.sort(tunnels, new TunnelCostComparator());

        cs_ = cs;
        kdTree_ = new KDTree<WeightedPoint>(3);

        source_ = source;

        for (Tunnel t : tunnels) {
            List<Sphere> spheres = t.computeProfile(
                    cs.getAverageSurfaceTunnelSamplingStep());
            Sphere farthest = null;
            double max = 0;
            for (Sphere s : spheres) {
                double d = source.distance(s.getS());
                if (max < d) {
                    max = d;
                    farthest = s;
                }
            }

            if (null != farthest) {
                include(farthest.getS(), source, cs.getSurfacePointMaxDist());
            }

        }

    }

    public int size() {
        return kdTree_.size();
    }

    private void insert(WeightedPoint wp)
            throws KeySizeException, KeyDuplicateException {
        kdTree_.insert(wp.getKey(), wp);
    }

    public final void include(Point a, Point s, double r) {

        Point au = a.minus(s).normalize();
        WeightedPoint aw = new WeightedPoint(au, a, 1);
        try {
            boolean insert = true;
            if (0 < kdTree_.size()) {

                // prevent Exception
                if (null != kdTree_.search(au.getCoordinates())) {
                    insert = false;
                }

                // build sparse
                WeightedPoint nearest =
                        kdTree_.nearest(au.getCoordinates());
                double d = new Point(nearest.getKey()).distance(
                        new Point(aw.getKey()));
                if (null != nearest && d < r) {
                    WeightedPoint average =
                            average(aw, nearest, source_, true);

                    double height = average.getPoint().minus(source_).size();
                    Point update = aw.getPoint().minus(source_).
                            normalize().multiply(height).plus(source_);

                    // updating just properties of point, not its position in 
                    // KD tree, because this KD library does not perform 
                    // updates correctly
                    nearest.update(update, average.getWeight());
                    insert = false;
                }

            }
            if (insert) { // point not present
                insert(aw);
            }
        } catch (KeySizeException e) {
            Logger.getLogger("caver").log(Level.SEVERE, warning2_, e);
        } catch (KeyDuplicateException e) {
            Logger.getLogger("caver").log(Level.SEVERE, warning1_
                    + "\n" + kdTree_.size() + " ", e);
        }

    }

    /*
     * Returns the center of gravity of two weighted points. The weight of the
     * resulting point equals to the sum of original points.
     */
    public final WeightedPoint average(WeightedPoint a, WeightedPoint b,
            Point s, boolean weighted) {

        double aw = 1;
        double bw = 1;
        if (weighted) {
            aw = a.getWeight();
            bw = b.getWeight();
        }

        Point ar = a.getPoint().minus(s);
        Point br = b.getPoint().minus(s);
        Point au = ar.normalize();
        Point bu = br.normalize();
        Point cu = au.plus(bu).divide(aw + bw).normalize();

        Point c = s.plus(cu.multiply((aw * ar.size() + bw * br.size()) / (aw + bw)));
        WeightedPoint cw = new WeightedPoint(cu, c, a.getWeight() + b.getWeight());


        return cw;
    }

    /*
     * Sets point to average height of "surface" points around p. Height is
     * computed as distance of source_ and a point.
     */
    public Point getSurfacePoint(Point p) {

        Point u = p.minus(source_).normalize();

        try {
            List<WeightedPoint> list =
                    kdTree_.nearestEuclidean(
                    u.getCoordinates(),
                    cs_.getSurfaceSmoothness());
            if (null == list || list.isEmpty()) {
                list = kdTree_.nearest(u.getCoordinates(),
                        cs_.getAdvanced().getMinSurfaceNeighbours());
            }

            double sum = 0;
            int count = 0;
            for (WeightedPoint wp : list) {
                sum += wp.getPoint().minus(source_).size();
                count++;
            }
            Point q = u.multiply(sum / count).plus(source_);
            return q;
        } catch (KeySizeException e) {
            throw new RuntimeException(e);
        }


    }

    private void savePoints(File file, Collection<Point> points) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            PrintStream ps = new PrintStream(out);
            int serial = 1;
            for (Point p : points) {
                PdbLine pl = new PdbLine(serial, "H", "TUN", "RRR", 'T',
                        p.getX(), p.getY(), p.getZ());
                ps.println(pl.getPdbString());
                serial++;

            }

            ps.close();
        } catch (IOException e) {
            Logger.getLogger("caver").log(
                    Level.SEVERE, file.getAbsolutePath(), e);
        }
    }

    public void saveDefinition(File file) {
        double a = 1000000;
        double[] lo = {-a, -a, -a};
        double[] hi = {a, a, a};


        try {
            List<WeightedPoint> list = kdTree_.range(lo, hi);
            List<Point> points = new ArrayList<Point>();

            for (WeightedPoint wp : list) {
                points.add(wp.getPoint());
            }


            savePoints(file, points);

        } catch (KeySizeException e) {
            Logger.getLogger("caver").log(Level.SEVERE, "saveDefinition", e);
        }
    }

    public void save(File file, Iterable<Tunnel> tunnels,
            CalculationSettings cs) {

        List<Point> points = new ArrayList<Point>();
        for (Tunnel t : tunnels) {
            if (cs_.lucky()) {
                List<Sphere> spheres = t.computeProfile(
                        cs.getAverageSurfaceTunnelSamplingStep());

                Sphere farthest = null;
                double max = 0;
                for (Sphere s : spheres) {
                    double d = source_.distance(s.getS());
                    if (max < d) {
                        max = d;
                        farthest = s;
                    }
                }
                points.add(getSurfacePoint(farthest.getS()));
            }
        }

        if (cs_.getAdvanced().showExtrapolatedSurfacePoints()) {
            Random r = new Random(1);
            for (int i = 0; i < 10000; i++) {
                Point p = new Point(
                        r.nextDouble(), r.nextDouble(), r.nextDouble()).minus(
                        new Point(0.5, 0.5, 0.5)).plus(source_);
                points.add(getSurfacePoint(p));
            }
        }
        savePoints(file, points);
    }
}
