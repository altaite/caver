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

/**
 * Interface for access to ATOM and HETATM sections of PDB file.
 * http://www.bmsc.washington.edu/CrystaLinks/man/pdb/guide2.2_frame.html
 * 
 * For more advanced manipulation see the class PdbFileProcessor. 
 */
public interface PdbFile {

    public File getFile();

    public Residue getResidue(ResidueId residueId)
            throws ResidueNotFoundException;

    public Atom getAtom(int serialNumber)
            throws AtomNotFoundException;

    public SortedMap<ResidueId, Residue> getResidues();

    /*
     * Indexed by atom serial number from PDB file, i.e. Atom.getSerialNumber()
     */
    public SortedMap<Integer, Atom> getAtoms();

    public void saveAsPdb(BufferedWriter bw)
            throws IOException;

    public void remove(ResidueId ri);
}
