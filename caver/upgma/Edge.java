package upgma;

import java.util.HashSet;
import java.util.Set;

public class Edge implements Comparable {

    public int c1;
    public int c2;
    float value;

    public Edge() {
    }

    public float getValue() {
        return value;
    }

    public Edge(int c1, int c2, float value) {
        this.c1 = c1;
        this.c2 = c2;
        this.value = value;
    }

    public boolean contains(int cluster) {
        return c1 == cluster || c2 == cluster;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Edge) {
            Edge e = (Edge) o;
            boolean normal = ((c1 == e.c1) && (c2 == e.c2));
            boolean cross = ((c1 == e.c2) && (c2 == e.c1));

            return cross || normal;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.c1;
        hash = 47 * hash + this.c2;
        return hash;
    }

    public int compareTo(Object o) {
        if (!(o instanceof Edge)) {
            throw new ClassCastException("EdgeExpected");
        }

        if (this.equals(o)) {
            return 0;
        }

        float val = ((Edge) o).getValue();

        if (this.value - val > 0) {
            return 1;
        }
        if (this.value - val < 0) {
            return -1;
        }


        //TODO: promyslet sofistikovanejsi razeni, napr. ohledne souctu uzlu, 
        // pozor ale abych nevracel nikdy nulu, ta jen v pripade ze jsou skutecne rovne
        return 1;
    }

    // this edge is for example: 1, 4 and the other is 1, 5; what we need to get is
    int getDifferentFrom(int index) {
        if (c1 == index) {
            return c2;
        } else if (c2 == index) {
            return c1;
        } else {
            System.out.println("warning: not contained");
            return -1;
        }
    }

    // this je ta dulezita, od ktere se hleda rozdil ktery je urcen hranou e
    public int getDifferentFrom(Edge e) {
        Set<Integer> items = new HashSet<Integer>();
        items.add(e.c1);
        items.add(e.c2);
        items.add(c1);
        items.add(c2);


        items.remove(c1);
        items.remove(c2);

        if (items.size() != 1) {
            System.out.println("WARNING!" + this.toString() + "," + e.toString());
        }

        // only one should be there
        Integer c = null;
        for (Integer cc : items) {
            c = cc;
        }
        return c;


    }

    int getSharedWith(Edge e) {
        if (e.contains(c1)) {
            return c1;
        }
        if (e.contains(c2)) {
            return c2;
        }
        return -1;
    }

    public boolean isIncident(int clusterId) {
        return c1 == clusterId || c2 == clusterId;
    }

    @Override
    public String toString() {
        return "[" + c1 + "," + c2 + "," + value + "]";
    }
}
