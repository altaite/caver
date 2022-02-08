package algorithms.clustering.statistics;

import chemistry.Atom;

public class AtomCounter {

    Atom atom_;
    int count_ = 0;

    public AtomCounter(Atom atom) {
        this.atom_ = atom;
    }

    public AtomCounter(Atom atom, int count) {
        this.atom_ = atom;
        this.count_ = count;
    }

    public void inc() {
        count_++;
    }

    public Atom getAtom() {
        return atom_;
    }

    public int getCount() {
        return count_;
    }
}
