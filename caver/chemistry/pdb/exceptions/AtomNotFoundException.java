package chemistry.pdb.exceptions;

/*
 * Thrown when an atom collection (such as PDBFile or MolecularSystem) is
 * queried for an atom which it does not contain.
 */
public class AtomNotFoundException extends Exception {

    private int atomSerial_;

    public AtomNotFoundException(int atomSerial) {
        this.atomSerial_ = atomSerial;
    }

    public int getAtomSerial() {
        return this.atomSerial_;
    }
}
