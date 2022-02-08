package chemistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Contains residue specific radii. User can provide such a file and has
 * its own more precise radii, compared to VDW radii from PeriodicTable class.
 *
 */
public class AtomRadii {

    private static Map<String, Map<String, Double>> radii;

    public AtomRadii(File file) throws IOException {
        radii = new HashMap<String, Map<String, Double>>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while (null != (line = br.readLine())) {
            if (line.startsWith("#") || line.startsWith("resi")) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line, " ");
            String residueName = st.nextToken().toUpperCase();
            String atomName = st.nextToken().toUpperCase();
            st.nextToken();
            st.nextToken();
            st.nextToken();
            double radius = Double.parseDouble(st.nextToken());

            if (!radii.containsKey(residueName)) {
                radii.put(residueName, new HashMap<String, Double>());
            }
            if (radii.get(residueName).containsKey(atomName)) {
                System.err.println("Warning: residue specific radii file "
                        + file + " contains duplicated atom " + atomName
                        + " for residue " + residueName);
            }

            radii.get(residueName).put(atomName, radius);
        }
        br.close();
    }

    /**
     * If nothing is found within residue context, residue name *
     * is searched.
     */
    public Double getRadius(String residueName, String atomName) {
        residueName = residueName.toUpperCase();
        atomName = atomName.toUpperCase();

        Double r = null;

        if (radii.containsKey(residueName)) {
            if (radii.get(residueName).containsKey(atomName)) {
                r = radii.get(residueName).get(atomName);
            }
        }

        if (null == r) {
            if (radii.containsKey("*")) {
                if (radii.get("*").containsKey(atomName)) {
                    r = radii.get("*").get(atomName);
                }
            }
        }

        return r;
    }
}
