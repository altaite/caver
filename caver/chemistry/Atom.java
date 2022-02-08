package chemistry;

import geometry.primitives.Point;
import geometry.primitives.Sphere;

/**
 * Stores information about atom from PDB file.
 */
public class Atom implements Comparable<Atom> {

    private int serialNumber_;
    private Point position_;
    private ChemicalElement element_;
    private Double temperatureFactor_; // B-factor
    private String name_;
    private double radius_;
    private ResidueId residueId_;
    private Residue residue_; // non-obligatory field, the only settable

    public Atom(ChemicalElement element, int serialNumber, ResidueId residueId,
            double x, double y, double z, double radius,
            Double temperatureFactor, String name) {
        element_ = element;
        serialNumber_ = serialNumber;
        residueId_ = residueId;
        position_ = new Point(x, y, z);
        temperatureFactor_ = temperatureFactor;
        name_ = name;
        radius_ = radius;
    }

    public int getSerialNumber() {
        return serialNumber_;
    }

    public ResidueId getResidueId() {
        return residueId_;
    }

    public Residue getResidue() {
        return residue_;
    }

    public void setResidue(Residue r) {
        assert (null == residue_) : "Atom residue set twice";
        assert residueId_.equals(r.getId());
        residue_ = r;
    }

    public void setSerialNumber(int number) {
        serialNumber_ = number;
    }

    public Point getCenter() {
        return position_;
    }

    public Sphere getSphere() {
        return new Sphere(getCenter(), getRadius());
    }

    public ChemicalElement getElement() {
        return element_;
    }

    public Double getTemperatureFactor() {
        return temperatureFactor_;
    }

    public String getName() {
        return name_;
    }

    public boolean isBackbone() {
        if (name_.trim().toUpperCase().equals("H")
                || name_.trim().toUpperCase().equals("N")
                || name_.trim().toUpperCase().equals("CA")
                || name_.trim().toUpperCase().equals("HA")
                || name_.trim().toUpperCase().equals("C")
                || name_.trim().toUpperCase().equals("O")) {
            return true;
        } else {
            return false;
        }
    }

    public double getRadius() {
        return radius_;
    }

    @Override
    public String toString() {
        return element_.getSymbol() + " " + serialNumber_ + " " + 
                position_.toString() + " r="
                + radius_;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + this.serialNumber_;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Atom)) {
            return false;
        }
        Atom a = (Atom) o;
        return this.serialNumber_ == a.serialNumber_;
    }

    @Override
    public int compareTo(Atom a) {
        return new Integer(this.serialNumber_).compareTo(a.serialNumber_);
    }
}
