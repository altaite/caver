package upgma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class Cluster {

    protected Cluster child1 = null;
    protected Cluster child2 = null;
    protected int id;
    private boolean isMerged = false;
    private float mergeDist = 0.f;
    private int size = 0;
    private Cluster topMostParent = this;

    public Cluster getTopMostParent() {
        return topMostParent;
    }

    public float getMergeDist() {
        return mergeDist;
    }

    public void setMergeDist(float mergeDist) {
        this.mergeDist = mergeDist;
    }

    public void setMerged() {
        this.isMerged = true;
    }

    public boolean isMerged() {
        return this.isMerged;
    }

    public Cluster() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;

    }

    public void addItem(Item i) {
        size = 1;
    }

    /**
     * Retrieve all item id-s under this cluster. Due to the fact, that cluster
     * id-s reflect item id-s if only one item is within a cluster, we can use
     * the construction below
     *
     * @param cluster parent to start recursive search from
     * @return list of item indices (= list of one-item cluster indices)
     */
    private List<Integer> getItemsRecursive() {
        if (!this.hasChildren()) {
            ArrayList<Integer> result = new ArrayList<Integer>();
            result.add(this.getId());
            return result;
        }

        List<Integer> result = new ArrayList<Integer>();

        result.addAll(child1.getItemsRecursive());
        result.addAll(child2.getItemsRecursive());
        return result;
    }

    public String getItemIDS() {
        List<Integer> indices = this.getItemsRecursive();
        StringBuilder sb = new StringBuilder();
        Collections.sort(indices);

        for (int i : indices) {
            sb.append(i).append(" ");
        }
        return "[" + sb.toString().trim() + "]";

    }

    public int getSize() {
        return size;
    }

    public static Cluster mergeClusters(Cluster c1, Cluster c2) {
        Cluster c = new Cluster();

        c.size = c1.size + c2.size;

        c.child1 = c1;
        c.child2 = c2;

        c1.setMerged();
        c2.setMerged();

        return c;
    }

    public static float getDistanceF2(float arg1, int size1, float arg2, float size2) {
        return (arg1 * size1 + arg2 * size2) / (size1 + size2);
    }

    /*
     * Solution based on "Efficient algorithms for accurate hierarchical
     * clustering of huge datasets: tackling the entire protein space"
     */
    public static float getUPGMADistance(Reference ref) {


        Set<Edge> edges = ref.getEdges();
        //if(edges.size() != 2)
        //    System.out.println("W:" + edges.size());
        float sumMultipliers = 0;
        float sumSizes = 0;

        for (Edge e : edges) {
            //System.out.println("e: " + e.toString());
            sumMultipliers += e.getValue() * ref.getRemoteClusterSize(e);
            sumSizes += ref.getRemoteClusterSize(e);
        }

        return sumMultipliers / sumSizes;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cluster)) {
            return false;
        }
        return ((Cluster) o).getId() == id;

    }

    @Override
    public int hashCode() {
        //TODO: design the hashcode precisely so that it is fast
        return this.id % 1000;
    }

    public boolean hasChildren() {
        return this.child1 != null && this.child2 != null;
    }

    /**
     * Set topmost parent of this cluster
     *
     * @param c
     */
    private void setTopMostParent(Cluster c) {
        this.topMostParent = c;
    }

    /**
     * Traverse all clusters below this cluster and for each which is a leaf,
     * i.e. contains only one item, set "c" as the top-most parent
     *
     * @param c Cluster to be set as top-most parent
     */
    void setTopMostParents(Cluster c) {

        if (child1.isLeaf()) {
            child1.setTopMostParent(c);
        } else {
            child1.setTopMostParents(c);
        }


        if (child2.isLeaf()) {
            child2.setTopMostParent(c);
        } else {
            child2.setTopMostParents(c);
        }
    }

    private boolean isLeaf() {
        return !this.hasChildren();
    }
}
