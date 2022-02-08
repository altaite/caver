package chemistry.pdb;

import chemistry.ResidueId;
import java.util.Arrays;
import java.util.Locale;

/*
 * Represents a line of a PDB file. For information about PDB files, see PDB
 * file format guide. Contains data from ATOM or HETATM line of PDB file.
 *
 * The class provides parsing of PDB line from String and then saving the
 * information into String.
 */
public class PdbLine {

    private boolean heteroAtom_;
    private int atomSerialNumber_;
    // atom name described in PDB file format guide
    private String atomName_;
    String afterChemicalSymbol_; // can be further parsed decomposed and
    // used to identify specific atoms in amino acids or nucleotides
    private char alternateLocationIndicator_;
    private String residueName_;
    private char chainId_;
    private String residueSequenceNumber_; // String becaus of Gromacs
    // 9999 is not sufficient for water boxes in simulations, so non-digit
    // characters are also used
    private char insertionCode_;
    private double x_, y_, z_;
    private double r_; // for PQR export
    private Double occupancy_;
    private Double temperatureFactor_;
    private String segmentId_;
    private String charge_;
    String chemicalSymbol_; // derived from atom name
    String elementSymbol_; // from dedicated columns 77-78
    // end of line properties
    private String line_;
    // derived
    private String element_; // final chemical element symbol
    private ResidueId residueId_;
    private String atomNamePrefix_;
    private static Locale locale_ = Locale.ENGLISH;

    private PdbLine() {
    }

    public PdbLine(
            int atomSerialNumber, String atomName,
            String residueName, String residueSequenceNumber, char chainId,
            double x, double y, double z) {

        if (99999 < atomSerialNumber) {
            atomSerialNumber = 99999;
        }

        if (1 == atomName.length()) {
            atomName = " " + atomName + "  ";
        } else if (2 == atomName.length()) {
            atomName = " " + atomName + " ";
        } else if (3 == atomName.length()) {
            atomName = " " + atomName;
        }

        heteroAtom_ = false;
        atomSerialNumber_ = atomSerialNumber;

        setAtomName(atomName);

        alternateLocationIndicator_ = ' ';
        residueName_ = residueName;
        chainId_ = chainId;

        residueSequenceNumber_ = residueSequenceNumber;
        insertionCode_ = ' ';
        x_ = x;
        y_ = y;
        z_ = z;
        occupancy_ = null;
        temperatureFactor_ = null;
        segmentId_ = null;
        charge_ = null;

        residueId_ = new ResidueId(chainId_, residueSequenceNumber_,
                insertionCode_);

        initElement();
    }

    public PdbLine(String line) {
        line_ = line;
        String recordName = sub(1, 6).trim();
        if (recordName.startsWith("HETATM")) {
            heteroAtom_ = true;
        } else if (recordName.startsWith("ATOM")) {
            heteroAtom_ = false;
        } else {
            throw new RuntimeException("PdbLine can be used only on ATOM or"
                    + " HETATM lines.");
        }
        try {
            atomSerialNumber_ = new Integer(sub(7, 11).trim());
        } catch (NumberFormatException e) {
            System.out.println(line);

            throw e;
        }

        setAtomName(sub(13, 16));

        alternateLocationIndicator_ = sub(17);
        residueName_ = sub(18, 20);
        chainId_ = sub(22);
        residueSequenceNumber_ = sub(23, 26).trim();
        insertionCode_ = sub(27);
        x_ = new Double(sub(31, 38).trim());
        y_ = new Double(sub(39, 46).trim());
        z_ = new Double(sub(47, 54).trim());
        if (60 <= line.length()) {
            occupancy_ = getDouble(sub(55, 60));
            if (66 <= line.length()) {
                temperatureFactor_ = getDouble(sub(61, 66));
                if (76 <= line.length()) {
                    segmentId_ = sub(73, 76);
                    // LString, spacing is important
                    if (79 <= line.length()) {
                        elementSymbol_ = sub(77, 78);
                        // LString, spacing is important
                        if (80 <= line.length()) {
                            charge_ = sub(79, 80);
                            // LString, spacing is important
                        }
                    }
                }
            }
        }
        this.line_ = null;

        initElement();

        residueId_ =
                new ResidueId(chainId_, residueSequenceNumber_, insertionCode_);
    }

    public static boolean isCoordinateLine(String line) {
        return line.startsWith("ATOM") || line.startsWith("HETATM");
    }

    public final void setAtomName(String name) {
        atomName_ = name;
        String s;
        if (Character.isDigit(atomName_.charAt(0))) { // 1HD1 LEU
            s = atomName_.substring(1);
            atomNamePrefix_ = atomName_.substring(0, 1);
        } else {
            s = atomName_;
        }
        atomName_ = atomName_.trim();

        if ('H' == s.charAt(0)) {
            chemicalSymbol_ = "H";
            afterChemicalSymbol_ = s.substring(1).trim();
        } else {
            if (1 == s.length()) {
                chemicalSymbol_ = s;
                afterChemicalSymbol_ = "";
            } else {
                chemicalSymbol_ = s.substring(0, 2).trim();
                afterChemicalSymbol_ = s.substring(2);
            }
        }

    }

    private Double getDouble(String s) {
        if (0 == s.trim().length()) {
            return null;
        } else {
            return new Double(s);
        }

    }

    public String getAtomNamePrefix() {
        return atomNamePrefix_;
    }

    private void initElement() {
        if (null == elementSymbol_ || 0 == elementSymbol_.trim().length()) {
            element_ = chemicalSymbol_;
        } else {
            element_ = elementSymbol_.trim();
        }
    }

    @Override
    public String toString() {
        System.err.println("Use PdbLine.getPdbString().");
        return getPdbString();
    }

    /*
     * Constructs and returns an ATOM or HETATM line of PDB file.
     */
    public String getPdbString() {

        char[] spaces = new char[80];
        Arrays.fill(spaces, ' ');
        StringBuilder sb = new StringBuilder(new String(spaces));

        if (isHeteroAtom()) {
            printLeft("HETATM", 1, 6, sb);
        } else {
            printLeft("ATOM", 1, 6, sb);
        }

        printRight(Integer.toString(atomSerialNumber_), 7, 11, sb);
        if (("H".equals(getElementSymbol())
                && atomName_.length() == 4)
                || Character.isDigit(atomName_.charAt(0))
                || getElementSymbol().length() == 2) {
            printLeft(atomName_, 13, 16, sb);
        } else {
            printLeft(atomName_, 14, 16, sb);
        }

        printLeft(Character.toString(alternateLocationIndicator_), 17, 17, sb);
        printRight(residueName_, 18, 20, sb);
        printLeft(Character.toString(chainId_), 22, 22, sb);

        String resiString = residueSequenceNumber_;
        try {
            int resi = Integer.parseInt(residueSequenceNumber_);
            if (999 < resi) {
                resi = 999;
            }
            resiString = String.valueOf(resi);
        } catch (Exception e) {
        }
        printRight(resiString, 23, 26, sb);
        printLeft(Character.toString(insertionCode_), 27, 27, sb);
        if (x_ < -999.999) {
            x_ = -999.999;
        }
        if (y_ < -999.999) {
            y_ = -999.999;
        }
        if (z_ < -999.999) {
            z_ = -999.999;
        }
        if (9999.999 < x_) {
            x_ = 9999.999;
        }
        if (9999.999 < y_) {
            y_ = 9999.999;
        }
        if (9999.999 < z_) {
            z_ = 9999.999;
        }

        printRight(String.format(locale_, "%8.3f", x_), 31, 38, sb);
        printRight(String.format(locale_, "%8.3f", y_), 39, 46, sb);
        printRight(String.format(locale_, "%8.3f", z_), 47, 54, sb);
        if (null != occupancy_) {
            printRight(String.format(locale_, "%6.2f", occupancy_), 55, 60, sb);
        }
        if (null != temperatureFactor_) {
            if (999.99 < temperatureFactor_) {
                temperatureFactor_ = 999.99;
            }
        }
        if (null != temperatureFactor_) {
            printRight(String.format(locale_, "%6.2f", temperatureFactor_),
                    61, 66, sb);
        }
        if (null != segmentId_) {
            printLeft(segmentId_, 73, 76, sb);
        }
        if (null != elementSymbol_) {
            printRight(elementSymbol_, 77, 78, sb);
        }
        if (null != charge_) {
            printLeft(charge_, 79, 80, sb);
        }

        return sb.toString();

    }

    public String getPqrString() {

        Locale locale = null;
        char[] spaces = new char[80];
        Arrays.fill(spaces, ' ');
        StringBuilder sb = new StringBuilder(new String(spaces));

        if (isHeteroAtom()) {
            printLeft("HETATM", 1, 6, sb);
        } else {
            printLeft("ATOM", 1, 6, sb);
        }

        printRight(Integer.toString(atomSerialNumber_), 7, 11, sb);
        if (("H".equals(getElementSymbol())
                && atomName_.length() == 4)
                || Character.isDigit(atomName_.charAt(0))
                || getElementSymbol().length() == 2) {
            printLeft(atomName_, 13, 16, sb);
        } else {
            printLeft(atomName_, 14, 16, sb);
        }

        printLeft(Character.toString(alternateLocationIndicator_), 17, 17, sb);
        printRight(residueName_, 18, 20, sb);
        printLeft(Character.toString(chainId_), 22, 22, sb);
        printRight(residueSequenceNumber_, 23, 26, sb);
        printLeft(Character.toString(insertionCode_), 27, 27, sb);
        if (x_ < -999.999) {
            x_ = -999.999;
        }
        if (y_ < -999.999) {
            y_ = -999.999;
        }
        if (z_ < -999.999) {
            z_ = -999.999;
        }
        if (9999.999 < x_) {
            x_ = 9999.999;
        }
        if (9999.999 < y_) {
            y_ = 9999.999;
        }
        if (9999.999 < z_) {
            z_ = 9999.999;
        }

        printRight(String.format(locale, "%8.3f", x_), 31, 38, sb);
        printRight(String.format(locale, "%8.3f", y_), 39, 46, sb);
        printRight(String.format(locale, "%8.3f", z_), 47, 54, sb);


        double charge = 0.0;
        if (null != charge_) {
            charge = Double.parseDouble(charge_);
        }
        printRight(String.format(locale, "%1.4f", charge), 56, 62, sb);


        double radius = r_;
        if (radius < 0) {
            radius = 0;
        }
        if (100 <= radius) {
            radius = 99.999;
        }
        if (10 <= radius) {
            printRight(String.format(locale, "%2.3f", radius), 64, 69, sb);
        } else {
            printRight(String.format(locale, "%1.4f", radius), 64, 69, sb);
        }



        return sb.toString();

    }

    private void printLeft(String s, int first, int last, StringBuilder sb) {
        first -= 1;
        last -= 1;
        s = s.trim();
        if (last - first + 1 < s.length()) {
            throw new RuntimeException(s + " " + first + " " + last);
        }
        last = Math.min(last, first + s.length() - 1);
        if (s.length() > last - first + 1) {
            throw new RuntimeException(s + " " + first + " " + last);
        }
        for (int i = 0; i < s.length(); i++) {
            sb.setCharAt(first + i, s.charAt(i));
        }
    }

    private void printRight(String s, int first, int last, StringBuilder sb) {
        first -= 1;
        last -= 1;
        s = s.trim();
        if (last - first + 1 < s.length()) {
            throw new RuntimeException(s + " " + first + " " + last);
        }
        for (int i = s.length() - 1; 0 <= i; i--) {
            sb.setCharAt(last - s.length() + 1 + i, s.charAt(i));
        }

    }

    private String sub(int first, int last) {
        return line_.substring(first - 1, last);

    }

    private char sub(int index) {
        return line_.charAt(index - 1);

    }

    public int getAtomSerialNumber() {
        return atomSerialNumber_;
    }

    public String getAtomName() {
        return atomName_;
    }

    public char getAlternateLocationIndicator() {
        return alternateLocationIndicator_;
    }

    public String getResidueName() {
        return residueName_;
    }

    public double getX() {
        return x_;
    }

    public double getY() {
        return y_;
    }

    public double getZ() {
        return z_;
    }

    public void setX(double x) {
        x_ = x;
    }

    public void setY(double y) {
        y_ = y;
    }

    public void setZ(double z) {
        z_ = z;
    }

    public void setR(double r) {
        r_ = r;
    }

    public boolean isHeteroAtom() {
        return heteroAtom_;
    }

    public Double getOccupancy() {
        return occupancy_;
    }

    public Double getTemperatureFactor() {
        return temperatureFactor_;
    }

    public String getSegmentId() {
        return segmentId_;
    }

    public String getCharge() {
        return charge_;
    }

    public String getElementSymbol() {
        return element_;
    }

    public ResidueId getResidueId() {
        return residueId_;
    }

    public char getChainId() {
        return chainId_;
    }

    public void setOccupancy(Double occupancy) {
        this.occupancy_ = occupancy;
    }

    public void setTemperatureFactor(Double temperatureFactor) {
        this.temperatureFactor_ = temperatureFactor;
    }

    public void setSegmentId_(String segmentId) {
        this.segmentId_ = segmentId;
    }

    public void setCharge_(String charge) {
        this.charge_ = charge;
    }

    public void setAtomSerialNumber(int n) {
        atomSerialNumber_ = n;
    }

    public void setResidueId(ResidueId ri) {
        this.residueId_ = ri;
        this.chainId_ = ri.getChain();
        this.residueSequenceNumber_ = ri.getSequenceNumber();
        this.insertionCode_ = ri.getInsertionCode();
    }
}
