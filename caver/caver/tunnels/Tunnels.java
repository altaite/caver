package caver.tunnels;

import algorithms.clustering.Clustering;
import algorithms.clustering.layers.LayeredTunnels;
import caver.CalculationSettings;
import caver.Clock;
import caver.ui.CalculationException;
import chemistry.pdb.SnapId;
import geometry.primitives.Point;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of tunnels from one snapshot from common origin (starting point).
 *
 * Method assignPriorities must be called once the set of tunnels contained
 * within this object is final.
 *
 */
public class Tunnels implements Serializable {

    private final static long serialVersionUID = 1L;//7349826802229492304L;
    private SortedSet<Tunnel> tunnels_ = new TreeSet<Tunnel>();
    private Point origin_;
    private Point voronoiOrigin_;
    private int voronoiOriginNode_;
    private transient CalculationSettings cs_;

    private Tunnels() {
    }

    public static Tunnels create(Point origin, Point voronoiOrigin,
            int voronoiOriginNode, CalculationSettings cs) {
        Tunnels ts = new Tunnels();
        ts.origin_ = origin;
        ts.voronoiOrigin_ = voronoiOrigin;
        ts.voronoiOriginNode_ = voronoiOriginNode;
        ts.cs_ = cs;
        return ts;
    }

    public static Tunnels create(File file, CalculationSettings cs)
            throws IOException {

        Clock.start("loading Tunnels");
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file));
        Object o;
        try {
            o = ois.readObject();
            ois.close();
            Tunnels ts = (Tunnels) o;

            for (Tunnel t : ts.tunnels_) {
                t.setCalculationSettings(cs);
                if (cs.swap()) {
                    t.deleteEdges();
                }
            }
            Clock.stop("loading Tunnels");
            ts.cs_ = cs;
            return ts;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public int filter(int n) {
        int out = tunnels_.size() - n;
        if (out < 0) {
            out = 0;
        }
        Tunnel[] ts = tunnels_.toArray(new Tunnel[tunnels_.size()]);
        Arrays.sort(ts, new TunnelCostComparator());
        tunnels_.clear();
        for (int i = 0; i < Math.min(n, ts.length); i++) {
            add(ts[i]);
        }
        return out;
    }

    public int filter(double d) {
        List<Tunnel> ts = new ArrayList<Tunnel>();
        int out = 0;

        for (Tunnel t : tunnels_) {
            if (t.getBottleneck().getR() < d) {
                out++;
            } else {
                ts.add(t);
            }
        }

        tunnels_.clear();
        addAll(ts);

        return out;
    }

    public void save(SnapId snap, CalculationSettings cs) throws IOException {

        Clock.start("saving edges");

        if (cs.saveSwap()) {

            SortedMap<Integer, Tunnel> byId = new TreeMap<Integer, Tunnel>();

            for (Tunnel t : tunnels_) {
                byId.put(t.getId(), t);
            }
            int n = byId.size();

            File edgesFile = cs.getEdgesFile(snap);
            DataOutputStream dos =
                    new DataOutputStream(
                    new BufferedOutputStream(
                    new FileOutputStream(edgesFile)));

            dos.writeInt(n);
            for (Tunnel t : byId.values()) {
                dos.writeInt(t.getId());
                dos.writeInt(t.getEdgesCount());
            }
            for (Tunnel t : byId.values()) {

                t.writeEdges(dos);
            }

            dos.close();
        }
        Clock.stop("saving edges");
        Clock.start("saving tunnels");
        File tunnelsFile = cs.getTunnelsFile(snap);
        ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(
                new FileOutputStream(tunnelsFile)));
        oos.writeObject(this);
        oos.close();
        Clock.stop("saving tunnels");
    }

    public void add(Tunnel t) {
        if (tunnels_.contains(t)) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Tunnel {0} already exists.", t.getNumber());
        }
        tunnels_.add(t);
    }

    public void assignPriorities() {
        int i = 1;
        for (Tunnel t : tunnels_) {
            t.setSnapshotPriority(i++);
        }

    }

    public void addAll(Collection<Tunnel> ts) {
        for (Tunnel t : ts) {
            add(t);
        }
    }

    public int size() {
        return tunnels_.size();
    }

    public boolean isEmpty() {
        return tunnels_.isEmpty();
    }

    public List<Tunnel> getTunnels() {
        return new ArrayList<Tunnel>(tunnels_);
    }

    public Point getOrigin() {
        return origin_;
    }

    public Point getVoronoiOrigin() {
        return voronoiOrigin_;
    }

    public int getVoronoiOriginNode() {
        return voronoiOriginNode_;
    }

    public void cluster(LayeredTunnels lts, CalculationSettings cs)
            throws CalculationException,
            IOException {
        if (!tunnels_.isEmpty()) {
            Clustering clustering = new Clustering(voronoiOrigin_, tunnels_, cs);
            tunnels_ = clustering.bestStaysClustering(lts);
        }
    }
}
