package upgma;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Reference {

    private Set<Edge> edges = new HashSet<Edge>();
    private Map<Edge, Integer> sizes = new HashMap<Edge, Integer>();

    public Set<Edge> getEdges() {
        return edges;
    }

    public int getRemoteClusterSize(Edge e) {
        return sizes.get(e);
    }

    public void addEdge(Edge e, int remoteSize) {
        edges.add(e);
        sizes.put(e, remoteSize);
    }
}
