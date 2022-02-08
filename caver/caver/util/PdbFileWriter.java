package caver.util;

import chemistry.pdb.PdbLine;
import geometry.primitives.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Class to help developers to visualize points using PDB files (and molecular
 * visualization software such as PyMOL)
 */
public class PdbFileWriter {

    int serial_ = 1;
    int resi_ = 1;
    File file_;

    public PdbFileWriter(File file) {
        file_ = file;
        file_.delete();
    }

    public void savePoint(Point p) {
        try {
            FileOutputStream out = new FileOutputStream(file_, true);
            PrintStream ps = new PrintStream(out);
            PdbLine pl = new PdbLine(serial_, "H", "POI", resi_ + "", 'P',
                    p.getX(), p.getY(), p.getZ());
            ps.println(pl.getPdbString());
            serial_++;

            ps.close();
        } catch (IOException e) {
            Logger.getLogger("caver").log(
                    Level.WARNING, file_.getAbsolutePath(), e);
        }
    }

    public void increaseResidue() {
        resi_++;
    }

    public void setResidue(int i) {
        resi_ = i;
    }
}
