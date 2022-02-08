package upgma;

public final class EdgeInterval implements Comparable {

    public int c1;
    public int c2;
    private float value;
    private float minValue;
    private float maxValue;

    EdgeInterval(Edge e) {
        this.c1 = e.c1;
        this.c2 = e.c2;
        this.value = e.value;
    }

    EdgeInterval(int c1, int c2, float value) {
        this.c1 = c1;
        this.c2 = c2;
        this.value = value;
    }

    EdgeInterval(int c1, int c2, float value, float min, float max) {
        this.c1 = c1;
        this.c2 = c2;
        this.value = value;
        this.minValue = min;
        this.maxValue = max;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    void setValue(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EdgeInterval) {
            EdgeInterval e = (EdgeInterval) o;
            return ((c1 == e.c1) && (c2 == e.c2)) || ((c1 == e.c2) && (c2 == e.c1));
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

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof EdgeInterval)) {
            throw new ClassCastException("EdgeExpected");
        }

        float comp = maxValue - ((EdgeInterval) o).maxValue;
        if (comp > 0) {
            return 1;
        } else if (comp < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "[" + c1 + "," + c2 + "," + value + "][" + this.getMinValue() + "," + this.getMaxValue() + "]";
    }

    boolean isEqual(int ec1, int ec2) {
        return (this.c1 == ec1 && this.c2 == ec2) || (this.c1 == ec2 && this.c2 == ec1);
    }
}
