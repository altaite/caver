package chemistry;

public class AtomRange {

    private int a_, b_;

    public AtomRange(int a) {
        a_ = a;
        b_ = a;
    }

    public AtomRange(int a, int b) {
        a_ = a;
        b_ = b;
    }

    public int getA() {
        return a_;
    }

    public int getB() {
        return b_;
    }

    public boolean within(int serial) {
        return a_ <= serial && serial <= b_;
    }

}
