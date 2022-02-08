package algorithms.clustering.statistics;

import algorithms.clustering.Cluster;
import caver.tunnels.Tunnel;
import caver.ui.graphics.MapDrawer;
import caver.util.Distribution;
import chemistry.Atom;
import chemistry.ResidueId;
import chemistry.pdb.SnapId;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Class for storing various information about tunnel cluster.
 */
public class ClusterStatistics {

    private Cluster cluster_;
    private static final List<StatisticsColumn> types_ = new ArrayList<StatisticsColumn>();
    private Set<SnapId> snaps_;
    private Double normalizedBottleneck_;
    private Double relativeAverageThroughput_;
    private Map<ResidueId, ClusterAtomContacts> residueTouches_ =
            new HashMap<ResidueId, ClusterAtomContacts>();
    private SortedSet<Atom> atoms_ = new TreeSet<Atom>();
    private int numTunnels_;
    private Distribution bottleneckDistribution_;
    private Distribution lengthDistribution_;
    private Distribution throughputDistribution_;
    private double[] throughputs_;
    private Distribution curvatureDistribution_;
    private Distribution bottleneckError_;
    private Distribution radiusMaxError_;
    private double spheresAverageError_ = NULL;
    private double maxBottleneck_;
    private double bestThroughputPercent_;
    private static Locale locale_ = Locale.ENGLISH;
    private static final int NULL = -1;

    static {
        types_.add(new StatisticsColumn("ID", 4));
        types_.add(new StatisticsColumn("No", 8));
        types_.add(new StatisticsColumn("No_snaps", 11));
        types_.add(new StatisticsColumn("Avg_BR", 9));
        types_.add(new StatisticsColumn("SD", 9));
        types_.add(new StatisticsColumn("Max_BR", 9));
        types_.add(new StatisticsColumn("Avg_L", 9));
        types_.add(new StatisticsColumn("SD", 8));
        types_.add(new StatisticsColumn("Avg_C", 8));
        types_.add(new StatisticsColumn("SD", 8));

        types_.add(new StatisticsColumn("Priority", 12));

        types_.add(new StatisticsColumn("Avg_throughput", 16));
        types_.add(new StatisticsColumn("SD", 9));

        types_.add(new StatisticsColumn("Avg_up_E_BR", 14));
        types_.add(new StatisticsColumn("SD", 8));
        types_.add(new StatisticsColumn("Max_up_E_BR", 14));
        types_.add(new StatisticsColumn("Avg_up_E_TR", 13));
        types_.add(new StatisticsColumn("Max_up_E_TR", 14));
    }

    public ClusterStatistics(Cluster c, Set<SnapId> snapSet,
            double bestThroughputPercent) {
        this.snaps_ = snapSet;
        this.cluster_ = c;
        this.numTunnels_ = c.getTunnels().size();
        this.bestThroughputPercent_ = bestThroughputPercent;

        calculateBottleneckDistribution();
        calculateLengthDistribution();
        calculateThroughputDistribution();
        calculateCurvatureDistribution();
        calculateMaxBottleneck();
    }

    public String[] getValues() {
        int digits = 2;
        String[] v = new String[types_.size()];
        int i = 0;



        v[i] = String.format(locale_, "%" + types_.get(i).getWidth() + "d",
                cluster_.getPriority());
        i++;
        v[i] = String.format(locale_, "%" + types_.get(i).getWidth() + "d",
                cluster_.getTunnels().size());
        i++;
        v[i] = String.format(locale_, "%" + types_.get(i).getWidth() + "d",
                cluster_.getTunnelsBySnapshots().size());
        i++;
        v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                + (digits + 1) + "f", bottleneckDistribution_.getAverage());
        i++;
        v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                + (digits + 1) + "f", bottleneckDistribution_.getStandardDeviation());
        i++;
        v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                + digits + "f", getMaxBottleneck());
        i++;
        v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                + (digits + 1) + "f", lengthDistribution_.getAverage());
        i++;
        v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                + (digits + 1) + "f", lengthDistribution_.getStandardDeviation());
        i++;
        v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                + (digits + 1) + "f", curvatureDistribution_.getAverage());
        i++;
        v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                + (digits + 1) + "f", curvatureDistribution_.getStandardDeviation());
        i++;
        v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                + 5 + "f", getPriority());
        i++;

        v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                + 5 + "f", throughputDistribution_.getAverage());
        i++;

        v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                + 5 + "f", throughputDistribution_.getStandardDeviation());
        i++;

        if (null != bottleneckError_) {
            v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                    + (digits + 1) + "f", bottleneckError_.getAverage());
        } else {
            v[i] = String.format(locale_, "%"
                    + (types_.get(i).getWidth()) + "s", "-");
        }
        i++;
        if (null != bottleneckError_) {
            v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                    + (digits + 1) + "f", bottleneckError_.getStandardDeviation());
        } else {
            v[i] = String.format(locale_, "%"
                    + (types_.get(i).getWidth()) + "s", "-");
        }
        i++;

        if (null != bottleneckError_) {
            v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                    + (digits + 1) + "f", bottleneckError_.getMax());
        } else {
            v[i] = String.format(locale_, "%"
                    + (types_.get(i).getWidth()) + "s", "-");
        }
        i++;

        if (NULL != spheresAverageError_) {
            v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                    + (digits + 1) + "f", spheresAverageError_);
        } else {
            v[i] = String.format(locale_, "%"
                    + (types_.get(i).getWidth()) + "s", "-");
        }
        i++;

        if (null != radiusMaxError_) {
            v[i] = String.format(locale_, "%" + (types_.get(i).getWidth()) + "."
                    + (digits + 1) + "f", radiusMaxError_.getMax());
        } else {
            v[i] = String.format(locale_, "%"
                    + (types_.get(i).getWidth()) + "s", "-");
        }
        i++;


        return v;
    }

    public static String getHeaderLine() {
        StringBuilder sb = new StringBuilder();
        for (StatisticsColumn sc : types_) {
            sb.append(String.format(locale_, "%" + sc.getWidth() + "s", sc.getShortHeader()));

        }
        return sb.toString();

    }

    public String getValueLine() {
        StringBuilder sb = new StringBuilder();
        String[] values = getValues();
        for (String v : values) {
            sb.append(v);
        }
        return sb.toString();
    }

    public final void calculateBottleneckDistribution() {

        Collection<Tunnel> ts = cluster_.getCheapestTunnelFromEachSnapshot().values();
        double[] values = new double[ts.size()];
        int i = 0;
        for (Tunnel t : ts) {
            values[i++] = t.getBottleneck().getR();
        }
        bottleneckDistribution_ = new Distribution(values);
    }

    public final void calculateLengthDistribution() {
        Collection<Tunnel> ts = cluster_.getCheapestTunnelFromEachSnapshot().values();
        double[] values = new double[ts.size()];
        int i = 0;
        for (Tunnel t : ts) {
            values[i++] = t.getLength();
        }

        lengthDistribution_ = new Distribution(values);


    }

    public final void calculateThroughputDistribution() {
        Collection<Tunnel> ts = cluster_.getCheapestTunnelFromEachSnapshot().values();
        throughputs_ = new double[ts.size()];
        int i = 0;
        for (Tunnel t : ts) {
            throughputs_[i++] = t.getThroughput();
        }

        throughputDistribution_ = new Distribution(throughputs_);
    }

    public Distribution getThroughputDistribution() {
        return throughputDistribution_;
    }

    public double[] getThroughputs() {
        return throughputs_;
    }

    public final void calculateCurvatureDistribution() {
        Collection<Tunnel> ts = cluster_.getCheapestTunnelFromEachSnapshot().values();
        double[] values = new double[ts.size()];
        int i = 0;
        for (Tunnel t : ts) {
            values[i++] = t.getCurvature();
        }
        curvatureDistribution_ = new Distribution(values);
    }

    public void calculateErrorBounds() {

        Collection<Tunnel> ts = cluster_.getCheapestTunnelFromEachSnapshot().values();
        double[] b = new double[ts.size()];
        double[] r = new double[ts.size()];
        int i = 0;
        double spheresSum = 0;
        int spheresCounter = 0;
        for (Tunnel t : ts) {
            b[i] = t.getBottleneckError();
            r[i] = t.getMaxError();
            spheresSum += t.getErrorSum();
            spheresCounter += t.getErrorCounter();
            i++;

        }
        bottleneckError_ = new Distribution(b);
        radiusMaxError_ = new Distribution(r);
        if (0 != spheresCounter) {
            spheresAverageError_ = spheresSum / spheresCounter;
        } else {
            spheresAverageError_ = NULL;
        }

    }

    public final void calculateMaxBottleneck() {
        double max = Double.NEGATIVE_INFINITY;
        for (Tunnel t : cluster_.getCheapestTunnelFromEachSnapshot().values()) {
            if (max < t.getBottleneck().getR()) {
                max = t.getBottleneck().getR();
            }
        }
        maxBottleneck_ = max;
    }

    public final double getPriority() {
        double avg = throughputDistribution_.getAverage();
        double p = avg * throughputDistribution_.size() / snaps_.size();
        return p;
    }

    public void calculateNormalizedAvgBottleneck(double max) {
        normalizedBottleneck_ = bottleneckDistribution_.getAverage() / max;
    }

    public double getNormalizedBottleneck() {
        if (null == normalizedBottleneck_) {
            throw new IllegalStateException();
        } else {
            return normalizedBottleneck_;
        }
    }

    public double getRelativeAverageThroughput() {
        if (null == relativeAverageThroughput_) {
            throw new IllegalStateException();
        } else {
            return relativeAverageThroughput_;
        }
    }

    public void setContactsInSnap(Collection<Atom> atoms) {
        Set<ResidueId> backbone = new HashSet<ResidueId>();
        Set<ResidueId> sideChain = new HashSet<ResidueId>();
        Set<ResidueId> ris = new HashSet<ResidueId>();
        for (Atom atom : atoms) {

            atoms_.add(atom);
            ResidueId ri = atom.getResidueId();
            if (!residueTouches_.containsKey(ri)) {
                residueTouches_.put(ri, new ClusterAtomContacts());
            }
            residueTouches_.get(ri).touch(atom);

            ris.add(ri);
            if (atom.isBackbone()) {
                backbone.add(ri);
            } else {
                sideChain.add(ri);
            }
        }

        for (ResidueId ri : backbone) {
            residueTouches_.get(ri).touchBackbone();
        }

        for (ResidueId ri : sideChain) {
            residueTouches_.get(ri).touchSideChain();
        }

        for (ResidueId ri : ris) {
            residueTouches_.get(ri).touchTotal();
        }
    }

    public Set<Atom> getAtoms() {
        return atoms_;
    }

    public Map<ResidueId, ClusterAtomContacts> getAtomTouches() {
        return residueTouches_;
    }

    public double getMaxBottleneck() {
        return maxBottleneck_;
    }

    private class CountComparator implements Comparator<ClusterAtomContacts> {

        @Override
        public int compare(ClusterAtomContacts a, ClusterAtomContacts b) {
            int c = -1 * new Integer(
                    a.getSideChainCount()).compareTo(
                    b.getSideChainCount());
            if (0 == c) {
                c = a.getResidueId().compareTo(b.getResidueId());
            }
            return c;
        }
    }

    public ClusterAtomContacts[] getResidueTouchesByNumber() {
        ClusterAtomContacts[] result = new ClusterAtomContacts[0];
        result = residueTouches_.values().toArray(result);
        Arrays.sort(result, new CountComparator());
        return result;
    }

    public int numTunnels() {
        return numTunnels_;
    }

    public double getBestThroughputPercent() {
        return bestThroughputPercent_;
    }

    public Distribution getBottleneckDistribution() {
        return bottleneckDistribution_;
    }

    public void drawHeatMap(double profileTunnelSampling, Point origin,
            Point voronoiOrigin,
            double resolution, int maxWidth, File pallette, File unknown,
            double heatMapLow, double heatMapHigh, int xZoom, int yZoom,
            File heatMapImage,
            File averageImage,
            File csv,
            File averageCsv) throws IOException {

        Map<SnapId, Tunnel> tunnels = cluster_.getTunnelsBySnapshots();

        double maxDist = 0;

        for (SnapId sid : snaps_) {
            if (tunnels.containsKey(sid)) {
                Tunnel t = tunnels.get(sid);
                List<Sphere> p = t.computeProfile(profileTunnelSampling);
                for (Sphere s : p) {
                    double dist = voronoiOrigin.distance(s.getS());
                    if (maxDist < dist) {
                        maxDist = dist;
                    }
                }
            }
        }

        int maxX = (int) Math.floor(maxDist / resolution);
        int height = maxX + 1;
        double[][] heat = new double[maxX + 1][snaps_.size()];

        double[][] averageHeat = new double[height][1];
        int[] counter = new int[height];

        for (int x = 0; x < heat.length; x++) {
            for (int y = 0; y < heat[x].length; y++) {
                heat[x][y] = MapDrawer.UNKNOWN;
            }
        }

        int y = 0;
        for (SnapId sid : snaps_) {


            if (tunnels.containsKey(sid)) {
                Tunnel t = tunnels.get(sid);

                List<Sphere> p = t.computeProfile(profileTunnelSampling);
                SortedMap<Integer, Double> column = new TreeMap<Integer, Double>();
                for (Sphere s : p) {
                    double dist = voronoiOrigin.distance(s.getS());
                    int x = (int) Math.floor(dist / resolution);
                    if (column.containsKey(x)) {
                        double r = column.get(x);
                        if (s.getR() < r) {
                            column.put(x, s.getR());
                        }
                    } else {
                        column.put(x, s.getR());
                    }
                }
                for (int x : column.keySet()) {
                    double v = column.get(x);
                    int i = heat.length - x - 1;
                    heat[i][y] = v;
                    averageHeat[i][0] += v;
                    counter[i]++;
                }
            }
            y++;
        }

        for (int i = 0; i < height; i++) {
            if (0 != counter[i]) {
                averageHeat[i][0] /= counter[i];
            } else {
                averageHeat[i][0] = MapDrawer.UNKNOWN;
            }
        }

        MapDrawer md = new MapDrawer(pallette, unknown);

        int width = snaps_.size();

        if (0 < width) {
            if (maxWidth < width) {
                int fold = (int) Math.ceil(width / maxWidth);
                double[][] h = new double[heat.length][maxWidth];
                for (int i = 0; i < maxWidth; i++) {
                    double[] s = new double[heat.length];
                    int[] c = new int[heat.length];
                    for (int k = 0; k < fold; k++) {
                        int index = i * fold + k;
                        if (index < width) {
                            for (int l = 0; l < heat.length; l++) {
                                if (MapDrawer.UNKNOWN != heat[l][index]) {
                                    s[l] += heat[l][index];
                                    c[l]++;
                                }
                            }
                        }
                    }
                    for (int l = 0; l < heat.length; l++) {
                        if (0 == c[l]) {
                            h[l][i] = MapDrawer.UNKNOWN;
                        } else {
                            h[l][i] = s[l] / c[l];
                        }
                    }
                }
                heat = h;
            }

            double[][] h = new double[heat.length][width + 1];
            for (int i = 0; i < heat.length; i++) {
                System.arraycopy(heat[i], 0, h[i], 1, heat[i].length);
            }
            for (int i = 0; i < h.length; i++) {
                h[i][0] = averageHeat[i][0];
            }
            heat = h;


            BufferedImage img = new BufferedImage(
                    (Math.min(width, maxWidth) + 1) * xZoom, height * yZoom,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();

            md.drawHeatMap(heat, heatMapLow, heatMapHigh, g, 0, 0, xZoom, yZoom);

            g.dispose();
            ImageIO.write(img, "png", heatMapImage);

            img = new BufferedImage(xZoom, height * yZoom,
                    BufferedImage.TYPE_INT_RGB);
            g = img.createGraphics();

            md.drawHeatMap(averageHeat, heatMapLow, heatMapHigh, g, 0, 0, xZoom, yZoom);

            g.dispose();
            ImageIO.write(img, "png", averageImage);

            BufferedWriter bw = new BufferedWriter(new FileWriter(averageCsv));
            for (int i = 0; i < heat.length; i++) {
                for (int j = 0; j < heat[i].length; j++) {

                    bw.write(heat[i][j] + ",");
                }
                bw.write("\n");
            }
            bw.close();


            bw = new BufferedWriter(new FileWriter(csv));
            for (int i = 0; i < height; i++) {
                double d = averageHeat[i][0];
                bw.write(d + ",");
            }
            bw.close();

        } else {
            Logger.getLogger("caver").log(Level.WARNING, "Heat map width is "
                    + "zero for " + "{0}" + ". Tunnels are too short,"
                    + " is the starting point inside the structure?",
                    heatMapImage);
        }

    }
}
