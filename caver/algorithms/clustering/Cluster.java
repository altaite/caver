package algorithms.clustering;

import algorithms.clustering.layers.AverageSurface;
import algorithms.clustering.layers.LayeredTunnel;
import algorithms.clustering.layers.LayeredTunnels;
import algorithms.clustering.statistics.ClusterStatistics;
import caver.CalculationSettings;
import caver.Clock;
import caver.tunnels.Tunnel;
import caver.tunnels.TunnelCostComparator;
import caver.tunnels.TunnelSnapshotAndCostComparator;
import caver.util.Distribution;
import chemistry.pdb.SnapId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Represents a cluster of tunnels.
 *
 * When the cluster is not needed anymore, call destroy to remove binding of
 * tunnels to this cluster.
 *
 * If tunnels contained in cluster are changed, set averageWidthCalculated =
 * false.
 */
public class Cluster {

    private List<Tunnel> tunnels = new ArrayList<Tunnel>();
    private ClusterId id_;
    private Integer priority = null;
    private ClusterStatistics statistics;
    private int[] starts;
    private int clusterStart;
    private int clusterEnd;
    private boolean characterized;
    private double dA_; // distance average
    private double dSD_; // distance standard deviation
    private CalculationSettings cs_;
    LayeredTunnels lts_;
    AverageSurface surface_;
    private Integer exactN_;

    public Cluster(ClusterId id, CalculationSettings cs) {
        id_ = id;
        cs_ = cs;
    }

    public void calculateStatistics(Set<SnapId> snapshotsSet,
            double bestThroughputPercent) {
        this.statistics = new ClusterStatistics(
                this, snapshotsSet, bestThroughputPercent);
    }

    public ClusterStatistics getStatistics() {
        return statistics;
    }

    public ClusterId getId() {
        return id_;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(int p) {
        this.priority = p;
    }

    public List<Tunnel> getTunnels() {
        return tunnels;
    }

    public Tunnel[] getTunnelsByCost() {
        Tunnel[] ts = tunnels.toArray(new Tunnel[tunnels.size()]);
        Arrays.sort(ts, new TunnelCostComparator());
        return ts;
    }

    public Tunnel[] getTunnelsBySnapshotAndCost() {
        Tunnel[] ts = tunnels.toArray(new Tunnel[tunnels.size()]);
        Arrays.sort(ts, new TunnelSnapshotAndCostComparator());
        return ts;
    }

    public int size() {
        return tunnels.size();
    }

    public int getClusterStart() {
        return clusterStart;
    }

    public int getClusterEnd() {
        return clusterEnd;
    }

    public int getSpaceN() {
        return starts.length;
    }

    public int getSpaceStart(int s) {
        return starts[s];
    }

    public Map<SnapId, List<Tunnel>> getAllTunnelsBySnapshots() {
        Map<SnapId, List<Tunnel>> map =
                new TreeMap<SnapId, List<Tunnel>>();
        for (Tunnel t : tunnels) {
            if (!map.containsKey(t.getSnapId())) {
                map.put(t.getSnapId(), new ArrayList<Tunnel>());
            }
            map.get(t.getSnapId()).add(t);
        }
        return map;
    }

    public Map<SnapId, Tunnel> getCheapestTunnelFromEachSnapshot() {
        Map<SnapId, Tunnel> map =
                new TreeMap<SnapId, Tunnel>();
        for (Tunnel t : tunnels) {
            if (map.containsKey(t.getSnapId())) {
                Tunnel to = map.get(t.getSnapId());
                if (t.getCost() < to.getCost()) {
                    map.put(t.getSnapId(), t);
                }
            } else {
                map.put(t.getSnapId(), t);
            }
        }
        return map;
    }

    public Map<SnapId, Tunnel> getTunnelsBySnapshots() {
        Map<SnapId, Tunnel> map =
                new TreeMap<SnapId, Tunnel>();
        for (Tunnel t : tunnels) {
            if (!map.containsKey(t.getSnapId())) {

                map.put(t.getSnapId(), t);
            } else {
                Tunnel tOld = map.get(t.getSnapId());
                if (t.getCost() < tOld.getCost()) {
                    map.put(t.getSnapId(), t);
                }
            }

        }
        return map;
    }

    public List<Tunnel> getTunnels(SnapId snapshot) {
        List<Tunnel> list =
                new ArrayList<Tunnel>();
        for (Tunnel t : tunnels) {
            if (t.getSnapId().equals(snapshot)) {
                list.add(t);
            }
        }
        return list;
    }

    public boolean addTunnel(Tunnel t, boolean checkDistance) {

        int n = 100;
        int m = 100;
        if (checkDistance && !characterized) {

            Clock.start("in-cluster distances characterization.");


            exactN_ = tunnels.size();

            if (exactN_ < n) {
                n = exactN_;
            }

            surface_ = new AverageSurface(
                    tunnels, cs_.getAverageVoronoiOrigin(), cs_);

            lts_ = new LayeredTunnels(
                    cs_.getAverageVoronoiOrigin(),
                    surface_, tunnels,
                    false, cs_.doAverageSurfaceGlobal(),
                    cs_.getGlobalLayersSettings(), cs_);

            double[] distances = new double[n];

            Random random = cs_.getRandom();

            for (int i = 0; i < n; i++) {
                int x = random.nextInt(exactN_);
                int y = random.nextInt(exactN_);
                distances[i] = (double) lts_.getDistance(x, y);
            }

            Distribution d = new Distribution(distances);

            dA_ = d.getAverage();
            dSD_ = d.getStandardDeviation();


            characterized = true;
            Clock.stop("in-cluster distances characterization.");
        }

        boolean add = true;
        if (checkDistance) {
            Clock.start("outlier detection.");
            Random random = cs_.getRandom();
            if (exactN_ < m) {
                m = exactN_;
            }
            double best = Double.MAX_VALUE;
            LayeredTunnel lt = lts_.createLayerdTunnel(t);
            for (int i = 0; i < m; i++) {
                int x = random.nextInt(exactN_);

                double d = lts_.getDistance(x, lt);
                if (d < best) {
                    best = d;
                }
            }

            if (best <= dA_ + 3 * dSD_) {
                add = true;
            } else { // outlier
                add = false;
            }
            Clock.stop("outlier detection.");

        }
        if (add) {
            tunnels.add(t);
            t.setCluster(this);
            return true;
        } else {
            return false;
        }
    }

    public void leaveRandomTunnelPerSnapshot(Random random) {
        List<Tunnel> newTunnels = new ArrayList<Tunnel>();
        for (List<Tunnel> list : getAllTunnelsBySnapshots().values()) {
            int r = random.nextInt(list.size());
            Tunnel wt = list.get(r);
            newTunnels.add(wt);
        }
        tunnels = newTunnels;
    }

    public void leaveCheapestTunnelPerSnapshot() {
        List<Tunnel> newTunnels = new ArrayList<Tunnel>();
        for (List<Tunnel> list : getAllTunnelsBySnapshots().values()) {
            Integer cheapest = null;
            for (int i = 0; i < list.size(); i++) {
                if (null == cheapest
                        || list.get(i).getCost()
                        < list.get(cheapest).getCost()) {
                    cheapest = i;
                }
            }
            Tunnel wt = list.get(cheapest);
            newTunnels.add(wt);
        }
        tunnels = newTunnels;
    }

    public List<Tunnel> getRandomTunnels(int n, Random random) {
        List<Tunnel> source = new ArrayList<Tunnel>();
        source.addAll(tunnels);
        List<Tunnel> list = new ArrayList<Tunnel>();
        while (list.size() < n && !source.isEmpty()) {
            int r = random.nextInt(source.size());
            list.add((Tunnel) source.remove(r));
        }
        return list;
    }

    public int subsampleRandom(int limit, Random random) {
        int removed = 0;
        if (limit < tunnels.size()) {
            int remove = tunnels.size() - limit;
            for (int i = 0; i < remove; i++) {
                int r = random.nextInt(tunnels.size());
                tunnels.remove(r);
                removed++;
            }
        }
        return removed;
    }

    /*
     * For sorting tunnels beginning from the widest ones.
     */
    private class TunnelWidthComparator implements Comparator<Tunnel> {

        @Override
        public int compare(Tunnel a, Tunnel b) {

            int c = Double.compare(
                    a.getCost(),
                    b.getCost());
            if (0 == c) {
                c = -Double.compare(
                        a.getBottleneck().getR(),
                        b.getBottleneck().getR());
            }

            if (0 == c) {
                c = -a.getSnapId().compareTo(b.getSnapId());
            }
            if (0 == c) {
                c = -Double.compare(
                        a.getNumber(),
                        b.getNumber());
            }

            return c;
        }
    }

    public int subsampleCheapest(int limit) {
        if (tunnels.size() <= limit) {
            return 0;
        }
        int removed = tunnels.size() - limit;
        Tunnel[] a = tunnels.toArray(new Tunnel[0]);
        Arrays.sort(a, new TunnelWidthComparator());
        if (a.length > 1 && a[0].getCost() > a[a.length - 1].getCost()) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Warning: subsampling might be incorrect.");
        }
        tunnels.clear();
        for (int i = 0; i < limit; i++) {
            tunnels.add(a[i]);
        }
        return removed;
    }

    public void destroy() {
        for (Tunnel t : tunnels) {
            t.clearCluster();
        }
    }
}
