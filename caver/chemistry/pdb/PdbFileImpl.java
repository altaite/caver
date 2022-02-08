package chemistry.pdb;

import chemistry.Atom;
import chemistry.Residue;
import chemistry.ResidueId;
import chemistry.pdb.exceptions.AtomNotFoundException;
import chemistry.pdb.exceptions.ResidueNotFoundException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class for access to ATOM and HETATM sections of PDB file.
 * http://www.bmsc.washington.edu/CrystaLinks/man/pdb/guide2.2_frame.html
 *
 */
class PdbFileImpl implements PdbFile {

    private SortedMap<ResidueId, Residue> residues_ =
            new TreeMap<ResidueId, Residue>();
    private SortedMap<Integer, Atom> atoms_;
    private boolean locked = false;
    private File file_;
    // makes growth (not removal) impossible after first getter usage
    // immutability here seems impractical, might be removed

    protected PdbFileImpl(File file) {
        this.file_ = file;
    }

    @Override
    public File getFile() {
        return file_;
    }

    /*
     * @return Residue object or null.
     *
     */
    @Override
    public Residue getResidue(ResidueId ri)
            throws ResidueNotFoundException {
        Residue r = null;
        if (residues_.containsKey(ri)) {
            r = residues_.get(ri);
        }
        if (null == r) {
            throw new ResidueNotFoundException(ri);
        }
        return r;
    }

    /*
     * @return Atom object or null.
     */
    @Override
    public Atom getAtom(int serialNumber) throws AtomNotFoundException {
        Atom a = null;
        if (getAtoms().containsKey(serialNumber)) {
            a = getAtoms().get(serialNumber);
        }
        if (null == a) {
            throw new AtomNotFoundException(serialNumber);
        }
        return a;
    }

    protected void addResidue(Residue r) {
        if (locked) {
            throw new IllegalStateException();
        }
        if (residues_.containsKey(r.getId())) {
            System.err.println("Residue " + r.getId()
                    + " was already constructed.");
        }
        residues_.put(r.getId(), r);
    }

    @Override
    public SortedMap<ResidueId, Residue> getResidues() {
        locked = true;
        return residues_;
    }

    @Override
    public SortedMap<Integer, Atom> getAtoms() {
        locked = true;
        if (null == atoms_) {
            atoms_ = new TreeMap<Integer, Atom>();
            for (Residue r : residues_.values()) {
                for (Atom a : r.getAtoms()) {
                    if (atoms_.containsKey(a.getSerialNumber())) {
                        throw new RuntimeException("Atom serial number "
                                + a.getSerialNumber() + " is not unique.");
                    }
                    atoms_.put(a.getSerialNumber(), a);
                }
            }
        }
        return atoms_;
    }

    @Override
    public void saveAsPdb(BufferedWriter bw) throws IOException {
        for (ResidueId ri : residues_.keySet()) {
            Residue r = residues_.get(ri);
            for (Atom a : r.getAtoms()) {
                PdbLine pl = new PdbLine(
                        a.getSerialNumber(), a.getName(),
                        r.getName(), ri.getSequenceNumber(), ri.getChain(),
                        a.getCenter().getX(),
                        a.getCenter().getY(),
                        a.getCenter().getZ());
                bw.write(pl.getPdbString() + "\n");
            }
        }
    }

    @Override
    public void remove(ResidueId ri) {
        try {
            Residue r = getResidue(ri);
            for (Atom a : r.getAtoms()) {
                getAtoms().remove(a.getSerialNumber());
            }
            residues_.remove(ri);
        } catch (ResidueNotFoundException e) {
        }
    }
}
