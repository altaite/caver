package chemistry.pdb.exceptions;

import chemistry.ResidueId;

/*
 * Thrown when an residue collection (such as PDBFile or MolecularSystem) is
 * queried for a residue which it does not contain.
 */
public class ResidueNotFoundException extends Exception {

    private ResidueId residueId_;

    public ResidueNotFoundException(ResidueId ri) {
        this.residueId_ = ri;
    }

    public ResidueId getResidueId() {
        return this.residueId_;
    }
}
