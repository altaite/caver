package chemistry.pdb;

/*
 * Occupancies of atoms from PDB file averaged over residue.
 */
public class AverageOccupancy implements Comparable<AverageOccupancy> {

    double value_;
    char location_;

    public AverageOccupancy(char location, double d) {
        value_ = d;
        location_ = location;
    }

    @Override
    public int compareTo(AverageOccupancy ao) {
        if (this.value_ < ao.value_) {
            return -1;
        }
        if (ao.value_ < this.value_) {
            return 1;
        } else {
            return new Character(this.location_).compareTo(ao.location_);
        }
    }
}
