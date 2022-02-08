package caver.ui;

import algorithms.clustering.Cluster;
import algorithms.clustering.Clusters;
import algorithms.clustering.layers.SphereSegmentIntersection;
import algorithms.clustering.statistics.ClusterAtomContacts;
import algorithms.clustering.statistics.ClusterStatistics;
import algorithms.clustering.statistics.Histogram;
import caver.CalculationSettings;
import caver.Printer;
import caver.tunnels.Tunnel;
import caver.tunnels.TunnelCsvComparator;
import caver.ui.graphics.MapDrawer;
import chemistry.*;
import chemistry.pdb.PdbFileProcessor;
import chemistry.pdb.SnapId;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;


/*
 * Class for computation of statistics for clusters and tunnels through time.
 */
public class Statistics {

    CalculationSettings cs_;
    private static final String sep = ", ";

    public Statistics(CalculationSettings cs) {
        cs_ = cs;
    }

    public void saveProfileHeatMaps(SortedSet<SnapId> snaps, Clusters clusters,
            Point origin, Point voronoiOrigin)
            throws IOException {
        SortedSet<Cluster> clusterSet = clusters.computePriorities();

        for (Cluster c : clusterSet) {

            c.getStatistics().drawHeatMap(cs_.getProfileTunnelSamplingStep(),
                    origin, voronoiOrigin,
                    cs_.getProfileHeatMapResolution(),
                    cs_.getMaxHeatMapWidth(),
                    cs_.getPallette(), cs_.getUnknownColor(),
                    cs_.getHeatMapLow(), cs_.getHeatMapHigh(),
                    cs_.getZoomX(), cs_.getZoomY(),
                    cs_.getProfileHeatMapImage(c.getPriority()),
                    cs_.getProfileAverageImage(c.getPriority()),
                    cs_.getProfileAverageCsv(c.getPriority()),
                    cs_.getProfileHeatMapCsv(c.getPriority()));
        }
    }

    public void saveBottleneckHeatMap(SortedSet<SnapId> snaps, Clusters clusters,
            Point origin, Point voronoiOrigin)
            throws IOException {
        SortedSet<Cluster> clusterSet = clusters.computePriorities();

        drawBottleneckHeatMap(clusterSet, snaps,
                cs_.getProfileTunnelSamplingStep(),
                origin, voronoiOrigin,
                cs_.getBottleneckHeatMapResolution(),
                cs_.getBottleneckPallette(), cs_.getBottleneckUnknownColor(),
                cs_.getBottleneckHeatMapLow(), cs_.getBottleneckHeatMapHigh(),
                cs_.getBottleneckZoomX(), cs_.getBottleneckZoomY(),
                cs_.getBottleneckHeatMapImage(),
                cs_.getBottleneckHeatMapCsv());
    }

    public void saveSummary(Clusters clusters,
            File file)
            throws IOException {
        SortedSet<Cluster> clusterSet = clusters.computePriorities();
        PrintStream p = new PrintStream(new FileOutputStream(file));

        for (String s : cs_.getSummaryComments()) {
            p.println(s);
        }

        p.println();

        p.println(ClusterStatistics.getHeaderLine());
        for (Cluster c : clusterSet) {
            p.println(c.getStatistics().getValueLine());
        }
        p.close();

    }

    public void savePreciseSummary(Clusters clusters, File preciseFile)
            throws IOException {
        SortedSet<Cluster> clusterSet = clusters.computePriorities();
        PrintStream p = new PrintStream(new FileOutputStream(preciseFile));
        p.println("Tunnel cluster ID" + sep
                + "Priority" + sep
                + "Average throughput" + sep
                + "Standard deviation");
        for (Cluster c : clusterSet) {

            p.print(c.getPriority());
            p.print(sep);

            p.print(c.getStatistics().getPriority());
            p.print(sep);

            p.print(c.getStatistics().getThroughputDistribution().getAverage());
            p.print(sep);

            p.print(c.getStatistics().getThroughputDistribution().
                    getStandardDeviation());
            p.print(sep);
            p.println();
        }
        p.close();
    }

    public void computeRadiusErrorBound(Clusters clusters) {

        SortedMap<SnapId, File> files = cs_.getTrajectoryFiles(
                cs_.getTimeSparsity(), cs_.getFirstFrame(), cs_.getLastFrame());

        int fails = 0;
        double sqrt3 = Math.sqrt(3);

        for (SnapId snap : files.keySet()) {

            File file = files.get(snap);
            PdbFileProcessor pfp = cs_.getPdbFileProcessor(file);
            MolecularSystem ms = cs_.createMolecularSystem(pfp);

            for (Cluster c : clusters.getClusters()) {
                for (Tunnel t : c.getTunnels(snap)) {

                    List<Sphere> profile = t.computeProfile(
                            cs_.getProfileTunnelSamplingStep());
                    double[] errors = new double[profile.size()];

                    for (int i = 0; i < profile.size(); i++) {
                        Sphere s = profile.get(i);
                        Set<Atom> atoms = ms.getAtomsWithinDistanceFromPoint(
                                s.getS(),
                                s.getR() + sqrt3 * cs_.getMaxShatter());

                        // distance of sphere surfaces, minus if they collide
                        double minDist = Double.MAX_VALUE;
                        for (Atom a : atoms) {
                            double dist = s.getS().distance(a.getCenter())
                                    - a.getRadius() - s.getR();
                            if (dist < minDist) {
                                minDist = dist;
                            }
                        }

                        double err = (-1) * minDist;
                        if (err < 0) {
                            // no sphere may be found because of shatter
                            // and favourable approximation rotation
                            if (err < (-1) * sqrt3 * cs_.getMaxShatter()) {
                                // should never happen
                                Printer.warn("Error estimate might be imprecise: "
                                        + err);
                                fails++;
                            }

                            err = 0;
                        }
                        errors[i] = err;
                    }
                    t.processErrors(profile, errors, cs_.saveErrorProfiles());
                }
            }
        }
        if (cs_.computeErrors()) {
            for (Cluster c : clusters.getClusters()) {
                c.getStatistics().calculateErrorBounds();
            }
        }
        if (0 < fails) {
            Printer.warn("Error estimate fails: " + fails);
        }
    }

    /*
     * Origin - should be common to all tunnels, i.e. averaged.
     */
    public void saveTunnelProfiles(Clusters clusters, Point origin)
            throws IOException {

        SortedMap<Tunnel, List<String>> map =
                new TreeMap<Tunnel, List<String>>(new TunnelCsvComparator());

        for (Cluster c : clusters.computePriorities()) {
            for (Tunnel t : c.getTunnelsBySnapshotAndCost()) {

                List<Sphere> profile = t.computeProfile(
                        cs_.getProfileTunnelSamplingStep());
                List<Double> lengths = new ArrayList<Double>();

                for (int i = 0; i < profile.size(); i++) {
                    Sphere s = profile.get(i);
                    if (0 == i) {
                        lengths.add(origin.distance(s.getS()));
                    } else {
                        lengths.add(lengths.get(lengths.size() - 1)
                                + cs_.getProfileTunnelSamplingStep());
                    }
                }

                List<String> list = new ArrayList<String>();
                StringBuilder sb = new StringBuilder();

                Double ae = t.getAverageError();
                String aes;
                if (null == ae) {
                    aes = "-";
                } else {
                    aes = ae.toString();
                }

                Double me = t.getMaxError();
                String mes;
                if (null == me) {
                    mes = "-";
                } else {
                    mes = me.toString();
                }

                String prefix = t.getSnapId() + sep
                        + c.getPriority() + sep
                        + t.getPriority() + sep
                        + t.getThroughput() + sep
                        + t.getCost() + sep
                        + t.getBottleneck().getR() + sep
                        + aes + sep
                        + mes + sep
                        + ds(t.getBottleneckError()) + sep
                        + t.getCurvature() + sep
                        + t.getLength() + sep + sep;

                sb.append(prefix).append("X");
                for (Sphere s : profile) {
                    sb.append(sep).append(s.getS().getX());
                }
                list.add(sb.toString());

                sb = new StringBuilder();
                sb.append(prefix).append("Y");
                for (Sphere s : profile) {
                    sb.append(sep).append(s.getS().getY());
                }
                list.add(sb.toString());

                sb = new StringBuilder();
                sb.append(prefix).append("Z");
                for (Sphere s : profile) {
                    sb.append(sep).append(s.getS().getZ());
                }
                list.add(sb.toString());

                sb = new StringBuilder();
                sb.append(prefix).append("distance");
                for (Sphere s : profile) {
                    sb.append(sep).append(origin.distance(s.getS()));
                }
                list.add(sb.toString());

                sb = new StringBuilder();
                sb.append(prefix).append("length");
                for (double l : lengths) {
                    sb.append(sep).append(l);
                }
                list.add(sb.toString());

                sb = new StringBuilder();
                sb.append(prefix).append("R");
                double min = Double.MAX_VALUE;
                for (Sphere s : profile) {
                    sb.append(sep).append(s.getR());
                    if (s.getR() < min) {
                        min = s.getR();
                    }
                }
                list.add(sb.toString());

                sb = new StringBuilder();
                sb.append(prefix).append("Upper limit of R overestimation");

                if (null == t.getErrorProfile()) {
                    for (int i = 0; i < profile.size(); i++) {
                        sb.append(sep).append("-");
                    }

                } else {

                    for (double d : t.getErrorProfile()) {
                        sb.append(sep).append(d);
                    }
                }
                list.add(sb.toString());

                map.put(t, list);

            }

        }

        PrintStream p = new PrintStream(new FileOutputStream(
                cs_.getTunnelProfilesFile()));

        p.println("Snapshot" + sep + "Tunnel cluster"
                + sep + "Tunnel" + sep
                + "Throughput" + sep
                + "Cost" + sep
                + "Bottleneck radius" + sep
                + "Average R error bound" + sep
                + "Max. R error bound" + sep
                + "Bottleneck R error bound" + sep
                + "Curvature" + sep + "Length" + sep + sep
                + "Axis" + sep + "Values...");

        for (List<String> list : map.values()) {
            for (String s : list) {
                p.println(s);
            }
        }

        p.close();

    }

    private String ds(Double d) {
        if (null == d) {
            return "-";
        } else {
            return d.toString();
        }
    }

    ;

    public void saveTunnelCharacteristics(Clusters clusters) throws IOException {
        PrintStream p = new PrintStream(new FileOutputStream(
                cs_.getTunnelCharacteristicsFile()));
        p.println("Snapshot" + sep
                + "Tunnel cluster" + sep
                + "Tunnel" + sep
                + "Throughput" + sep
                + "Cost" + sep
                + "Bottleneck radius" + sep
                + "Bottleneck R error bound" + sep
                + "Length" + sep
                + "Curvature");

        SortedMap<Tunnel, String> map =
                new TreeMap<Tunnel, String>(new TunnelCsvComparator());


        for (Cluster c : clusters.computePriorities()) {
            for (Tunnel t : c.getTunnelsBySnapshotAndCost()) {

                StringBuilder sb = new StringBuilder();

                sb.append(t.getSnapId());
                sb.append(sep);
                sb.append(t.getCluster().getPriority());
                sb.append(sep);
                sb.append(t.getPriority());
                sb.append(sep);
                sb.append(t.getThroughput());
                sb.append(sep);
                sb.append(t.getCost());
                sb.append(sep);
                sb.append(t.getBottleneck().getR());
                sb.append(sep);
                sb.append(ds(t.getBottleneckError()));
                sb.append(sep);
                sb.append(t.getLength());
                sb.append(sep);
                sb.append(t.getCurvature());

                map.put(t, sb.toString());

            }
        }

        for (String s : map.values()) {
            p.println(s);
        }
        p.close();
    }

    /*
     * Atom b do not intersects cylinder s-a, i.e. a is visible from s
     */
    public boolean visible(Sphere sphere, Atom a, Atom b) {

        Point p1 = sphere.getS();
        Point p2 = a.getCenter();

        p2 = p2.plus(
                p1.minus(p2).normalize().multiply(
                a.getRadius() + cs_.getVisibilityProbeRadius()));

        Point s = b.getCenter();
        double r = b.getRadius();
        Point[] ps = SphereSegmentIntersection.sphereSegmentIntersection(
                p1, p2, s, r + cs_.getVisibilityProbeRadius());

        if (0 == ps.length) {
            return true;
        } else {
            return false;


        }
    }

    public void saveResidueClusterTouches(Clusters clusters) throws IOException {

        SortedMap<SnapId, File> files = cs_.getTrajectoryFiles(
                cs_.getTimeSparsity(), cs_.getFirstFrame(), cs_.getLastFrame());

        for (SnapId snap : files.keySet()) {

            File file = files.get(snap);
            PdbFileProcessor pfp = cs_.getPdbFileProcessor(file);

            MolecularSystem ms = cs_.createMolecularSystem(pfp);
            for (Cluster c : clusters.getClusters()) {
                // contacts of cluster in snapshot
                Set<Atom> visible = new HashSet<Atom>();
                for (Tunnel t : c.getTunnels(snap)) {
                    // what atoms are within contact distance?
                    Set<Atom> ta = new HashSet<Atom>();
                    List<Sphere> ts = t.computeProfile(
                            cs_.getProfileTunnelSamplingStep());
                    for (Sphere s : ts) {
                        Set<Atom> atoms = ms.getAtomsWithinDistanceFromPoint(
                                s.getS(), s.getR() + cs_.getContactDistance());
                        for (Atom a : atoms) {
                            ta.add(a);
                        }
                    }

                    // are they also visible from any tunnel sphere within
                    // contact distance?
                    if (cs_.checkVisibility()) {

                        AtomSearchTree tree = new AtomSearchTree(ta);
                        for (Sphere s : ts) {

                            Set<Atom> close =
                                    tree.getAtomsWithinDistanceFromPoint(
                                    s.getS(),
                                    s.getR() + cs_.getContactDistance());

                            for (Atom a : close) {
                                boolean atomIsVisible = true;
                                for (Atom b : close) {
                                    if (a.equals(b)) {
                                        continue;
                                    }

                                    if (!visible(s, a, b)) {
                                        atomIsVisible = false;
                                        break;
                                    }
                                }
                                if (atomIsVisible) {
                                    visible.add(a);
                                }
                            }
                        }
                    } else {
                        visible.addAll(ta);
                    }
                }
                c.getStatistics().setContactsInSnap(visible);
            }
        }

        PrintStream p = new PrintStream(new FileOutputStream(
                cs_.getResidueFile()));

        p.println("# Tunnel-lining resiudes (residues wihtin the "
                + cs_.getContactDistance() + " A distnace from individual"
                + " tunnels)");
        p.println("# Column content");
        p.println("# C:     chain");
        p.println("# res:   residue index in PDB file");
        p.println("# AA:    amino acid");
        p.println("# N:     number of snapshots in which the residue lined the tunnel");
        p.println("# sideN: same as N, but backbone atoms are not counted (those named H, N, C, O, CA or HA)");
        p.println("# atoms: tunnel-lining atoms. Format is <number of snapshots>:<atom name>_<atom serial number>");
        p.println("#");


        for (Cluster c : clusters.computePriorities()) {
            p.println("== Tunnel cluster " + c.getPriority() + " ==");
            p.println(String.format("%3s %6s %3s %7s %7s %s",
                    "# C",
                    "res",
                    "AA",
                    "N",
                    "sideN",
                    "atoms"));
            for (ClusterAtomContacts rt :
                    c.getStatistics().getResidueTouchesByNumber()) {
                ResidueId ri = rt.getResidueId();
                p.println(String.format(
                        "  %1s %6s %3s %7d %7d %s",
                        ri.getChain(),
                        (ri.getSequenceNumber() + ri.getInsertionCode()).trim(),
                        rt.getResidueName(),
                        rt.getTotalCount(),
                        rt.getSideChainCount(),
                        rt.getAtomCounts()));
            }
        }
        p.close();

        p = new PrintStream(new FileOutputStream(
                cs_.getAtomsFile()));

        p.println("# Tunnel-lining atoms (atoms wihtin the "
                + cs_.getContactDistance()
                + " A distnace from individual tunnels)");

        for (Cluster c : clusters.computePriorities()) {

            p.println("== Tunnel cluster " + c.getPriority() + " ==");
            for (Atom a : c.getStatistics().getAtoms()) {
                p.print(a.getSerialNumber() + " ");
            }
            p.println();


        }
        p.close();

        PrintStream py = new PrintStream(new FileOutputStream(
                cs_.getAtomSelectionsScript()));

        for (Cluster c : clusters.computePriorities()) {

            String name = "T_" + c.getPriority() + "_atoms";
            py.println("cmd.select('" + name + "','none')");
            for (Atom a : c.getStatistics().getAtoms()) {
                py.println("cmd.select('" + name + "','" + name + " id "
                        + a.getSerialNumber() + " & structure')");
            }
        }
        py.close();

        Printer.println("... done.");
    }

    public void saveBottleneckResidues(Clusters clusters) throws IOException {
        List<Tunnel> tunnels = clusters.getTunnels();

        char s = ',';
        Collections.sort(tunnels, new TunnelCsvComparator());

        SortedMap<SnapId, File> files = cs_.getTrajectoryFiles(
                cs_.getTimeSparsity(), cs_.getFirstFrame(), cs_.getLastFrame());

        PrintStream p = new PrintStream(new FileOutputStream(
                cs_.getBottleneckResidueFile()));

        p.println("Bottleneck residues:, Residues in distance <= "
                + cs_.getBottleneckContactDistance()
                + " A from the bottleneck, sorted from the closest.");


        StringBuilder sb = new StringBuilder();
        sb.append("Snapshot");
        sb.append(s);
        sb.append("Tunnel cluster");
        sb.append(s);
        sb.append("Tunnel");
        sb.append(s);
        sb.append("Throughput");
        sb.append(s);
        sb.append("Cost");
        sb.append(s);
        sb.append("Bottleneck X");
        sb.append(s);
        sb.append("Bottleneck Y");
        sb.append(s);
        sb.append("Bottleneck Z");
        sb.append(s);
        sb.append("Bottleneck R");
        sb.append(s);
        sb.append("Bottleneck residues");
        p.println(sb);

        SortedMap<Tunnel, String> map =
                new TreeMap<Tunnel, String>(new TunnelCsvComparator());

        for (SnapId snap : files.keySet()) {

            File file = files.get(snap);
            PdbFileProcessor pfp = cs_.getPdbFileProcessor(file);

            MolecularSystem ms = cs_.createMolecularSystem(pfp);

            for (Tunnel t : tunnels) {
                if (t.getSnapId().equals(snap)) {


                    sb = new StringBuilder();
                    sb.append(t.getSnapId());
                    sb.append(s);
                    sb.append(t.getCluster().getPriority());
                    sb.append(s);
                    sb.append(t.getPriority());
                    sb.append(s);
                    sb.append(t.getThroughput());
                    sb.append(s);
                    sb.append(t.getCost());
                    sb.append(s);

                    List<Residue> rs = t.computeBottleneckResidues(ms,
                            cs_.getBottleneckContactDistance());

                    Sphere min = t.getBottleneck();
                    sb.append(min.getS().getX());
                    sb.append(s);
                    sb.append(min.getS().getY());
                    sb.append(s);
                    sb.append(min.getS().getZ());
                    sb.append(s);
                    sb.append(min.getR());


                    for (Residue r : rs) {
                        sb.append(s);
                        sb.append(r.getId().toString());
                    }

                    map.put(t, sb.toString());

                }
            }
        }
        for (String line : map.values()) {
            p.println(line);
        }

        p.close();
    }

    public void saveThroughputStatistics(Clusters clusters) throws IOException {

        PrintStream p = new PrintStream(new FileOutputStream(
                cs_.getThroughtputStatistics()));
        p.print("Cluster,"
                + "Priority,"
                + "Throughput average,"
                + "Throughputs");
        p.println();
        for (Cluster c : clusters.computePriorities()) {
            p.print(c.getPriority());
            p.print(sep + c.getStatistics().getRelativeAverageThroughput());
            for (double d : c.getStatistics().getThroughputs()) {
                p.print(sep + d);
            }
            p.println();
        }

        p.close();
    }

    public void saveThroughputHistograms(Clusters clusters) throws IOException {


        PrintStream p = new PrintStream(new FileOutputStream(
                cs_.getThroughtputHistograms()));
        p.print("Tunnel cluster" + sep
                + "Average throughput" + sep
                + "Standard deviation" + sep
                + "Out of histogram range" + sep
                + "Histogram");

        List<Double> bs = cs_.getThroughputHistogram().getBorders();
        for (int i = 0; i < bs.size() - 1; i++) {
            p.print(sep + f(bs.get(i)) + "_" + f(bs.get(i + 1)));
        }
        p.println();

        for (Cluster c : clusters.computePriorities()) {
            p.print(c.getPriority());
            p.print(sep + c.getStatistics().getThroughputDistribution().
                    getAverage());
            p.print(sep + c.getStatistics().getThroughputDistribution().
                    getStandardDeviation());

            List<Integer> h = new ArrayList<Integer>();
            int outside = computeHistogram(c.getStatistics().getThroughputs(),
                    cs_.getThroughputHistogram(), h);

            p.print(sep + outside + sep);
            for (double d : h) {
                p.print(sep + d);
            }
            p.println();
        }

        p.close();
    }

    public void saveBottleneckHistograms(Clusters clusters) throws IOException {

        PrintStream p = new PrintStream(new FileOutputStream(
                cs_.getBottleneckHistograms()));
        p.print("Tunnel cluster" + sep
                + "Average bottleneck radius" + sep
                + "Standard deviation" + sep
                + "Out of histogram range" + sep
                + "Histogram");
        List<Double> bs = cs_.getBottleneckHistogram().getBorders();
        for (int i = 0; i < bs.size() - 1; i++) {
            p.print(sep + f(bs.get(i)) + "_" + f(bs.get(i + 1)));
        }
        p.println();
        for (Cluster c : clusters.computePriorities()) {
            p.print(c.getPriority());
            p.print(sep + c.getStatistics().getBottleneckDistribution().
                    getAverage());
            p.print(sep + c.getStatistics().getBottleneckDistribution().
                    getStandardDeviation());

            Collection<Tunnel> ts = c.getCheapestTunnelFromEachSnapshot().values();

            Iterator<Tunnel> it = ts.iterator();
            double[] bottlenecks = new double[ts.size()];
            for (int i = 0; i < ts.size(); i++) {
                Tunnel t = it.next();
                bottlenecks[i] = t.getBottleneck().getR();
            }

            List<Integer> h = new ArrayList<Integer>();
            int outside = computeHistogram(
                    bottlenecks, cs_.getBottleneckHistogram(), h);

            p.print(sep + outside + sep);
            for (double d : h) {
                p.print(sep + d);
            }

            p.println();
        }

        p.close();
    }

    /*
     * Returns number of values outside the scope of the histogram. The
     * histogram itself is returned in output variable histogram.
     */
    public static int computeHistogram(double[] values,
            Histogram hp, List<Integer> histogram) {

        int outside = 0;


        for (int i = 0; i < hp.getN(); i++) {
            histogram.add(0);
        }

        for (double d : values) {
            int index = (int) Math.floor((d - hp.getLeft())
                    / ((hp.getRight() - hp.getLeft()) / hp.getN()));
            if (0 <= index && index < histogram.size()) {
                histogram.set(index, histogram.get(index) + 1);
            } else {
                outside++;
            }
        }

        return outside;


    }

    private static double f(double d) {
        return (double) Math.round(d * 100) / 100;
    }

    public void drawBottleneckHeatMap(SortedSet<Cluster> clusters,
            SortedSet<SnapId> snaps,
            double profileTunnelSampling, Point origin,
            Point voronoiOrigin,
            double resolution, File pallette, File unknown,
            double heatMapLow, double heatMapHigh, int xZoom, int yZoom,
            File heatMapImage,
            File heatMapCsv) throws IOException {


        int cn = clusters.size();
        if (1000 < cn) {
            cn = 1000;
        }

        double[][] heat = new double[cn][snaps.size()];
        {
            int y = 0;
            for (Cluster cluster : clusters) {
                Map<SnapId, Tunnel> tunnels = cluster.getTunnelsBySnapshots();

                int x = 0;
                for (SnapId sid : snaps) {
                    double v = MapDrawer.UNKNOWN;
                    if (tunnels.containsKey(sid)) {
                        Tunnel t = tunnels.get(sid);
                        v = t.getBottleneck().getR();
                    }
                    heat[y][x] = v;
                    x++;
                }
                y++;
            }
        }

        MapDrawer md = new MapDrawer(pallette, unknown);

        BufferedImage img = new BufferedImage(
                snaps.size() * xZoom, cn * yZoom,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        md.drawHeatMap(heat, heatMapLow, heatMapHigh, g, 0, 0, xZoom, yZoom);

        g.dispose();
        ImageIO.write(img, "png", heatMapImage);


        BufferedWriter bw = new BufferedWriter(new FileWriter(heatMapCsv));
        for (int y = 0; y < cn; y++) {
            for (int x = 0; x < snaps.size(); x++) {
                bw.write(heat[y][x] + ",");
            }
            bw.write("\n");
        }
        bw.close();
    }
}
