package upgma;

import java.util.*;

public class NaiveUPGMA extends UPGMA {

    NaiveUPGMA(String string) {
        edgesFile = string;

        // load items
        List<Item> localItems = EdgeLoader.loadItems(edgesFile);
        for (Item i : localItems) {
            this.addItem(i);
        }


    }

    @Override
    public Cluster cluster() {

        // create one-item clusters
        for (Item i : items) {
            Cluster c = new Cluster();
            c.addItem(i);

            c.setId(clusters.size());
            clusters.add(c);
        }

        List<Edge> edges = EdgeLoader.loadEdges(edgesFile);
        int edgeCount = edges.size();


        Set<Edge> sortedEdges = new TreeSet<Edge>();
        sortedEdges.addAll(edges);

        System.out.println("PQ: size: " + sortedEdges.size());

        // all edges are within the queue
        Cluster result = null;
        while (!sortedEdges.isEmpty()) {

            // extract minimum
            Edge minimum = null;
            for (Edge etest : sortedEdges) {

                if (minimum == null) {
                    minimum = etest;
                }

                if (minimum.getValue() > etest.getValue()) {
                    minimum = etest;
                }

            }
            Edge e = minimum;

            Set<Edge> afterRemove = new TreeSet<Edge>();
            for (Edge ed : sortedEdges) {
                if (!ed.equals(e)) {
                    afterRemove.add(ed);
                }
            }
            sortedEdges = afterRemove;

            Cluster cnew = Cluster.mergeClusters(clusters.get(e.c1), clusters.get(e.c2));
            cnew.setId(clusters.size()); // set id and then add, i.e. for size 0 we add index 0 and insert, then size will be 1 and newly inserted will get 1
            cnew.setMergeDist(e.value);

            System.out.println("JOINED: " + clusters.get(e.c1).getItemIDS() + " + " + clusters.get(e.c2).getItemIDS() + " = " + cnew.getItemIDS() + "; " + e.value + " {" + e.c1 + " " + e.c2 + "} {" + cnew.getId() + "}");

            result = cnew;
            clusters.add(cnew);


            // fix neighbours

            // find clusters (l) joined with edges to i(c1) and j(c2)
            // (different from itself) and for each such cluster know its edges to i(c1) and j(c2)
            Map<Cluster, Reference> intersected = new HashMap<Cluster, Reference>();

            for (Edge testEdge : sortedEdges) {
                if (testEdge.contains(e.c1) || testEdge.contains(e.c2)) {
                    Cluster cluster = clusters.get(e.getDifferentFrom(testEdge));

                    // we have cluster l
                    if (!intersected.containsKey(cluster)) {
                        intersected.put(cluster, new Reference());
                    }

                    // compute correct values
                    Cluster i_or_j = clusters.get(testEdge.getSharedWith(e));
                    intersected.get(cluster).addEdge(testEdge, i_or_j.getSize());
                }
            }


            for (Cluster remote : intersected.keySet()) {
                int size = intersected.get(remote).getEdges().size();
                if (size != 2) {
                    System.out.println("WARNING!@!");
                }
            }


            // now!! intersected should hold two edges per cluster

            // intersected now holds clusters and for each there should be 
            // two edges (one to i and one to j)

            for (Cluster remote : intersected.keySet()) {
                // recompute distances
                // on the basis of simple computation

                float distance = Cluster.getUPGMADistance(intersected.get(remote));

                Edge en = new Edge(cnew.getId(), remote.getId(), distance);
                edgeCount++;

                // add new edge
                if (!sortedEdges.contains(en)) {
                    sortedEdges.add(en);
                }

                Set<Edge> newse = new TreeSet<Edge>();
                for (Edge se : sortedEdges) {
                    if (!intersected.get(remote).getEdges().contains(se)) {
                        newse.add(se);
                    }
                }
                sortedEdges = newse;

            }

        }

        return result;
    }
}
