package chemistry;

import geometry.primitives.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Monomeric part of a repeated element in a polymer, property number is index
 * in this chain. Two objects representing the same monomer may differ in
 * number, if one comes form Swissprot and the other from PDB. Other information
 * (appart from aa, number) are optional.
 */
public class Residue implements Comparable<Residue> {

    private String name_;
    private ResidueId id_;
    private SortedSet<Atom> atoms_ = new TreeSet<Atom>();

    public Residue(ResidueId index, String name, SortedSet<Atom> atoms) {
        this.name_ = name;
        this.id_ = index;
        this.atoms_ = atoms;
    }

    public ResidueId getId() {
        return id_;
    }

    public char getChain() {
        return id_.getChain();
    }

    public String getName() {
        return name_;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }
        if (!(o instanceof Residue)) {
            return false;
        }
        Residue r = (Residue) o;
        return id_.equals(r.id_);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.id_ != null ? this.id_.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Residue r) {
        return id_.compareTo(r.id_);
    }

    public SortedSet<Atom> getAtoms() {
        return atoms_;
    }

    public List<Atom> getNonHydrogenAtoms() {
        List<Atom> list = new ArrayList<Atom>();
        for (Atom a : atoms_) {
            if (!a.getElement().isHydrogen()) {
                list.add(a);
            }
        }
        return list;
    }

    public Atom getCarbonAlpha() {
        for (Atom a : atoms_) {
            if (a.getName().equals("CA")) {
                return a;
            }
        }
        return null;
    }

    public Atom getAtom(int atomSerial) {
        for (Atom a : atoms_) {
            if (a.getSerialNumber() == atomSerial) {
                return a;
            }
        }
        return null;
    }

    public Point getCenter() {
        Point p = new Point(0, 0, 0);
        for (Atom a : atoms_) {
            p = p.plus(a.getCenter());
        }
        p = p.divide(atoms_.size());

        return p;
    }

    public Double getCarbonAlphaTemperatureFactor() {
        for (Atom a : atoms_) {
            if ("CA".equals(a.getName().toUpperCase())) {
                return a.getTemperatureFactor();
            }
        }
        return null;
    }

    public double getAverageBFactor() {
        double sum = 0;
        for (Atom a : atoms_) {
            Double b = a.getTemperatureFactor();
            if (null == b) {
                b = 0.0;
            }
            sum += b;
        }
        if (atoms_.isEmpty()) {
            throw new IllegalStateException("Residue has no atoms");
        } else {
            return sum / atoms_.size();
        }
    }
}
