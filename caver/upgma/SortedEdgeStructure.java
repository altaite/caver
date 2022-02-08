package upgma;

import java.util.*;

public final class SortedEdgeStructure implements Iterable<EdgeInterval> {

    private Map<Integer, Map<Integer, EdgeInterval>> internalStructure;
    private int size;
    private List<EdgeInterval> internalSearch = new ArrayList<EdgeInterval>();

    public SortedEdgeStructure() {

        internalStructure = new HashMap<Integer, Map<Integer, EdgeInterval>>();
        size = 0;
    }

    public int size() {
        return size;
    }

    public Iterator<EdgeInterval> iterator() {
        return internalSearch.iterator();
    }

    public List<EdgeInterval> getInternalSearch() {
        return this.internalSearch;
    }

    /**
     * Add edge which !CERTAINLY! is not present in the structure warning: not
     * safe if edge is present, causes inconsistency
     *
     * @param e, must satisfy e.c1 <= e.c2
     */
    void addEdgeWhichDidNotExistOrdered(EdgeInterval e) {

        Map secondIndex = internalStructure.get(e.c1);
        if (secondIndex == null) {
            secondIndex = new HashMap<Integer, EdgeInterval>();
            internalStructure.put(e.c1, secondIndex);
        }

        size++;
        secondIndex.put(e.c2, e);

        //BINSEARCH
        float eValue = e.getMaxValue();
        int leftIndex = 0;
        int rightIndex = internalSearch.size();

        while (leftIndex < rightIndex) {
            int middle = (leftIndex + rightIndex) / 2;
            if (internalSearch.get(middle).getMaxValue() >= eValue) {
                rightIndex = middle;
            } else {
                leftIndex = middle + 1;
            }
        }

        internalSearch.add(rightIndex, e);

    }

    /**
     * safe method for adding edges. If edge exists, it is fetched and erased
     * before adding this new edge e, important for sorting newly added edges
     *
     * @param e
     */
    public void addEdge(EdgeInterval e) {
        // switch indices if not ascending
        if (e.c1 > e.c2) {
            int tmp = e.c2;
            e.c2 = e.c1;
            e.c1 = tmp;
        }

        float previousValue = this.containsEdgeOrdered(e.c1, e.c2) ? this.getEdgeOrdered(e.c1, e.c2).getMaxValue() : -1.f;

        Map secondIndex = internalStructure.get(e.c1);
        if (secondIndex == null) {
            secondIndex = new HashMap<Integer, EdgeInterval>();
            internalStructure.put(e.c1, secondIndex);
        }

        if (!secondIndex.containsKey(e.c2)) {
            size++;
        }
        secondIndex.put(e.c2, e);

        this.internalSearchRemoveIfContains(e.c1, e.c2, previousValue);

        float eValue = e.getMaxValue();

        //BINSEARCH
        int leftIndex = 0;
        int rightIndex = internalSearch.size();

        while (leftIndex < rightIndex) {
            int middle = (leftIndex + rightIndex) / 2;
            if (internalSearch.get(middle).getMaxValue() >= eValue) {
                rightIndex = middle;
            } else {
                leftIndex = middle + 1;
            }
        }
        internalSearch.add(rightIndex, e);

    }

    public EdgeInterval getEdge(int i, int j) {
        if (i <= j) {
            return internalStructure.get(i).get(j);
        } else {
            return internalStructure.get(j).get(i);
        }
    }

    // i <= j certainly!
    public EdgeInterval getEdgeOrdered(int i, int j) {
        return internalStructure.get(i).get(j);
    }

    /**
     * removes edge with the smallest value, i.e. it is situated in .get(0) in
     * internalSearch WARNING: use with caution, the removed edge i, j has to be
     * the first !!! WARNING: and the edge i, j has to be present in the set
     * certainly !!! IMPORTANT: i <= j must hold
     *
     *
     *
     *
     *
     *
     *
     *
     *

     *
     * @param i
     * @param j
     */
    public void removeEdgeFirst(int i, int j) {

        internalStructure.get(i).remove(j);
        size--;
        this.internalSearch.remove(0);



    }

    /**
     * removes edge i,j from the structure
     *
     * @param i
     * @param j
     */
    // optimize for sorted i, j
    public void removeEdge(int i, int j) {

        float previousValue = -1;

        if (i <= j) {
            if (this.containsEdgeOrdered(i, j)) {
                previousValue = this.getEdgeOrdered(i, j).getMaxValue();
                size--;
            } else {
                return; //nothing to remove
            }
            internalStructure.get(i).remove(j);
        } else {
            if (this.containsEdgeOrdered(j, i)) {
                previousValue = this.getEdgeOrdered(j, i).getMaxValue();
                size--;
            } else {
                return; //nothing to remove
            }
            internalStructure.get(j).remove(i);

        }

        this.internalSearchRemoveIfContains(i, j, previousValue);

    }

    public void removeEdgeOrdered(int i, int j) {

        float previousValue = -1;

        if (this.containsEdgeOrdered(i, j)) {
            previousValue = this.getEdgeOrdered(i, j).getMaxValue();
            size--;
        } else {
            return; //nothing to remove
        }
        internalStructure.get(i).remove(j);
        this.internalSearchRemoveIfContains(i, j, previousValue);


    }

    public boolean containsEdge(int i, int j) {
        if (i <= j) {
            if (internalStructure.get(i) == null
                    || !internalStructure.get(i).containsKey(j)) {
                return false;
            }
        } else {
            if (internalStructure.get(j) == null
                    || !internalStructure.get(j).containsKey(i)) {
                return false;
            }
        }
        return true;
    }

    // i<= j certainly
    public boolean containsEdgeOrdered(int i, int j) {
        if (internalStructure.get(i) == null
                || !internalStructure.get(i).containsKey(j)) {
            return false;
        }
        return true;
    }

    /**
     * returns all integers which participate in edges leading from i1 or i2 to
     * any other node
     *
     * @param i1
     * @param i2
     * @return
     */
    public Set<Integer> getIntersected(int i1, int i2) {
        Set<Integer> result = new HashSet<Integer>();
        for (EdgeInterval ei : internalSearch) {

            if (ei.c1 == i1 || ei.c1 == i2) {
                result.add(ei.c2);
            } else if (ei.c2 == i1 || ei.c2 == i2) {
                result.add(ei.c1);
            }

        }
        return result;
    }

    public void rebuild() {
        //System.out.println("rebuilding search structure...");
        Collections.sort(internalSearch);
    }

    void checkMerged(List<Cluster> clusters) {
        System.out.println("==== =checking");
        for (EdgeInterval e : internalSearch) {
            if (clusters.get(e.c1).isMerged() || clusters.get(e.c2).isMerged()) {
                System.out.println("STG: WRONG" + e.toString());
            }
        }
    }

    public EdgeInterval getFirstEdge() {
        return this.internalSearch.get(0);

    }

    void internalSearchRemoveIfContains(int ec1, int ec2, float previousValue) {

        if (previousValue < 0) {
            return;
        }

        int leftIndex = 0;


        int rightIndex = internalSearch.size();

        while (leftIndex < rightIndex) {

            int middle = (leftIndex + rightIndex) / 2;
            float middleValue = internalSearch.get(middle).getMaxValue();
            if (middleValue > previousValue) {
                rightIndex = middle;
            } else if (middleValue < previousValue) {
                leftIndex = middle + 1;
            } else {
                break;
            }



        }

        for (int i = leftIndex; i < rightIndex; i++) {
            if (internalSearch.get(i).isEqual(ec1, ec2)) {
                internalSearch.remove(i);
                break;
            }
        }
    }

    void addEdgeNoSort(EdgeInterval e) {
        // switch indices if not ascending
        if (e.c1 > e.c2) {
            int tmp = e.c2;
            e.c2 = e.c1;
            e.c1 = tmp;
        }

        Map secondIndex = internalStructure.get(e.c1);
        if (secondIndex == null) {
            secondIndex = new HashMap<Integer, EdgeInterval>();
            internalStructure.put(e.c1, secondIndex);
        }

        if (secondIndex.put(e.c2, e) == null) {
            size++;
        }

        internalSearch.add(e);

    }
}
