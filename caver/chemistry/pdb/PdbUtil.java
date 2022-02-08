package chemistry.pdb;

import geometry.primitives.AnnotatedPoint;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import geometry.search.SpaceTree;
import java.io.*;
import java.util.*;

/*
 * Helper class for conversions of PDB files and their ordering in molecular
 * dynamic trajectory.
 */
public class PdbUtil {

    public enum Visualizer {

        VMD, PyMOL
    }

    public static SortedSet<Integer> allAtomSerials(File pdb)
            throws IOException {
        SortedSet<Integer> serials = new TreeSet<Integer>();

        BufferedReader br = new BufferedReader(
                new FileReader(pdb));
        String line;
        while (null != (line = br.readLine())) {
            if (PdbLine.isCoordinateLine(line)) {
                PdbLine pl = new PdbLine(line);
                serials.add(pl.getAtomSerialNumber());
            }
        }
        br.close();
        return serials;
    }

    private static int getMaxAtomSerial(File file) throws IOException {
        BufferedReader br = new BufferedReader(
                new FileReader(file));
        String line;
        int max = 0;
        while (null != (line = br.readLine())) {

            if (PdbLine.isCoordinateLine(line)) {
                try {
                    PdbLine pl = new PdbLine(line);
                    int serial = pl.getAtomSerialNumber();
                    if (max < serial) {
                        max = serial;
                    }
                } catch (Exception e) {
                    System.err.println(line);
                    throw new RuntimeException(e);
                }
            }
        }
        br.close();
        return max;
    }

    public static void finalizeFile(Visualizer visualizer,
            File in, File out) throws IOException {

        int max = getMaxAtomSerial(in);

        BufferedReader br = new BufferedReader(
                new FileReader(in));

        BufferedWriter bw = new BufferedWriter(new FileWriter(out));
        String line;
        PdbLine pdbLine = null;
        int serial = 0;
        boolean first = true;

        while (null != (line = br.readLine())) {
            if (line.startsWith("MODEL")) {
                bw.write(line + "\n");
            } else if (line.startsWith("ENDMDL")) {
                // faking of missing atom serials in this model
                if (first || visualizer == Visualizer.VMD) {
                    while (++serial <= max) {
                        pdbLine.setAtomSerialNumber(serial);
                        bw.write(pdbLine.getPdbString() + "\n");

                    }
                }
                bw.write(line + "\n");
                first = false;
            } else if (PdbLine.isCoordinateLine(line)) {
                pdbLine = new PdbLine(line);
                serial = pdbLine.getAtomSerialNumber();
                bw.write(line + "\n");
            }
        }

        bw.close();
        br.close();
    }

    /*
     * Extracts all digits from file names and orders files by resulting
     * integer.
     */
    public static SortedMap<SnapId, File> getTrajectoryFiles(File directory,
            int sparsity, int firstFrame, int lastFrame) {


        FileFilter fileFilter = new FileFilter() {

            public boolean accept(File file) {
                return file.getName().endsWith("pdb");
            }
        };
        File[] files = directory.listFiles(fileFilter);

        if (null == files || 0 == files.length) {
            System.err.println("Directory " + directory
                    + " must contain PDB files of your trajectory.");
        }

        SortedMap<SnapId, File> map = new TreeMap<SnapId, File>();

        for (File f : files) {
            SnapId snap = SnapId.create(f);
            if (map.containsKey(snap)) {
                System.err.println("Warning: trajectory"
                        + " file " + f + "with ID " + snap + " repeated.");
            }
            map.put(snap, f);
        }

        int i = 1;
        Set<SnapId> remove = new HashSet<SnapId>();
        for (SnapId snap : map.keySet()) {
            if (i < firstFrame || lastFrame < i || 0 != (i - firstFrame)
                    % sparsity) {
                remove.add(snap);
            }
            i++;
        }

        for (SnapId snap : remove) {
            map.remove(snap);
        }
        return map;
    }

    public static String getModelString(int modelNumber) {
        return "MODEL        " + modelNumber;
    }

    public static boolean areSpheresDissimilar(Collection<Sphere> spheres,
            double sphereSimilarity) {
        SpaceTree<Sphere> st = new SpaceTree<Sphere>();
        for (Sphere sphere : spheres) {
            AnnotatedPoint<Sphere> close = st.close(sphere.getS(),
                    sphereSimilarity);
            if (null != close) { // minimal bond length He 0.64

                Point a = close.getPoint();
                Point b = sphere.getS();
                System.err.println("Similar " + a + " " + b);

                return false;
            } else {
                st.add(sphere.getS(), sphere);
            }
        }
        return true;
    }

    public static void saveSpheres(Collection<Sphere> spheres, File file)
            throws IOException {
        int serial = 1;
        PrintStream ps = new PrintStream(new FileOutputStream(file));
        for (Sphere s : spheres) {

            PdbLine pdbLine = new PdbLine(
                    serial, "H", "APP", "1", 'A',
                    s.getS().getX(),
                    s.getS().getY(),
                    s.getS().getZ());
            pdbLine.setTemperatureFactor(s.getR());
            ps.println(pdbLine.getPdbString());
            serial++;
        }
        ps.close();

    }
}
