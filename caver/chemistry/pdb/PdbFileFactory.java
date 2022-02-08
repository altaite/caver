package chemistry.pdb;

import caver.Printer;
import chemistry.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Class for creation of PdbFile instances.
 */
public class PdbFileFactory {

    private final PeriodicTable periodicTable_;
    private final AtomRadii atomRadii_;
    private String chainLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
    private boolean verbose = false;

    /*
     * Argument periodicTable holds information associated with chemical element
     * of atom (see chemistry.ChemicalElement). Argument atomRadii holds atom
     * radii which takes into account position of atoms in molecule.
     */
    public PdbFileFactory(PeriodicTable periodicTable, AtomRadii atomRadii) {
        periodicTable_ = periodicTable;
        atomRadii_ = atomRadii;
    }

    /*
     * Use if chemical context radii file is not available - Van der Waals radii
     * based on chemical elements will be used instead.
     */
    public PdbFileFactory(PeriodicTable periodicTable) {
        periodicTable_ = periodicTable;
        atomRadii_ = null;
    }

    public void setVerbose(boolean b) {
        verbose = b;
    }

    private double getVdwRadius(ChemicalElement element) {
        double r;
        if (null == element.getVanDerWaalsRadius()) {
            r = periodicTable_.getDefaultVdwR();
            System.err.println("Unknown VDW radius for "
                    + "element " + element.getSymbol() + ". "
                    + "Using radius of " + r + " A. To change the value, "
                    + "edit element " + element.getProtonNumber() + " "
                    + "(for this element only) or 0 (for all unknown radii) in "
                    + periodicTable_.getFile());
        } else {
            r = element.getVanDerWaalsRadius();
        }
        return r;
    }

    private int getAtomsCount(File file, Integer modelId) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        boolean opened = null == modelId;
        int model;
        int count = 0;
        while (null != (line = br.readLine())) {
            if (PdbLine.isCoordinateLine(line)) {
                if (opened) {
                    count++;
                }
            } else if (line.startsWith("MODEL")) {
                StringTokenizer st = new StringTokenizer(line, " \t");
                st.nextToken();
                model = new Integer(st.nextToken());
                if (modelId != null && model == modelId) {
                    opened = true;
                }
            } else if (line.startsWith("ENDMDL")) {
                if (opened) {
                    break;
                }
            }

        }
        br.close();
        return count;
    }

    /*
     * Use only if PDB file has multiple MODEL. Then, modelId is the number on
     * the MODEL line.
     */
    public PdbFile create(File file, Integer modelId)
            throws IOException {

        PdbFileImpl pdbFile = new PdbFileImpl(file);
        SortedMap<ResidueId, List<PdbLine>> pdbLines =
                new TreeMap<ResidueId, List<PdbLine>>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        boolean opened = null == modelId;
        int model;
        ResidueId oldRi = null;
        List<PdbLine> batch = new ArrayList<PdbLine>();
        boolean finished = false;



        while (!finished) {

            line = br.readLine();
            if (null == line) {
                finished = true;
            }

            if (finished || PdbLine.isCoordinateLine(line)) {
                if (opened) {
                    try {

                        PdbLine pl = null;
                        ResidueId ri = null;
                        if (finished) {
                            ri = oldRi;
                        } else {
                            pl = new PdbLine(line);
                            ri = pl.getResidueId();
                        }



                        if ((finished && null != oldRi)
                                || (null != oldRi && !ri.equals(oldRi))) {
                            // residue change or EOF

                            if (!pdbLines.containsKey(oldRi)) {
                                pdbLines.put(oldRi, batch);
                                batch = new ArrayList<PdbLine>();
                            } else { // ResidueId collision

                                ResidueId newRi =
                                        new ResidueId(oldRi.getChain(),
                                        oldRi.getSequenceNumber(),
                                        oldRi.getInsertionCode());

                                while (pdbLines.containsKey(newRi)) {
                                    newRi = new ResidueId(newRi.getChain(),
                                            newRi.getSequenceNumber() + 1,
                                            newRi.getInsertionCode());
                                }

                                batch = new ArrayList<PdbLine>();
                            } // residue collision
                        } // residue change
                        if (null != pl) { // might be if EOF
                            batch.add(pl);
                            oldRi = ri;
                        }
                    } catch (Exception e) {
                        Logger.getLogger("caver").log(Level.SEVERE, line, e);
                    }
                }
            } else if (line.startsWith("MODEL")) {
                StringTokenizer st = new StringTokenizer(line, " \t");
                st.nextToken();
                model = new Integer(st.nextToken());
                if (modelId != null && model == modelId) {
                    opened = true;
                }
            } else if (line.startsWith("ENDMDL")) {
                if (opened) {

                    finished = true;
                }
            }
        }

        br.close();
        int count = 0;
        for (ResidueId ri : pdbLines.keySet()) {
            for (PdbLine l : pdbLines.get(ri)) {
                count++;
            }
        }
        int linesCount = getAtomsCount(file, modelId);
        if (verbose) {
            System.out.println("PDB file " + file + " has "
                    + linesCount + " ATOM or HETATOM lines, "
                    + count + " lines processed.");
        }
        if (count != linesCount) {
            System.err.println("Counted " + count + " != loaded " + linesCount);

        }

        assert count == linesCount;

        HashSet<Atom> uniqueAtoms = new HashSet<Atom>();
        int maxAtomSerial = 0;

        SortedMap<ResidueId, SortedSet<Character>> removedConformationsByResidue =
                new TreeMap<ResidueId, SortedSet<Character>>();
        Set<Integer> atomsRemoved = new TreeSet<Integer>();
        Map<ResidueId, Set<Integer>> removedAtomsByResidue =
                new TreeMap<ResidueId, Set<Integer>>();
        boolean remove = true;

        Map<ResidueId, List<PdbLine>> toRemove =
                new TreeMap<ResidueId, List<PdbLine>>();

        for (ResidueId ri : pdbLines.keySet()) {


            Set<Character> altLetters = new HashSet<Character>();
            for (PdbLine l : pdbLines.get(ri)) {
                char alt = l.getAlternateLocationIndicator();
                if (' ' != alt) {
                    altLetters.add(alt);
                }

            }
            boolean present = (2 <= altLetters.size());

            if (present) {


                SortedMap<Character, List<PdbLine>> alts =
                        new TreeMap<Character, List<PdbLine>>();
                for (PdbLine l : pdbLines.get(ri)) {
                    char alt = l.getAlternateLocationIndicator();
                    if (' ' != alt) {
                        if (!alts.containsKey(alt)) {
                            alts.put(alt, new ArrayList<PdbLine>());
                        }
                        alts.get(alt).add(l);
                    }
                }

                boolean equal = true;
                {
                    List<Integer> list = new ArrayList<Integer>();
                    for (Collection c : alts.values()) {
                        list.add(c.size());
                    }
                    for (int i = 1; i < list.size(); i++) {
                        if (list.get(i - 1) != list.get(i)) {
                            equal = false;
                        }
                    }
                }

                if (!equal) {
                    remove = false;
                }
                if (remove) {
                    Map<Character, Double> values = new HashMap<Character, Double>();
                    Map<Character, Integer> counts = new HashMap<Character, Integer>();


                    for (char alt : alts.keySet()) {
                        for (PdbLine l : alts.get(alt)) {
                            Double occ = l.getOccupancy();
                            if (null != occ) {
                                if (!values.containsKey(alt)) {
                                    values.put(alt, occ);
                                    counts.put(alt, 1);
                                } else {
                                    values.put(alt, values.get(alt) + occ);
                                    counts.put(alt, counts.get(alt) + 1);
                                }
                            }

                        }
                    }


                    SortedSet<AverageOccupancy> ao =
                            new TreeSet<AverageOccupancy>();
                    for (char alt : values.keySet()) {
                        ao.add(new AverageOccupancy(
                                alt, values.get(alt) / counts.get(alt)));
                    }
                    char best = ao.last().location_;

                    if (!toRemove.containsKey(ri)) {
                        toRemove.put(ri, new ArrayList<PdbLine>());
                    }

                    for (PdbLine l : pdbLines.get(ri)) {
                        char alt = l.getAlternateLocationIndicator();
                        if (best != alt && ' ' != alt) {
                            toRemove.get(ri).add(l);
                        }
                    }
                }
            }
        }

        // remove alt. confs.

        if (remove) {
            for (ResidueId ri : toRemove.keySet()) {
                for (PdbLine l : toRemove.get(ri)) {
                    pdbLines.get(ri).remove(l);

                    if (!removedConformationsByResidue.containsKey(l.getResidueId())) {
                        removedConformationsByResidue.put(l.getResidueId(), new TreeSet<Character>());
                    }
                    removedConformationsByResidue.get(l.getResidueId()).add(
                            l.getAlternateLocationIndicator());
                    int ai = l.getAtomSerialNumber();
                    atomsRemoved.add(ai);

                    if (!removedAtomsByResidue.containsKey(ri)) {
                        removedAtomsByResidue.put(ri, new TreeSet<Integer>());
                    }
                    removedAtomsByResidue.get(ri).add(ai);
                }
            }


            if (!atomsRemoved.isEmpty()) {
                StringBuilder sb = new StringBuilder();

                for (ResidueId rid : removedConformationsByResidue.keySet()) {
                    sb.append(rid.toString()).append(" (removed conformation ");
                    for (char alt : removedConformationsByResidue.get(rid)) {
                        sb.append(alt).append(", ");
                    }
                    sb.append("atoms ");
                    for (int ai : removedAtomsByResidue.get(rid)) {
                        sb.append(ai).append(" ");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append("), ");
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.deleteCharAt(sb.length() - 1);

                Printer.println("Alternative conformations were removed from residues "
                        + sb.toString() + " in file " + file, Printer.IMPORTANT);

                StringBuilder sba = new StringBuilder();

                for (int i : atomsRemoved) {
                    sba.append(i).append(", ");
                }
                sba.deleteCharAt(sba.length() - 1);
                sba.deleteCharAt(sba.length() - 1);


            }

        } else {
            Printer.warn("Difficult case of alternative conformations detected, "
                    + "please remove them manually.");
        }

        // construct results

        for (ResidueId ri : pdbLines.keySet()) {

            String residueName = null;
            SortedSet<Atom> atoms = new TreeSet<Atom>();

            for (PdbLine l : pdbLines.get(ri)) {
                Double radius;
                ChemicalElement element = periodicTable_.getChemicalElement(
                        l.getElementSymbol());
                String atomName = l.getAtomName();
                if (null == atomRadii_) {
                    radius = getVdwRadius(element);
                } else {
                    radius = atomRadii_.getRadius(
                            l.getResidueName(), atomName);
                    if (null == radius) {
                        radius = getVdwRadius(element);
                        System.err.println("Warning: residue specific "
                                + "radius not found for "
                                + l.getResidueName() + " "
                                + l.getAtomName() + ". "
                                + "Using VDW radius " + radius + ".");
                    }
                }

                Atom a = new Atom(element, l.getAtomSerialNumber(), ri,
                        l.getX(), l.getY(), l.getZ(), radius,
                        l.getTemperatureFactor(), l.getAtomName());
                if (uniqueAtoms.contains(a)) {
                    a.setSerialNumber(maxAtomSerial + 1);
                }

                atoms.add(a);
                uniqueAtoms.add(a);
                if (maxAtomSerial < a.getSerialNumber()) {
                    maxAtomSerial = a.getSerialNumber();
                }

                if (residueName != null && !l.getResidueName().equals(residueName)) {
                    System.err.println("Multiple residue names under "
                            + "one residue ID (chain + residue serial "
                            + "number + insertion code). Please assign "
                            + "different chains to different residues with "
                            + "same serial numbers."
                            + l.getResidueId() + "  *  " + residueName + " / ");
                }
                residueName = l.getResidueName();

            }
            Residue r = new Residue(ri, residueName, atoms);
            for (Atom a : atoms) {
                a.setResidue(r);
            }
            pdbFile.addResidue(r);
        }


        return pdbFile;
    }

    public PdbFile create(File file) throws IOException {
        PdbFile pf = create(file, null);
        return pf;
    }
}
