package algorithms.clustering;

import java.io.Serializable;

/**
 *
 * Represents unique identifier of a cluster.
 */
public class ClusterId implements Serializable {

    private int id_;

    public static ClusterId create(int id) {
        ClusterId query = new ClusterId(id);
        return query;
    }

    private ClusterId(int id) {
        id_ = id;
    }

    public int get() {
        return id_;
    }

    @Override
    public boolean equals(Object o) {
        ClusterId ti = (ClusterId) o;
        return ti.id_ == id_;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.id_;
        return hash;
    }

    @Override
    public String toString() {
        return "" + id_;
    }
}
