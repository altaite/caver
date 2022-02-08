package upgma;

import java.util.HashMap;
import java.util.Map;

public final class PartialValues {

    private Map<Integer, Map<Integer, ERep>> internalStructure;
    private int size;

    public PartialValues() {
        internalStructure = new HashMap<Integer, Map<Integer, ERep>>();
        size = 0;
    }

    public void add(int i, int j, ERep er) {

        if (i <= j) {
            Map secondIndex = internalStructure.get(i);
            if (secondIndex == null) {
                secondIndex = new HashMap<Integer, ERep>();
                internalStructure.put(i, secondIndex);
            }
            if (!secondIndex.containsKey(j)) {
                size++;
            }
            secondIndex.put(j, er);

        } else {
            Map secondIndex = internalStructure.get(j);
            if (secondIndex == null) {
                secondIndex = new HashMap<Integer, ERep>();
                internalStructure.put(j, secondIndex);
            }
            if (!secondIndex.containsKey(i)) {
                size++;
            }
            secondIndex.put(i, er);

        }

    }

    public void addOrdered(int i, int j, ERep er) {

        Map secondIndex = internalStructure.get(i);
        if (secondIndex == null) {
            secondIndex = new HashMap<Integer, ERep>();
            internalStructure.put(i, secondIndex);
        }
        if (!secondIndex.containsKey(j)) {
            size++;
        }
        secondIndex.put(j, er);

    }

    public ERep get(int i, int j) {
        if (i <= j) {
            if (!internalStructure.containsKey(i)) {
                return null;
            }
            return internalStructure.get(i).get(j);
        } else {
            if (!internalStructure.containsKey(j)) {
                return null;
            }
            return internalStructure.get(j).get(i);
        }
    }

    public ERep getOrdered(int i, int j) {
        if (!internalStructure.containsKey(i)) {
            return null;
        }
        return internalStructure.get(i).get(j);
    }

    public boolean containsERep(int i, int j) {
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

    void remove(int i, int j) {
        if (i <= j) {
            if (internalStructure.get(i) == null) {
                return;
            }
            internalStructure.get(i).remove(j);

        } else {
            if (internalStructure.get(j) == null) {
                return;
            }
            internalStructure.get(j).remove(i);
        }
    }

    void removeOrdered(int i, int j) {
        if (internalStructure.get(i) == null) {
            return;
        }
        internalStructure.get(i).remove(j);
    }
}
