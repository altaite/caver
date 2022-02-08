package algorithms.clustering;

import java.util.Comparator;

/*
 * Compares clusters using their priority. For more details, see description of
 * the cluster priority in: CAVER 3.0: A Tool for the Analysis of Transport
 * Pathways in Dynamic Protein Structures
 */
public class RelevanceComparator implements Comparator<Cluster> {

    public int compare(Cluster a, Cluster b) {
        int c = -Double.compare(
                a.getStatistics().getPriority(),
                b.getStatistics().getPriority());

        if (0 == c) { // equality prevention
            c = -Double.compare(
                    a.getStatistics().getBottleneckDistribution().getAverage(),
                    b.getStatistics().getBottleneckDistribution().getAverage());
        }

        if (0 == c) { // final equality prevention
            c = -Double.compare(a.getId().get(), b.getId().get());
        }
        return c;
    }
}
