package algorithms.triangulation;

import caver.CalculationSettings;
import caver.Printer;
import chemistry.pdb.PdbLine;
import chemistry.pdb.PdbUtil;
import chemistry.pdb.SnapId;
import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Voronoi diagram of set of a set of spheres of identical radii. Only Voronoi
 * edges are stored.
 *
 */
public class VoronoiDiagram {

    private int neighbours_[][]; // neighbours_[node] contains four 
    //                              neighbouring nodes of node
    private VE[][] segments_;
    private boolean disabled_[]; // disabled nodes, i.e. deleted. 
    //                              WARNING: eges still exists 
    //                              (neighbours_ etc.)
    private boolean outer_[];
    private Sphere[] spheres_;
    private double[] distances_;
    private int[] previous_;
    public final int OUT = -2;
    public final int NULL = -1;
    KDTree<Integer> kdTree_;

    private VoronoiDiagram() {
    }

    public void initializeSearchTables() {
        distances_ = new double[size()];
        Arrays.fill(distances_, Double.MAX_VALUE);
        previous_ = new int[size()];
        Arrays.fill(previous_, NULL);
    }

    public double[] copyDistances() {
        double[] distances = new double[distances_.length];
        System.arraycopy(distances_, 0, distances, 0, distances_.length);
        return distances;
    }

    public int[] copyPrevious() {
        int[] previous = new int[previous_.length];
        System.arraycopy(previous_, 0, previous, 0, previous_.length);
        return previous;
    }

    public static VoronoiDiagram create(int n) {
        VoronoiDiagram vd = new VoronoiDiagram();
        vd.neighbours_ = new int[n][4]; // initialized to NONE

        for (int i = 0; i < vd.neighbours_.length; i++) {
            for (int j = 0; j < 4; j++) {
                vd.neighbours_[i][j] = vd.OUT;
            }
        }
        vd.segments_ = new VE[n][4];
        vd.disabled_ = new boolean[n];
        vd.outer_ = new boolean[n];
        vd.spheres_ = new Sphere[n];


        return vd;
    }

    public boolean check() {

        for (int i = 0; i < neighbours_.length; i++) {
            for (int j = 0; j < 4; j++) {
                int x = i;
                int y = neighbours_[x][j];
                if (0 <= y) {
                    if (areConnected(x, y) != areConnected(y, x)) {
                        throw new RuntimeException();
                    }
                }
            }
        }

        for (int i = 0; i < neighbours_.length; i++) {
            for (int j = i + 1; j < neighbours_.length; j++) {
                assert areConnected(i, j) == areConnected(j, i);
                if (getBottleneckByNodes(i, j) != getBottleneckByNodes(j, i)) {
                    VE a = this.getSegmentByNodes(i, j);
                    VE b = this.getSegmentByNodes(j, i);
                    if (a == null) {
                        System.err.println("A NULL");
                    } else if (b == null) {
                        System.err.println("B NULL");
                    } else {


                        double diff = Math.abs(
                                a.getBottleneck().getR()
                                - b.getBottleneck().getR());
                        if (diff > 0.1) {
                            System.err.println("SYMETRY VD CHECK " + diff);
                        }



                        if (!a.equals(b)) {
                            System.err.println(a.getNodeA() + " "
                                    + a.getNodeB());
                            System.err.println(b.getNodeA() + " "
                                    + b.getNodeB());
                        }
                    }
                }
            }
        }

        KDTree<Point> kdTree = new KDTree<Point>(3);
        // outside Voronoi vertices
        for (int node = 0; node < spheres_.length; node++) {
            Point p = getPoint(node);
            double[] coords = p.getCoordinates();
            try {
                kdTree.insert(coords, p);
            } catch (KeySizeException e) {
                throw new RuntimeException(e);
            } catch (KeyDuplicateException e) {
                Logger.getLogger("caver").warning("DUPL");
                Logger.getLogger("caver").log(Level.WARNING,
                        "inserted node: {0}", node);
                try {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "existant node: {0}", kdTree.search(coords));
                } catch (KeySizeException ex) {
                    throw new RuntimeException(ex);
                }
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private boolean small(Point p) {
        return p.getX() < 9999.999
                && p.getY() < 9999.999
                && p.getZ() < 9999.999
                && -999.999 < p.getX()
                && -999.999 < p.getY()
                && -999.999 < p.getZ();

    }

    public void createKDTree() {

        kdTree_ = new KDTree<Integer>(3);
        for (int node = 0; node < spheres_.length; node++) {
            double[] coords = getPoint(node).getCoordinates();
            try {
                kdTree_.insert(coords, node);
            } catch (KeySizeException e) {
                throw new RuntimeException(e);
            } catch (KeyDuplicateException e) {
                Logger.getLogger("caver").warning("DUPL");
                Logger.getLogger("caver").log(Level.WARNING,
                        "inserted node: {0}", node);
                try {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "existant node: {0}", kdTree_.search(coords));
                } catch (KeySizeException ex) {
                    throw new RuntimeException(ex);
                }
                throw new RuntimeException(e);
            }
        }
    }

    public void destroyKDTree() {
        kdTree_ = null;
    }

    public List<Integer> nearestEuclidean(double[] coords, double distance) {
        try {
            return kdTree_.nearestEuclidean(coords, distance);
        } catch (KeySizeException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(CalculationSettings cs, File f) throws IOException {

        int serial = 1;
        int model = 1;
        double maxR = cs.getShellRadius() * 5;

        f.delete();
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(PdbUtil.getModelString(model) + "\n");

        for (int node = 0; node < segments_.length; node++) {
            for (VE s : segments_[node]) {
                if (null == s) {
                    continue;
                }

                if (null == s.getA() || null == s.getB()) {
                    continue;
                }

                if (!small(s.getA()) || !small(s.getB())) {
                    continue;
                }

                if (maxR < s.getARadius() || maxR < s.getBRadius()) {
                    continue;
                }

                if (s.getBottleneck().getR() < cs.getProbeRadius()) {
                    continue;
                }

                Point[] ps = {s.getA(), s.getB()};

                for (int i = 0; i < 2; i++) {
                    PdbLine pl = new PdbLine(serial, "H", "TUN", "RRR", 'T',
                            ps[i].getX(), ps[i].getY(), ps[i].getZ());
                    bw.write(pl.getPdbString() + "\n");
                    serial++;
                }

                String connectS = String.format("CONECT%5d%5d", serial - 2, serial - 1);
                bw.write(connectS + "\n");
                if (99999 - 2 < serial) { // trick to overcome 99999 limit, 
                    // bonds stays same, use show all states in pymol
                    bw.write("ENDMDL\n");
                    bw.write(PdbUtil.getModelString(model) + "\n");
                    serial = 1;
                }
            }
        }
        bw.write("ENDMDL\n");
        bw.close();
    }

    public void saveTree(File f) throws IOException {

        int serial = 1;
        int model = 1;

        f.delete();
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(PdbUtil.getModelString(model) + "\n");


        for (int node = 0; node < segments_.length; node++) {
            int previous = getPrevious(node);
            if (valid(previous)) {

                VE e = this.getSegmentByNodes(node, previous);

                if (null == e) {
                    System.err.println("Strange tree.");
                    continue;
                }

                Sphere[] s = {e.getASphere(), e.getBSphere()};

                for (int i = 0; i < 2; i++) {
                    PdbLine pl = new PdbLine(serial, "H", "TUN", "RRR", 'T',
                            s[i].getS().getX(), s[i].getS().getY(), s[i].getS().getZ());
                    pl.setTemperatureFactor(s[i].getR());
                    bw.write(pl.getPdbString() + "\n");
                    serial++;
                }

                String connectS = String.format("CONECT%5d%5d", serial - 2, serial - 1);
                bw.write(connectS + "\n");
                if (99999 - 2 < serial) { // trick to overcome 99999 limit, 
                    // bonds stays same, use show all states in pymol
                    bw.write("ENDMDL\n");
                    bw.write(PdbUtil.getModelString(model) + "\n");
                    serial = 1;
                }

            }
        }
        bw.write("ENDMDL\n");
        bw.close();
    }

    public void saveVoid(File f, SnapId snapId) throws IOException {

        boolean overflow = false;
        int serial = 1;

        BufferedWriter bw;
        bw = new BufferedWriter(new FileWriter(f, true));
        bw.write(PdbUtil.getModelString(snapId.getNumber()) + "\n");

        for (int node = 0; node < segments_.length; node++) {
            int previous = getPrevious(node);
            if (valid(previous)) {

                VE e = this.getSegmentByNodes(node, previous);

                if (null == e) {
                    System.err.println("Strange tree.");
                    continue;
                }

                Sphere[] ss = {e.getASphere(), e.getBSphere()};

                for (int i = 0; i < 2; i++) {
                    PdbLine pl = new PdbLine(serial, "H", "TUN", "RRR", 'T',
                            ss[i].getS().getX(), ss[i].getS().getY(),
                            ss[i].getS().getZ());
                    pl.setTemperatureFactor(ss[i].getR());
                    bw.write(pl.getPdbString() + "\n");
                    serial++;
                }

                if (99999 - 2 < serial) { // trick to overcome 99999 limit,
                    // bonds stays same, use show all states in pymol
                    overflow = true;

                }

            }
        }
        bw.write("ENDMDL\n");
        bw.close();
        if (overflow) {
            Logger.getLogger("caver").log(Level.WARNING, "Void "
                    + "space was not saved whole, number of spheres > 99999.");
        }
    }

    public int size() {
        return neighbours_.length;
    }

    // connects x and y by directed edge
    // in the end all edges should be bidirectional
    public void connect(int x, int y, VE segment) {

//        assert !areConnected(x, y);
//        assert !areConnected(y, x);

        assert x != y;
        assert x < neighbours_.length : x + "<" + neighbours_.length;
        assert y < neighbours_.length : y + "<" + neighbours_.length;

        int slot = NULL;
        for (int i = 0; i < 4; i++) {
            if (OUT == neighbours_[x][i]) {
                slot = i;
            }
        }
        neighbours_[x][slot] = y;
        segments_[x][slot] = segment;
    }

    public boolean areConnected(int x, int y) {
        for (int i = 0; i < 4; i++) {
            if (y == neighbours_[x][i]) {
                return true;
            }
        }
        return false;
    }

    public int getNeighbour(int node, int gate) {
        return neighbours_[node][gate];
    }

    public boolean valid(int node) {
        return 0 <= node && node < size();
    }

    public void makeOuter(int node) {
        outer_[node] = true;
    }

    /*
     * Is outer (just for safety) or borders outer?
     */
    public boolean isOuter(int node) {

        for (int n : neighbours_[node]) {

            if (NULL == n || OUT == n) {
                return true;
            }
            if (outer_[n]) {
                return true;
            }

        }

        return outer_[node];
    }

    public boolean isOut(int node) {
        return outer_[node];
    }


    /*
     * Is outer (just for safety) or borders outer?
     */
    public boolean isOnBorder(int node) {

        for (int n : neighbours_[node]) {

            if (NULL == n || OUT == n) {
                return true;
            }
        }

        return false;
    }

    public List<Integer> getNeighbours(int node) {
        List<Integer> list = new ArrayList<Integer>();
        for (int n : neighbours_[node]) {
            if (NULL != n && OUT != n) {
                list.add(n);
            }
        }
        assert list.size() <= 4;
        return list;
    }

    public VE getSegmentByNodes(int nodeA, int nodeB) {
        for (int i = 0; i < 4; i++) {
            if (nodeB == neighbours_[nodeA][i]) {
                return segments_[nodeA][i];
            }
        }
        return null;
    }

    // WARNING: returns 0 for gates leading to outer space
    public double getBottleneckByGate(int node, int gate) {
        if (OUT == neighbours_[node][gate]) {
            return 0; // in fact, we do not know, but search ends here anyway
        } else {
            return segments_[node][gate].getBottleneck().getR();
        }
    }

    public Double getBottleneckByNodes(int a, int b) {
        for (int i = 0; i < 4; i++) {
            if (neighbours_[a][i] == b) {
                return getBottleneckByGate(a, i);
            }
        }
        return null;
    }

    public double getWeight(int node, int gate) {
        return segments_[node][gate].getWeight();
    }

    public boolean disabled(int node) {
        return !valid(node) || disabled_[node];
    }

    public void disable(int node) {
        disabled_[node] = true;
    }

    public void setPoint(int node, Sphere s) {
        spheres_[node] = s;
    }

    public Sphere getSphere(int node) {
        return spheres_[node];
    }

    public Point getPoint(int node) {
        return spheres_[node].getS();
    }

    public double getVertexRadius(int node) {
        return spheres_[node].getR();
    }

    public void setDistance(int node, double distance) {
        distances_[node] = distance;
    }

    public double getDistance(int node) {
        return distances_[node];
    }

    public void setPrevious(int node, int previousNode) {
        previous_[node] = previousNode;
    }

    public int getPrevious(int node) {
        return previous_[node];
    }

    public int countNodes() {
        int count = 0;
        for (int i = 0; i < disabled_.length; i++) {
            if (!disabled_[i]) {
                count++;
            }
        }
        return count;
    }

    public Integer getClosestNode(Point point) {
        double min = Double.MAX_VALUE;
        Integer node = null;
        for (int i = 0; i < spheres_.length; i++) {
            if (!disabled(i)) {
                double dist = point.distance(spheres_[i].getS());
                if (dist < min) {
                    node = i;
                    min = dist;
                }
            }
        }
        return node;
    }

    private Integer findClosest(Point origin) {

        Integer node = null;

        for (int i = 0; i < spheres_.length; i++) {
            if (null == node) {
                node = i;
            } else {
                double d = origin.distance(spheres_[i].getS());
                double dOld = origin.distance(spheres_[node].getS());
                if (d < dOld) {
                    node = i;
                }
            }
        }
        return node;
    }

    private Integer optimizeByParameters(
            Point origin, double desiredRadius, double maxDistance) {

        Integer node = null;

        for (int i = 0; i < spheres_.length; i++) {
            if (desiredRadius <= spheres_[i].getR()) {
                double d = origin.distance(spheres_[i].getS());
                if (d <= maxDistance) {
                    if (null == node) {
                        node = i;
                    } else {
                        double dOld = origin.distance(spheres_[node].getS());
                        if (d < dOld) {
                            node = i;
                        }
                    }
                }
            }
        }

        // find vertex with greatest possible radius within maxDistance
        if (null == node) {
            for (int i = 0; i < spheres_.length; i++) {
                double d = origin.distance(spheres_[i].getS());
                if (d < maxDistance) {
                    if (null == node) {
                        node = i;
                    } else {
                        if (spheres_[node].getR() < spheres_[i].getR()) {
                            node = i;
                        }
                    }
                }
            }
        }
        return node;
    }

    public Integer getOptimizedOrigin(Point origin,
            double desiredRadius,
            double maxDistance,
            double defaultMaxDistance, double probeR) {


        Integer node = optimizeByParameters(origin,
                desiredRadius, maxDistance);

        if (null == node) {

            Printer.warn("No vertex found within the specified distance of "
                    + maxDistance + ", setting max_distance to "
                    + defaultMaxDistance + ".");

            node = optimizeByParameters(origin,
                    desiredRadius, defaultMaxDistance);

        }

        if (null == node) {
            node = findClosest(origin);

            double d = reduce(origin.distance(spheres_[node].getS()));
            double r = reduce(spheres_[node].getR());
            Printer.warn("No vertex found within the distance of "
                    + defaultMaxDistance + ", using the closest vertex "
                    + "as a starting point (distance: " + d + ", radius: "
                    + r + ").");
        }

        double r = reduce(spheres_[node].getR());
        if (spheres_[node].getR() < probeR) {
            Printer.warn("Starting point is more narrow (radius: "
                    + spheres_[node].getR() + ") than probe (radius: " + probeR
                    + "). No tunnels will be find, please specify starting "
                    + "point better or change starting point optimization"
                    + " parameters max_distance and desired_radius.");
        }

        return node;
    }

    private double reduce(double d) {
        int p = 100;
        return (double) Math.round(d * p) / p;
    }

    /*
     * On surface after outer and inner shell was removed.
     */
    public Set<Integer> getOuterNodes() {
        Set<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < neighbours_.length; i++) {
            if (!disabled(i)) {
                for (int j = 0; j < 4; j++) {
                    // no neighbour or disabled neighbour
                    if (NULL == neighbours_[i][j] || OUT == neighbours_[i][j]
                            || disabled(neighbours_[i][j])) {
                        set.add(i);
                        break;
                    }
                }
            }
        }
        return set;
    }

    public Set<Integer> getDisbledNodes() {
        Set<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < neighbours_.length; i++) {
            if (disabled(i)) {
                set.add(i);
            }
        }
        return set;
    }

    public Set<Integer> getDisabledNeighbouringEnabledNodes() {
        Set<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < neighbours_.length; i++) {
            if (disabled(i)) {
                for (int j = 0; j < 4; j++) {
                    int n = neighbours_[i][j];
                    if (valid(n) && !disabled(n)) {
                        set.add(i);
                        break;
                    }
                }
            }
        }
        return set;
    }

    public Set<Integer> getNodesOnSurface() {
        Set<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < neighbours_.length; i++) {
            for (int j = 0; j < 4; j++) {
                // no neighbour                
                if (OUT == neighbours_[i][j] || NULL == neighbours_[i][j]) {
                    set.add(i);
                    break;
                }
            }
        }
        return set;
    }
}
