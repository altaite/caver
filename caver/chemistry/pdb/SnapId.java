package chemistry.pdb;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Identifier of a molecular dynamics snapshot. The class enables to find
 * respective PDB file or possibly snapshot in other formats.
 *
 * If two SnapId references equals, they point to the same object.
 */
public class SnapId implements Serializable, Comparable<SnapId> {

    private String prefix_;
    private int number_;
    private String suffix_;
    private static Map<SnapId, SnapId> map = new HashMap<SnapId, SnapId>();

    private SnapId(String prefix, int number, String suffix) {
        prefix_ = prefix;
        number_ = number;
        suffix_ = suffix;
    }

    public int getNumber() {
        return number_;
    }

    public static SnapId create(File file) {

        String s = file.getName();
        int stage = 1;

        StringBuilder prefixSb = new StringBuilder();
        StringBuilder numberSb = new StringBuilder();
        StringBuilder suffixSb = new StringBuilder();

        for (char c : s.toCharArray()) {
            if (stage == 1) {
                if (Character.isDigit(c)) {
                    stage = 2;
                    numberSb.append(c);
                } else {
                    prefixSb.append(c);
                }
            } else if (2 == stage) {
                if (Character.isDigit(c)) {
                    numberSb.append(c);
                } else {
                    suffixSb.append(c);
                    stage = 3;
                }
            } else { // stage 3
                suffixSb.append(c);
            }
        }

        String prefix = prefixSb.toString();
        int number = 0;
        if (0 < numberSb.length()) {
            number = Integer.parseInt(numberSb.toString());
        }
        String suffix = suffixSb.toString();

        SnapId snap = new SnapId(prefix, number, suffix);
        if (!map.containsKey(snap)) {
            map.put(snap, snap);
        }
        return map.get(snap);
    }

    @Override
    public String toString() {
        return prefix_ + number_ + suffix_;
    }

    @Override
    public boolean equals(Object o) {
        SnapId snap = (SnapId) o;
        return prefix_.equals(snap.prefix_)
                && number_ == snap.number_
                && suffix_.equals(snap.suffix_);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.prefix_ != null ? this.prefix_.hashCode() : 0);
        hash = 97 * hash + this.number_;
        hash = 97 * hash + (this.suffix_ != null ? this.suffix_.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(SnapId id) {

        int c = prefix_.compareTo(id.prefix_);

        if (0 == c) {
            c = new Integer(number_).compareTo(id.number_);
            if (0 == c) {
                c = suffix_.compareTo(id.suffix_);
            }
        }

        return c;
    }
}
