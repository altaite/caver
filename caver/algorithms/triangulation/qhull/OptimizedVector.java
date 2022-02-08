package algorithms.triangulation.qhull;

public class OptimizedVector {

    public Object[] elementData;
    public int elementCount;

    public OptimizedVector() {
        this.elementData = new Object[20];
    }

    public OptimizedVector(int initial) {
        this.elementData = new Object[initial];
    }

    public void addElement(Object e) {
        if (this.elementData.length == this.elementCount) {
            Object[] newData = new Object[1 + 2 * this.elementData.length];

            System.arraycopy(this.elementData, 0, newData, 0,
                    this.elementCount);
            this.elementData = newData;
        }
        this.elementData[(this.elementCount++)] = e;
    }

    public void append(OptimizedVector l) {
        if (this.elementData.length < this.elementCount + l.elementCount) {
            Object[] newData = new Object[Math.max(1 + 2
                    * this.elementData.length, 1 + this.elementCount
                    + l.elementCount)];

            System.arraycopy(this.elementData, 0, newData, 0, this.elementCount);
            this.elementData = newData;
        }
        for (int i = 0; i < l.elementCount; i++) {
            this.elementData[(this.elementCount++)] = l.elementData[i];
        }
    }

    public Object elementAt(int i) {
        return this.elementData[i];
    }

    public int size() {
        return this.elementCount;
    }

    public void clear() {
        this.elementCount = 0;
    }
}
