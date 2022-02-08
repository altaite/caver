package algorithms.clustering;

import algorithms.clustering.layers.LayeredTunnels;
import caver.CalculationSettings;
import caver.Printer;
import caver.tunnels.Tunnel;
import caver.tunnels.TunnelPriorityComparator;
import chemistry.pdb.SnapId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
 * Data structure for storing set of clusters.
 *
 * If map is changed, ss must be set to null!
 */
public class Clusters {

    private SortedSet<Cluster> ss_;
    private Map<ClusterId, Cluster> map_ = new HashMap<ClusterId, Cluster>();
    CalculationSettings cs_;

    public Clusters(CalculationSettings cs) {
        cs_ = cs;
    }

    public boolean isEmpty() {
        return map_.isEmpty();
    }

    public void leaveBest(int n, double bestThroughputPercent,
            SortedSet<SnapId> snaps) {

        calculateStatistics(snaps, bestThroughputPercent);
        computePriorities();

        Set<Cluster> remove = new HashSet<Cluster>();
        for (Cluster c : ss_) {
            if (n < c.getPriority()) {
                remove.add(c);
                c.destroy();
            }
        }

        ss_.removeAll(remove);

        for (Cluster c : remove) {
            map_.remove(c.getId());
        }

    }

    public int size() {
        return map_.size();
    }

    public Collection<Cluster> getClusters() {
        return map_.values();
    }

    public Cluster getCluster(ClusterId id) {
        return map_.get(id);
    }

    public void calculateStatistics(Set<SnapId> snapshotsSet,
            double bestThroughputFraction) {
        for (Cluster c : map_.values()) {
            c.calculateStatistics(snapshotsSet, bestThroughputFraction);
        }
        normalizeAverageBottleneck();
    }

    public boolean addTunnel(ClusterId clusterId, Tunnel t, boolean checkDistance) {

        if (!map_.containsKey(clusterId)) {
            Cluster c = new Cluster(clusterId, cs_);
            map_.put(clusterId, c);
        }
        Cluster c = map_.get(clusterId);

        return c.addTunnel(t, checkDistance);

    }

    public double getDistance(ClusterId[] cis,
            LayeredTunnels lts) {

        Cluster c1 = map_.get(cis[0]);
        Cluster c2 = map_.get(cis[1]);

        if (c1 == null) {
            throw new RuntimeException();
        }
        if (c2 == null) {
            throw new RuntimeException();
        }

        double sum = 0;
        int count = 0;
        for (Tunnel x : c1.getTunnels()) {
            for (Tunnel y : c2.getTunnels()) {
                int xi = lts.getTnunelIndex(x);
                int yi = lts.getTnunelIndex(y);
                sum += lts.getDistance(xi, yi);
                count++;
            }
        }
        return sum / count;
    }

    public void merge(ClusterId[] childs, ClusterId parent) {
        Cluster c = new Cluster(parent, cs_);
        for (int i = 0; i < 2; i++) {
            Cluster child = this.map_.get(childs[i]);
            if (child == null) {
                Logger.getLogger("caver").log(Level.WARNING,
                        "You probably need to set "
                        + "''load_cluster_tree no''. " + "{0}*", childs[i]);
            }
            if (null == child) {
                throw new RuntimeException(parent.toString());
            }
            for (Tunnel t : child.getTunnels()) {
                //tunnelToCluster.put(t, c);
                c.addTunnel(t, false);
            }
        }
        for (int i = 0; i < 2; i++) {
            map_.remove(childs[i]);
        }
        map_.put(c.getId(), c);
    }

    public SortedSet<Cluster> computePriorities() {
        if (null == ss_ || ss_.size() != map_.size()) {
            ss_ = new TreeSet(new RelevanceComparator());
            for (Cluster c : map_.values()) {
                ss_.add(c);
            }

            int priority = 1;
            for (Cluster c : ss_) {
                if (c.getStatistics().getPriority() <= 0) {
                    Printer.warn("cost_function_exponent is probably too high "
                            + "(" + cs_.getCostFunctionExponent() + ")"
                            + ". "
                            + "Priorities of clusters 1 ... " + (priority - 1)
                            + " were computed correctly, but priorities of "
                            + " remaining clusters are zero and thus they "
                            + "will be ordered by bottleneck radius "
                            + "instead of priority.");
                    break;
                }
                priority++;
            }

        }
        int priority = 1;
        for (Cluster c : ss_) {
            c.setPriority(priority++);
        }

        return ss_;
    }

    public void computeTunnelPriorities() {
        for (List<Tunnel> list : getTunnelsBySnapshots().values()) {
            Collections.sort(list, new TunnelPriorityComparator());
            int priority = 1;
            for (Tunnel t : list) {
                t.setSnapshotPriority(priority++);
            }
        }
    }

    public void leaveCheapestTunnelPerSnapshot() {
        for (Cluster c : map_.values()) {
            c.leaveCheapestTunnelPerSnapshot();
        }
    }

    public void leaveRandomTunnelPerSnapshot(Random random) {
        for (Cluster c : map_.values()) {
            c.leaveRandomTunnelPerSnapshot(random);
        }
    }

    public int subsampleCheapest(int limitPerCluster) {
        int sum = 0;
        for (Cluster c : map_.values()) {
            sum += c.subsampleCheapest(limitPerCluster);
        }
        ss_ = null;
        return sum;
    }

    public int subsampleRandom(int limitPerCluster, Random random) {
        int sum = 0;
        for (Cluster c : map_.values()) {
            sum += c.subsampleRandom(limitPerCluster, random);
        }
        ss_ = null;
        return sum;
    }

    public void normalizeAverageBottleneck() {
        double max = Float.MIN_VALUE;
        for (Cluster c : map_.values()) {
            double avg =
                    c.getStatistics().getBottleneckDistribution().getAverage();
            if (max < avg) {
                max = avg;
            }
        }
        for (Cluster c : map_.values()) {
            c.getStatistics().calculateNormalizedAvgBottleneck(max);
        }
    }

    public List<Tunnel> getTunnels() {
        List<Tunnel> ts = new ArrayList<Tunnel>();
        for (Cluster c : getClusters()) {
            ts.addAll(c.getTunnels());
        }
        return ts;
    }

    public SortedMap<SnapId, List<Tunnel>> getTunnelsBySnapshots() {
        SortedMap<SnapId, List<Tunnel>> ts = new TreeMap<SnapId, List<Tunnel>>();
        for (Cluster c : getClusters()) {
            for (Tunnel t : c.getTunnels()) {
                SnapId id = t.getSnapId();
                if (!ts.containsKey(id)) {
                    ts.put(id, new ArrayList<Tunnel>());
                }
                ts.get(id).add(t);
            }
        }
        return ts;
    }

    public SortedSet<SnapId> getSnapshots() {
        SortedSet<SnapId> ts = new TreeSet<SnapId>();
        for (Cluster c : getClusters()) {
            for (Tunnel t : c.getTunnels()) {
                SnapId id = t.getSnapId();

                ts.add(id);
            }
        }
        return ts;
    }
}
