package algorithms.search;

import algorithms.triangulation.Tetrahedron;
import algorithms.triangulation.VoronoiDiagram;
import caver.Clock;
import chemistry.pdb.PdbUtil;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Class associating algorithms for searches in Voronoi Diagram.
 */
public class VoronoiDiagramSearches {

    /*
     * Disables all nodes accessible from outside. Outside is defined as set of
     * nodes with cardinality lower than 4.
     */
    public void markOuterShellNodes(VoronoiDiagram vd, double shellRadius,
            Point origin, double depth, Sphere originZone) {
        // Breadth-first search of space reachable by spherical probe with shellRadius_
        LinkedList<Integer> fifo = new LinkedList<Integer>();

        Set<Integer> convexBorder = vd.getOuterNodes();

        Logger.getLogger("caver").log(Level.FINE, "Outer nodes: {0}",
                convexBorder.size());

        for (int i : convexBorder) {
            fifo.push(i);
        }
        int counter = 0;
        while (!fifo.isEmpty()) {
            int node = fifo.poll();
            vd.disable(node); // removing all nodes accessible from outer space
            vd.makeOuter(node);
            counter++;
            for (int i = 0; i < 4; i++) {
                // don't return to removed nodes
                int neighbour = vd.getNeighbour(node, i);
                if (shellRadius <= vd.getBottleneckByGate(node, i)
                        && !vd.disabled(neighbour)) {
                    Point v = vd.getPoint(neighbour);
                    if (0 < originZone.distance(v)) {
                        fifo.push(neighbour);
                    }
                }
            }
        }
        Logger.getLogger("caver").log(Level.FINE, "Outer nodes disabled: {0}",
                counter);
    }

    /*
     * Marks the nodes which are inside outer shell sphere.
     */
    public void markInnerShellNodes(
            VoronoiDiagram vd, double probeR,
            double outerR, double depth, File outerPdb, File innerPdb,
            Sphere originZone, boolean saveSurface) {

        Set<Integer> outer = vd.getDisabledNeighbouringEnabledNodes();

        if (saveSurface) {

            Set<Sphere> spheresOuter = new HashSet<Sphere>();
            for (int node = 0; node < vd.size(); node++) {
                if (!vd.disabled(node)) {
                    boolean border = false;
                    for (int n : vd.getNeighbours(node)) {
                        if (vd.disabled(n)) {
                            border = true;
                        }
                    }

                    if (border) {
                        spheresOuter.add(vd.getSphere(node));
                    }

                }
            }
            try {
                PdbUtil.saveSpheres(spheresOuter, outerPdb);
            } catch (IOException e) {
                Logger.getLogger("caver").log(Level.WARNING, "", e);
            }

        }

        Set<Integer> inner = new HashSet<Integer>();

        for (int o : outer) {
            LinkedList<Integer> fifo = new LinkedList<Integer>();
            fifo.push(o);
            Sphere s = vd.getSphere(o);

            while (!fifo.isEmpty()) {

                int t = fifo.poll();
                vd.disable(t);
                if (saveSurface) {
                    inner.add(t);
                }
                for (int i = 0; i < 4; i++) {
                    int u = vd.getNeighbour(t, i);
                    if (!vd.disabled(u)) {
                        double d = s.distance(vd.getPoint(u));
                        if (probeR <= vd.getBottleneckByGate(t, i)
                                && d + probeR < depth) {
                            // accessible by probe not too deep 
                            // under outer space

                            if (0 < originZone.distance(vd.getPoint(u))) {
                                fifo.push(u);
                            }
                        }
                    }
                }
            }
        }

        if (saveSurface) {
            Set<Sphere> spheresInner = new HashSet<Sphere>();
            for (int node = 0; node < vd.size(); node++) {
                if (!vd.disabled(node)) {
                    boolean border = false;
                    for (int n : vd.getNeighbours(node)) {
                        if (vd.disabled(n)) {
                            border = true;
                        }
                    }

                    if (border) {
                        spheresInner.add(vd.getSphere(node));
                    }

                }
            }
            try {
                PdbUtil.saveSpheres(spheresInner, innerPdb);
            } catch (IOException e) {
                Logger.getLogger("caver").log(Level.WARNING, "", e);
            }
        }

    }

    /*
     * Search paths in vd from sourceNode that are wider than minRadius.
     */
    public void dijkstra(VoronoiDiagram vd, int sourceNode, double minRadius) {
        Clock.start("dijkstra");
        //long time1 = new Date().getTime();
        int operations = 0;
        int visited = 0;
        vd.setDistance(sourceNode, 0);
        SortedMap<Double, List<Integer>> q =
                new TreeMap<Double, List<Integer>>();
        q.put(0.0, new ArrayList<Integer>());
        q.get(0.0).add(sourceNode);

        while (!q.isEmpty()) {
            List<Integer> list = q.get(q.firstKey());
            if (list.isEmpty()) {
                break;
            }
            Integer u = list.iterator().next(); // TODO remove here
            if (1 == list.size()) {
                q.remove(q.firstKey());
            } else {
                list.remove(u);
            }
            for (int i = 0; i < 4; i++) {

                int v = vd.getNeighbour(u, i);

                if (!vd.disabled(v) && minRadius
                        <= vd.getBottleneckByGate(u, i)) {
                    double alt = vd.getDistance(u) + vd.getWeight(u, i);

                    if (alt < vd.getDistance(v)) {
                        operations++;
                        vd.setDistance(v, alt);
                        visited++;
                        vd.setPrevious(v, u);

                        if (!q.containsKey(alt)) {
                            q.put(alt, new ArrayList<Integer>());
                        }
                        q.get(alt).add(v);
                    }

                }
            }
        }

        Clock.stop("dijkstra");
    }


    /*
     * Finds shortes path through disabled nodes. Just to provide end of
     * tunnels, i.e. their parts on the surface.
     */
    public Integer finalizingDijkstra(VoronoiDiagram vd, int sourceNode,
            double minRadius, double[] distances, int[] previous) {

        int operations = 0;
        int visited = 0;
        SortedMap<Double, List<Integer>> q =
                new TreeMap<Double, List<Integer>>();
        double sourceDistance = distances[sourceNode];
        q.put(sourceDistance, new ArrayList<Integer>());
        q.get(sourceDistance).add(sourceNode);

        while (!q.isEmpty()) {
            List<Integer> list = q.get(q.firstKey());
            if (list.isEmpty()) {
                break;
            }
            Integer u = list.iterator().next();

            if (vd.isOuter(u)) {
                return u;
            }

            if (1 == list.size()) {
                q.remove(q.firstKey());
            } else {
                list.remove(u);
            }
            for (int i = 0; i < 4; i++) {

                int v = vd.getNeighbour(u, i);
                // we want to go only through disabled, i.e. surface TODO rename
                if (vd.disabled(v)
                        && minRadius <= vd.getBottleneckByGate(u, i)) {
                    double alt = distances[u] + vd.getWeight(u, i);
                    if (alt < distances[v]) {
                        operations++;
                        distances[v] = alt;
                        visited++;
                        previous[v] = u;
                        if (!q.containsKey(alt)) {
                            q.put(alt, new ArrayList<Integer>());
                        }
                        q.get(alt).add(v);
                    }

                }
            }
        }
        return null;
    }

    /*
     * Runs Dijkstra's algorithm just to learn the width of the widest path from
     * sourceNode to surface.
     */
    public double dijkstraGetBottleneck(VoronoiDiagram vd,
            int sourceNode, double minRadius) {
        Clock.start("dijkstra get bottleneck");
        double bottleneck = minRadius;
        int operations = 0;
        int visited = 0;

        // node [i] is reacheble by path with bottleneck bottlenecks[i]
        double[] bottlenecks = new double[vd.size()];
        bottlenecks[sourceNode] = Double.MAX_VALUE;
        SortedMap<Double, List<Integer>> q =
                new TreeMap<Double, List<Integer>>();
        q.put(Double.MAX_VALUE, new ArrayList<Integer>());
        q.get(Double.MAX_VALUE).add(sourceNode);

        while (!q.isEmpty()) {
            List<Integer> list = q.get(q.lastKey());
            if (list.isEmpty()) {
                break;
            }
            Integer u = list.iterator().next();
            if (1 == list.size()) {
                q.remove(q.lastKey());
            } else {
                list.remove(u);
            }
            for (int i = 0; i < 4; i++) {

                int v = vd.getNeighbour(u, i);
                if (!vd.valid(v)) {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "Warning, border should never be in queue.");
                    continue;
                }
                // width of last edge of newly considered path
                double b = vd.getBottleneckByGate(u, i);
                if (minRadius <= b) {
                    // bottleneck of newly considered path
                    double alt = Math.min(bottlenecks[u], b);
                    if (bottlenecks[v] < alt) {
                        operations++;
                        bottlenecks[v] = alt;
                        visited++;
                        if (vd.isOnBorder(v)) { // reached convex hull
                            if (bottleneck < alt) {
                                bottleneck = alt;
                            }
                        } else { // continue search
                            if (!q.containsKey(alt)) {
                                q.put(alt, new ArrayList<Integer>());
                            }
                            q.get(alt).add(v);
                        }
                    }

                }
            }
        }

        Clock.stop("dijkstra get bottleneck");

        return bottleneck;
    }

    public static void saveTetrahedrons(Iterable<Tetrahedron> tetrahedrons,
            String fileName) {
        try {
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(
                        fileName));
                int serial = 1;
                for (Tetrahedron t : tetrahedrons) {
                    serial = t.toPdb(bw, serial, "1");
                }
                bw.close();
            } catch (IOException e) {
                bw.close();
                Logger.getLogger("caver").log(Level.WARNING,
                        "Save tetrahedrons.", e);
            }
        } catch (Exception e) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Save tetrahedrons.", e);
        }
    }

    /*
     * Finds the cheapest paths from sourceNode to a node outside of sphere of
     * center source and radius bubbleRadius.
     */
    public Set<Integer> dijkstraInBubble(VoronoiDiagram vd, int sourceNode,
            double minRadius, double bubbleRadius) {

        Point source = vd.getPoint(sourceNode);

        Clock.start("dijkstra");
        Set<Integer> ends = new HashSet<Integer>();
        int operations = 0;
        int visited = 0;
        vd.setDistance(sourceNode, 0);
        SortedMap<Double, List<Integer>> q =
                new TreeMap<Double, List<Integer>>();
        q.put(0.0, new ArrayList<Integer>());
        q.get(0.0).add(sourceNode);

        while (!q.isEmpty()) {
            List<Integer> list = q.get(q.firstKey());
            if (list.isEmpty()) {
                break;
            }
            Integer u = list.iterator().next();
            if (1 == list.size()) {
                q.remove(q.firstKey());
            } else {
                list.remove(u);
            }
            for (int i = 0; i < 4; i++) {

                int v = vd.getNeighbour(u, i);

                if (!vd.valid(v)) {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "Incorect state of Voronoi diagram.");
                }

                boolean outer = vd.isOuter(v) || vd.disabled(v);

                if (minRadius <= vd.getBottleneckByGate(u, i)) {
                    double alt = vd.getDistance(u) + vd.getWeight(u, i);

                    if (alt < vd.getDistance(v)) {
                        operations++;
                        vd.setDistance(v, alt);
                        visited++;
                        vd.setPrevious(v, u);

                        if (outer || bubbleRadius
                                < vd.getPoint(v).distance(source)) {
                            ends.add(v);
                        } else {
                            if (!q.containsKey(alt)) {
                                q.put(alt, new ArrayList<Integer>());
                            }
                            q.get(alt).add(v);
                        }
                    }

                }
            }
        }

        Clock.stop("dijkstra");
        return ends;
    }

    /*
     * Second, prolonging, phase of the tunnel identification algorithm.
     */
    public Set<Integer> secondaryDijkstra(VoronoiDiagram vd, int sourceNode,
            double minRadius, double[] distances, int[] previous) {

        HashSet<Integer> ends = new HashSet<Integer>();
        if (vd.isOuter(sourceNode)) {
            return ends;
        }
        int operations = 0;
        int visited = 0;

        SortedMap<Double, List<Integer>> q =
                new TreeMap<Double, List<Integer>>();
        double sourceDistance = distances[sourceNode];
        q.put(sourceDistance, new ArrayList<Integer>());
        q.get(sourceDistance).add(sourceNode);

        while (!q.isEmpty()) {
            List<Integer> list = q.get(q.firstKey());
            if (list.isEmpty()) {
                break;
            }
            Integer u = list.iterator().next();

            if (1 == list.size()) {
                q.remove(q.firstKey());
            } else {
                list.remove(u);
            }
            for (int i = 0; i < 4; i++) {

                int v = vd.getNeighbour(u, i);
                if (minRadius <= vd.getBottleneckByGate(u, i)) {
                    double alt = distances[u] + vd.getWeight(u, i);
                    if (alt < distances[v]) {
                        operations++;
                        distances[v] = alt;
                        visited++;
                        previous[v] = u;

                        boolean outer = vd.isOuter(v) || vd.disabled(v);
                        if (outer) {
                            ends.add(v);
                        } else {
                            if (!q.containsKey(alt)) {
                                q.put(alt, new ArrayList<Integer>());
                            }
                            q.get(alt).add(v);
                        }
                    }

                }
            }
        }

        return ends;
    }
}
