package algorithms.search;

import algorithms.clustering.layers.AverageSurface;
import algorithms.clustering.layers.LayeredTunnels;
import algorithms.triangulation.VE;
import algorithms.triangulation.VoronoiDiagram;
import caver.CalculationSettings;
import caver.Clock;
import caver.Printer;
import caver.tunnels.Tunnel;
import caver.tunnels.Tunnels;
import caver.ui.CalculationException;
import caver.util.CaverCounter;
import chemistry.pdb.SnapId;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
 * The class implements and algorithm for tunnel identification based on
 * Dijktra's algorithm performed on weighted edges of Voronoi Diagram.
 */
public class DijkstraTunnelComputation implements TunnelComputation {

    @Override
    /*
     * Identifies tunnels using an algorithm described in CAVER 3.0: A Tool for
     * the Analysis of Transport Pathways in Dynamic Protein Structures
     */
    public Tunnels computeTunnels(CalculationSettings cs, VoronoiDiagram vd,
            Point origin, double proteinR, SnapId snapId, CaverCounter counter)
            throws IOException, CalculationException {

        Printer.println("Voronoi nodes:" + vd.countNodes());

        Integer sourceNode = vd.getOptimizedOrigin(origin,
                cs.getDesiredRadius(),
                cs.getMaxDistance(),
                cs.getDefaultMaxDistance(),
                cs.getProbeRadius());

        if (null == sourceNode) {
            Logger.getLogger("caver").log(Level.WARNING, "Failed to optimize"
                    + " starting point with parameters desired_radius = {0}"
                    + " max_distance = {1}. Using closest Voronoi vertex.",
                    new Object[]{cs.getDesiredRadius(), cs.getMaxDistance()});
            sourceNode = vd.getClosestNode(origin);
        }

        if (null == sourceNode) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Starting point not found, maybe "
                    + "every site is accessible to the specified outer space "
                    + "probe?");
            throw new RuntimeException();
        }

        VoronoiDiagramSearches tp = new VoronoiDiagramSearches();

        double shellRadius;

        Double greatestBottleneck =
                tp.dijkstraGetBottleneck(vd, sourceNode, cs.getProbeRadius());
        Printer.println("Bottleneck: " + greatestBottleneck);

        if (cs.automaticShellRadius()) {
            shellRadius = greatestBottleneck * cs.getBottleneckMultiplier();
            Printer.println("Using automatic outer shell radius " + shellRadius);
        } else { // make sure outer will not reach starting point
            if (cs.getShellRadius() <= greatestBottleneck) {
                shellRadius = greatestBottleneck * cs.getBottleneckMultiplier();
                Logger.getLogger("caver").log(Level.WARNING, "Outer shell "
                        + "probe reached starting point, setting its"
                        + " radius to {0}", shellRadius);
            } else {
                shellRadius = cs.getShellRadius();
            }
        }
        cs.setShellRadius(shellRadius);

        Point voronoiOrigin = vd.getPoint(sourceNode);

        Printer.println("Using outer shell radius " + shellRadius);

        Clock.start("compute tunnels: remove outer shell");
        tp.markOuterShellNodes(vd, shellRadius, voronoiOrigin, cs.getShellDepth(),
                new Sphere(voronoiOrigin, cs.getOriginProtectionRadius()));
        Clock.stop("compute tunnels: remove outer shell");
        Printer.println("Voronoi nodes after outer remove:" + vd.countNodes());

        Clock.start("compute tunnels: remove inner shell");

        Printer.println("Using depth " + (cs.getShellDepth()));
        tp.markInnerShellNodes(vd, cs.getProbeRadius(), shellRadius,
                cs.getShellDepth(), cs.getOuterPdbFile(),
                cs.getInnerPdbFile(),
                new Sphere(voronoiOrigin, cs.getOriginProtectionRadius()),
                cs.isAdmin());
        Clock.stop("compute tunnels: remove inner shell");
        Printer.println("Voronoi nodes after inner remove:" + vd.countNodes());


        Clock.start("compute tunnels: rest tunnels 1");

        if (cs.generateVoronoi()) {
            vd.save(cs, cs.getVoronoiDiagramFile());
        }

        if (vd.disabled(sourceNode)) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Starting point is out. Check starting point "
                    + "position (" + "{0}" + ")."
                    + " If starting point is correct, "
                    + "increase shell_radius.", voronoiOrigin);
            throw new RuntimeException();
        }

        Clock.stop("compute tunnels: rest tunnels 1");
        Clock.start("compute tunnels: rest tunnels 2");

        List<List<Integer>> nodeTunnels = new ArrayList<List<Integer>>();
        List<Double> tunnelDistances = new ArrayList<Double>();

        Tunnels all = Tunnels.create(origin, voronoiOrigin, sourceNode, cs);

        double ri = proteinR / (cs.getWaypointCount() + 1); // last one is worthless

        Printer.println("Waipoint sampling distance: " + ri);

        for (int i = 1; i <= cs.getWaypointCount(); i++) {
            double r = i * ri;
            vd.initializeSearchTables();

            Set<Integer> waypoint = tp.dijkstraInBubble(
                    vd, sourceNode, cs.getProbeRadius(), r);

            for (int waypointI : waypoint) {

                double[] distances = vd.copyDistances();
                int[] previous = vd.copyPrevious();

                Set<Integer> innerEnds = tp.secondaryDijkstra(vd, waypointI,
                        cs.getProbeRadius(), distances, previous);

                for (int innerEnd : innerEnds) {

                    Integer outerEnd = tp.finalizingDijkstra(vd, innerEnd,
                            cs.getProbeRadius(), distances, previous);

                    if (null != outerEnd) {
                        List<Integer> nodeTunnel = new ArrayList<Integer>();

                        int back = outerEnd;
                        double w = distances[outerEnd];

                        while (vd.NULL != back) {
                            nodeTunnel.add(back);
                            back = previous[back];
                        }

                        nodeTunnels.add(nodeTunnel);
                        tunnelDistances.add(w);
                    }

                }
            }

            // process and cluster tunnels - too many for memory
            Tunnels tunnels = Tunnels.create(origin, voronoiOrigin, sourceNode,
                    cs);
            for (int t = 0; t < nodeTunnels.size(); t++) {
                List<Integer> nodeTunnel = nodeTunnels.get(t);

                if (2 <= nodeTunnel.size()) {
                    List<VE> segments = new ArrayList<VE>();
                    for (int j = 0; j < nodeTunnel.size() - 1; j++) {
                        int a = nodeTunnel.get(j);
                        int b = nodeTunnel.get(j + 1);
                        VE as = vd.getSegmentByNodes(a, b);
                        segments.add(as);
                    }

                    if (2 < segments.size()) {

                        double cost = tunnelDistances.get(t);
                        Tunnel tunnel = Tunnel.create(
                                origin, snapId,
                                counter.get(), segments, nodeTunnel,
                                cost,
                                cs.getShellRadius(),
                                cs.getProfileTunnelSamplingStep(), cs);
                        if (null != tunnel) {
                            tunnels.add(tunnel);
                        }
                    }
                }
            }


            nodeTunnels.clear();
            tunnelDistances.clear();

            if (cs.doFrameClustering()) {

                List<Tunnel> ts = tunnels.getTunnels();
                AverageSurface surface = null;
                if (cs.doAverageSurfaceGlobal()) {
                    surface = new AverageSurface(ts,
                            tunnels.getVoronoiOrigin(), cs);
                }

                LayeredTunnels lts = new LayeredTunnels(
                        tunnels.getVoronoiOrigin(),
                        surface, ts, // just tunnels for exact clutering
                        false, cs.doAverageSurfaceFrame(),
                        cs.getFrameLayersSettings(), cs);

                tunnels.cluster(lts, cs);
            }

            all.addAll(tunnels.getTunnels());


        }

        Clock.stop("compute tunnels: rest tunnels 2");
        Clock.start("compute tunnels: rest tunnels 3");

        Printer.println(all.size() + " found in second phase.");
        Clock.stop("compute tunnels: rest tunnels 3");
        return all;
    }

    @Override
    /*
     * Experimental solution of overshaddowing problem described in Eitan Yaffe,
     * Dan Fishelovitch, Haim J. Wolfson, Ruth Nussinov, Dan Halperin. "MolAxis:
     * A server for Identification of Channels in Macromolecule". Nucleic Acids
     * Research. Volume 36 (Web Server issue), 1 July 2008, Pages: W210-W215.
     *
     * Tunnels are constructed iteratively from starting points. Each iteration
     * identified tunnel parts in space between two spheres S1 and S2. In each
     * iteration, a Dijkstra's algorithm is used to find shortest path from each
     * tunnel endpoints from previous iteration (spheres S0 and S1, S0.r < S1.r
     * < S2.r) to a point beyond S2.
     *
     */
    public Tunnels computeTunnelsBlock(CalculationSettings cs, VoronoiDiagram vd,
            Point origin, SnapId snapId, CaverCounter counter) throws IOException {

        Printer.println("Voronoi nodes:" + vd.countNodes());

        Integer sourceNode = vd.getOptimizedOrigin(origin,
                cs.getDesiredRadius(),
                cs.getMaxDistance(),
                cs.getDefaultMaxDistance(), cs.getProbeRadius());

        if (null == sourceNode) {
            Logger.getLogger("caver").log(Level.WARNING, "Failed to optimize"
                    + " starting point with parameters desired_radius = {0}"
                    + " max_distance = {1}. Using closest Voronoi vertex.",
                    new Object[]{cs.getDesiredRadius(), cs.getMaxDistance()});
            sourceNode = vd.getClosestNode(origin);
        }

        if (null == sourceNode) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Starting point not found, maybe "
                    + "every site is accessible to the specified outer space "
                    + "probe?");
            throw new RuntimeException("Starting point not found, maybe "
                    + "every site is accessible to the specified outer space "
                    + "probe?");
        }

        VoronoiDiagramSearches tp = new VoronoiDiagramSearches();

        double shellRadius;


        Double bottleneck =
                tp.dijkstraGetBottleneck(vd, sourceNode, cs.getProbeRadius());
        Printer.println("Bottleneck: " + bottleneck);

        if (cs.automaticShellRadius()) {
            shellRadius = bottleneck * cs.getBottleneckMultiplier();
            Printer.println("Using automatic outer shell radius " + shellRadius);
        } else { // make sure outer will not reach starting point
            if (cs.getShellRadius() <= bottleneck) {
                shellRadius = bottleneck * cs.getBottleneckMultiplier();
                Logger.getLogger("caver").log(Level.WARNING, "Outer shell "
                        + "probe reached starting point, setting its"
                        + " radius to {0}", shellRadius);
            } else {
                shellRadius = cs.getShellRadius();
            }
        }
        cs.setShellRadius(shellRadius);

        Point voronoiOrigin = vd.getPoint(sourceNode);

        Printer.println("Using outer shell radius " + shellRadius);

        Clock.start("compute tunnels: remove outer shell");
        tp.markOuterShellNodes(vd, shellRadius, voronoiOrigin, cs.getShellDepth(),
                new Sphere(voronoiOrigin, cs.getOriginProtectionRadius()));
        Clock.stop("compute tunnels: remove outer shell");
        Printer.println("Voronoi nodes after outer remove:" + vd.countNodes());

        Clock.start("compute tunnels: remove inner shell");

        Clock.start("compute tunnels: rest tunnels 1");

        Printer.println("Using depth " + (cs.getShellDepth()));
        tp.markInnerShellNodes(vd, cs.getProbeRadius(), shellRadius,
                cs.getShellDepth(), cs.getOuterPdbFile(),
                cs.getInnerPdbFile(),
                new Sphere(voronoiOrigin, cs.getOriginProtectionRadius()), cs.isAdmin());
        Clock.stop("compute tunnels: remove inner shell");
        Printer.println("Voronoi nodes after inner remove:" + vd.countNodes());

        if (cs.generateVoronoi() && cs.isAdmin()) {
            vd.save(cs, cs.getVoronoiDiagramFile());
        }

        if (vd.disabled(sourceNode)) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Starting point is out. Check starting point "
                    + "position (" + "{0}" + ")."
                    + " If starting point is correct, "
                    + "increase shell_radius.", voronoiOrigin);
            throw new RuntimeException();
        }

        Logger.getLogger("caver").log(Level.FINE, "User origin and Voronoi origin are distant {0}", origin.distance(voronoiOrigin));

        Clock.stop("compute tunnels: rest tunnels 1");
        Clock.start("compute tunnels: rest tunnels 2");

        vd.initializeSearchTables();
        tp.dijkstra(vd, sourceNode, cs.getProbeRadius());

        List<List<Integer>> nodeTunnels = new ArrayList<List<Integer>>();
        List<Double> tunnelDistances = new ArrayList<Double>();


        Set<Integer> outerNodes = vd.getOuterNodes();
        for (int endNode : outerNodes) {
            if (vd.NULL != vd.getPrevious(endNode)) {
                // route to this endNode was found

                assert vd.getDistance(endNode) > 0;

                double[] distances = vd.copyDistances();
                int[] previous = vd.copyPrevious();

                Integer out = tp.finalizingDijkstra(vd, endNode,
                        cs.getProbeRadius(), distances, previous);

                if (null != out) {
                    List<Integer> nodeTunnel = new ArrayList<Integer>();
                    int back = out;
                    double w = distances[out];

                    while (vd.NULL != back) {
                        nodeTunnel.add(back);
                        back = previous[back];
                    }

                    nodeTunnels.add(nodeTunnel);
                    tunnelDistances.add(w);

                }
            }
        }
        Printer.println(nodeTunnels.size() + " found in first phase.");

        Clock.stop("compute tunnels: rest tunnels 2");
        Clock.start("compute tunnels: rest tunnels 3");

        Tunnels tunnels = Tunnels.create(origin, voronoiOrigin, sourceNode, cs);

        for (int t = 0; t < nodeTunnels.size(); t++) {

            List<Integer> nodeTunnel = nodeTunnels.get(t);

            if (2 <= nodeTunnel.size()) {
                List<VE> segments = new ArrayList<VE>();
                for (int i = 0; i < nodeTunnel.size() - 1; i++) {
                    int a = nodeTunnel.get(i);
                    int b = nodeTunnel.get(i + 1);
                    VE as = vd.getSegmentByNodes(a, b);
                    segments.add(as);
                }

                if (2 < segments.size()) {
                    double cost = tunnelDistances.get(t);

                    Tunnel tunnel = Tunnel.create(
                            origin, snapId,
                            counter.get(), segments, nodeTunnel,
                            cost,
                            cs.getShellRadius(),
                            cs.getProfileTunnelSamplingStep(), cs);
                    if (null != tunnel) {
                        tunnels.add(tunnel);
                    }

                }
            }

        }
        Printer.println(tunnels.size() + " found in second phase.");
        Clock.stop("compute tunnels: rest tunnels 3");
        return tunnels;
    }
}
