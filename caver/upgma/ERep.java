package upgma;

public final class ERep {

    public float value;
    public int count;

    public ERep(float value, int count) {
        this.value = value;
        this.count = count;
    }

    ERep() {
        this.value = 0.f;
        this.count = 0;
    }

    public void add(float value, int count) {
        this.value += value;
        this.count += count;
    }

    // the (+) implementation
    public static ERep add(ERep e1, ERep e2) {
        return new ERep(e1.value + e2.value, e1.count + e2.count);
    }

    // the (+) implementation
    // allows that e1 or e2 may be null
    public static ERep addNullAllowed(ERep e1, ERep e2) {
        int ec = 0;
        float ev = 0.f;

        if (e1 != null) {
            ec += e1.count;
            ev += e1.value;
        }
        if (e2 != null) {
            ec += e2.count;
            ev += e2.value;
        }

        return new ERep(ev, ec);
    }
}
