package chemistry;

public class ResidueRange {

    private ResidueId a_, b_;

    public ResidueRange(ResidueId a) {
        a_ = a;
        b_ = a;
    }

    public ResidueRange(ResidueId a, ResidueId b) {
        a_ = a;
        b_ = b;
    }

    public ResidueId getA() {
        return a_;
    }

    public ResidueId getB() {
        return b_;
    }

    public boolean within(ResidueId ri) {
        if (a_.compareTo(ri) <= 0 && ri.compareTo(b_) <= 0) {
            return true;
        } else {
            return false;
        }
    }
}
