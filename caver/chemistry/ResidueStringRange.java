package chemistry;

public class ResidueStringRange {

    private String a_, b_;

    public ResidueStringRange(String a) {
        a_ = a;
        b_ = a;
    }

    public ResidueStringRange(String a, String b) {
        a_ = a;
        b_ = b;
    }

    public String getA() {
        return a_;
    }

    public String getB() {
        return b_;
    }

}
