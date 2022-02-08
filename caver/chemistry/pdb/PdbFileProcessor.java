package chemistry.pdb;

import chemistry.Atom;
import chemistry.Residue;
import chemistry.ResidueId;
import chemistry.pdb.exceptions.AtomNotFoundException;
import chemistry.pdb.exceptions.ResidueNotFoundException;
import geometry.primitives.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Class for advanced queries over PDB files.
 */
public class PdbFileProcessor implements PdbFile {

    PdbFile pf_;
    private Map<Integer, ResidueId> atomToResidue =
            new HashMap<Integer, ResidueId>();

    public PdbFileProcessor(PdbFile pdbFile) {
        this.pf_ = pdbFile;
        for (Residue r : pf_.getResidues().values()) {
            for (Atom a : r.getAtoms()) {
                atomToResidue.put(a.getSerialNumber(), r.getId());
            }
        }
    }

    public char guessMostImportantChain() {
        char chain = ' ';
        for (Residue r : pf_.getResidues().values()) {
            if (r.getChain() < chain || (' ' == chain && ' ' != r.getChain())) {
                chain = r.getChain();
            }
        }
        return chain;
    }

    public PdbFile getPdbFile() {
        return pf_;
    }

    @Override
    public Residue getResidue(ResidueId residueId)
            throws ResidueNotFoundException {
        return pf_.getResidue(residueId);
    }

    @Override
    public Atom getAtom(int serialNumber) throws AtomNotFoundException {
        Atom a = pf_.getAtom(serialNumber);
        return a;
    }

    @Override
    public SortedMap<ResidueId, Residue> getResidues() {
        return pf_.getResidues();
    }

    @Override
    public SortedMap<Integer, Atom> getAtoms() {
        return pf_.getAtoms();
    }

    /*
     * Input: A:123A OR 123A (chain will be guessed)
     */
    public Residue guessResidue(String s) throws ResidueNotFoundException {
        char chain;
        String rest;

        if (2 <= s.length() && ':' == s.charAt(1)) {
            chain = s.charAt(0);
            rest = s.substring(2);
        } else {
            chain = guessMostImportantChain();
            rest = s;
        }
        ResidueId ri = new ResidueId(chain, rest);

        return pf_.getResidue(ri);
    }

    public ResidueId getResidueIdForAtomId(int serialNumber) {
        return atomToResidue.get(serialNumber);
    }

    public Residue getResidueForAtom(Atom a) throws AtomNotFoundException {
        try {
            ResidueId ri = atomToResidue.get(a.getSerialNumber());
            return getResidue(ri);
        } catch (ResidueNotFoundException e) {
            throw new RuntimeException("Residue for atom "
                    + a.getSerialNumber() + " not found.");
        }
    }

    public Set<Atom> stringsToAtoms(Iterable<String> it) {
        Set<Atom> set = new HashSet<Atom>();
        for (String ai : it) {
            try {
                Atom a = getAtom(Integer.parseInt(ai));
                set.add(a);
            } catch (AtomNotFoundException e) {
                Logger.getLogger("caver").log(Level.WARNING, "", e);
            }
        }
        return set;
    }

    public Point getResidueAndAtomsCenter(Iterable<String> residuesIt,
            Iterable<String> atomsIt) throws ResidueNotFoundException {
        Point p = new Point(0, 0, 0);

        Set<Residue> residues = new HashSet<Residue>();
        for (String s : residuesIt) {
            residues.add(guessResidue(s));
        }


        for (Residue r : residues) {
            p = p.plus(r.getCenter());
        }

        Set<Atom> atoms = stringsToAtoms(atomsIt);
        for (Atom a : atoms) {
            p = p.plus(a.getCenter());
        }

        p = p.divide(residues.size() + atoms.size());
        return p;
    }

    @Override
    public void saveAsPdb(BufferedWriter bw) throws IOException {
        pf_.saveAsPdb(bw);
    }

    @Override
    public void remove(ResidueId ri) {
        try {
            for (Atom a : getResidue(ri).getAtoms()) {
                atomToResidue.remove(a.getSerialNumber());
            }
            pf_.remove(ri);
        } catch (ResidueNotFoundException e) {
            Logger.getLogger("caver").log(Level.WARNING, "", e);
        }
    }

    @Override
    public File getFile() {
        return pf_.getFile();
    }
}
