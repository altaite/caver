package chemistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class PeriodicTable {

    private SortedMap<String, ChemicalElement> elements_ =
            new TreeMap<String, ChemicalElement>();
    private double defaultVdwR_ = 2;
    private File file_;

    public PeriodicTable(File file) throws IOException {
        file_ = file;
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        br.readLine();
        while (null != (line = br.readLine())) {
            if (0 == line.trim().length()) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line, ";");
            int z = new Integer(st.nextToken());

            String symbol = st.nextToken();
            String name = st.nextToken();
            String svdw = st.nextToken();



            Double vdw = null;
            if (!svdw.contains("no data")) {
                vdw = new Double(svdw) / 100;
            }


            if (0 == z && null != vdw) {
                defaultVdwR_ = vdw;
            }

            ChemicalElement e = new ElementImpl(z, symbol, name, vdw);

            elements_.put(e.getSymbol(), e);
        }

        br.close();

        ChemicalElement e = new ElementImpl(0, "X", "Unknown", defaultVdwR_);
        elements_.put(e.getSymbol(), e);
    }

    public double getDefaultVdwR() {
        return defaultVdwR_;
    }

    public File getFile() {
        return file_;
    }

    public static String formatCase(String symbol) {
        symbol = symbol.trim();
        String result = Character.toUpperCase(symbol.charAt(0)) + "";
        if (2 == symbol.length()) {
            result += Character.toLowerCase(symbol.charAt(1));
        }
        return result;
    }

    /*
     * Returns null if symbol is not recognized.
     */
    public ChemicalElement getChemicalElement(String symbol) {
        symbol = formatCase(symbol);
        if (elements_.containsKey(symbol)) {
            if (null == elements_.get(symbol)) {
                throw new RuntimeException();
            }
            return elements_.get(symbol);
        } else {
            return elements_.get("X"); // unknown element
        }
    }
}
