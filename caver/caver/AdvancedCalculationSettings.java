package caver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Manages parameters that will probably not be changed by ordinary user. For
 * other parameters see class CalculationSettings.
 */
public class AdvancedCalculationSettings {

    private File file_;
    private int minSurfaceNeighbours = 1;
    private boolean showExtrapolatedSurfacePoints = false;

    public AdvancedCalculationSettings(File file) throws IOException {

        file_ = file;
        if (file_.exists()) {
            readFile();
        }
    }

    public final void readFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file_));
        String line;
        while (null != (line = br.readLine())) {
            if (line.trim().length() == 0 || line.trim().startsWith("#")) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line, " \t=");
            String name = st.nextToken();
            String rest = line.substring(name.length()).trim();

            name = name.trim();
            String value;
            if (st.hasMoreTokens()) {
                value = st.nextToken().trim();
            } else {
                value = "";
            }
            if ("min_surface_neighbours".equals(name)) {
                minSurfaceNeighbours = Integer.parseInt(value);
            }
            if ("show_extrapolated_surface_points".equals(name)) {
                if ("yes".equals(value)) {
                    showExtrapolatedSurfacePoints = true;
                } else {
                    showExtrapolatedSurfacePoints = false;
                }
            }
        }


    }

    public int getMinSurfaceNeighbours() {
        return minSurfaceNeighbours;
    }

    public boolean showExtrapolatedSurfacePoints() {
        return showExtrapolatedSurfacePoints;
    }
}
