package caver.ui;

import caver.Printer;
import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;
import geometry.primitives.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;


/*
 * Generator of colors for tunnel visualization. Allows to create a pallete of
 * colors which are dissimilar and sufficiently bright or dark (depending on
 * black or white background).
 *
 */
public class PalletteGenerator {

    KDTree<Color> kdTree;
    private final int CN = 10000;
    private final int N = 100;
    private List<Color> order = new ArrayList<Color>();

    public PalletteGenerator() {
        kdTree = new KDTree<Color>(3);

    }

    private void add(Color c) {
        try {
            kdTree.insert(c.getCoords(), c);
            order.add(c);
        } catch (KeySizeException e) {
            throw new RuntimeException(e);
        } catch (KeyDuplicateException e) {
            throw new RuntimeException(e);
        }

    }

    private double nearest(Color c) {
        try {
            if (kdTree.size() == 0) {
                return Double.MAX_VALUE;
            }
            Color n = kdTree.nearest(c.getCoords());
            return c.distance(n);
        } catch (KeySizeException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void generate() {
        List<Color> candidates = new ArrayList<Color>();
        Random random = new Random(1);
        for (int i = 0; i < CN; i++) {

            Point p = null;

            Point[] fs = new Point[100];
            Point u = new Point(1, 1, 1);
            for (int j = 0; j < fs.length; j++) {
                fs[j] = u.multiply((double) j / fs.length);
            }

            double brightness = 0;
            double min = 0;
            while (min < 0.2 || brightness < 0.5 || 1.0 < brightness) {
                p = new Point(random.nextDouble(), random.nextDouble(), random.nextDouble());
                min = Double.MAX_VALUE;
                for (Point f : fs) {
                    double d = f.distance(p);
                    if (d < min) {
                        min = d;
                    }
                }

                brightness = p.size();
            }

            candidates.add(new Color(p.getX(), p.getY(), p.getZ()));
        }
        int counter = 0;
        while (!candidates.isEmpty() && counter++ < N) {
            int best = -1;
            double greatestDist = 0;
            for (int i = 0; i < candidates.size(); i++) {
                double dist = nearest(candidates.get(i));
                if (-1 == best || greatestDist < dist) {
                    best = i;
                    greatestDist = dist;
                }

            }
            Color color = candidates.remove(best);
            Printer.println("Color " + color + " number " + counter
                    + " out of " + N + " placed into RGB space.");
            add(color);

        }

    }

    private String ds(double d) {
        return "" + (double) Math.round(d * 100) / 100;
    }

    public void save(File file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        for (Color c : order) {
            bw.write(ds(c.getRed()) + " "
                    + ds(c.getGreen()) + " "
                    + ds(c.getBlue()) + "\n");
        }

        bw.close();
    }

    public void savePymol(File file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        int i = 1;

        bw.write("from pymol import cmd\n");

        for (Color c : order) {

            bw.write("cmd.set_color('caver" + i + "',[" + ds(c.getRed()) + ","
                    + ds(c.getGreen()) + ","
                    + ds(c.getBlue()) + "])\n");
            i++;
        }

        bw.close();
    }

    public void saveVmd(File file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        int i = 1;

        for (Color c : order) {

            bw.write("color change rgb " + i + " " + ds(c.getRed()) + " "
                    + ds(c.getGreen()) + " "
                    + ds(c.getBlue()) + "\n");
            i++;
        }

        bw.close();
    }

    public void load(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while (null != (line = br.readLine())) {
            if (0 == line.trim().length()) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line, " \t,;");
            add(new Color(
                    Double.parseDouble(st.nextToken()),
                    Double.parseDouble(st.nextToken()),
                    Double.parseDouble(st.nextToken())));
        }
        br.close();
    }

    public static void main(String[] args) throws IOException {
        PalletteGenerator pg = new PalletteGenerator();
        File fixed = new File("rgb.fixed.txt");
        pg.load(fixed);
        pg.generate();
        pg.save(new File("rgb.txt"));
        pg.savePymol(new File("rgb.py"));
        pg.saveVmd(new File("rgb.tcl"));
    }
}

class Color {

    private double r, g, b;

    public Color(double r, double g, double b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public double getRed() {
        return r;
    }

    public double getGreen() {
        return g;
    }

    public double getBlue() {
        return b;
    }

    public double[] getCoords() {
        double[] cs = {r, g, b};
        return cs;
    }

    public double distance(Color c) {
        return Math.sqrt((r - c.r) * (r - c.r) + (g - c.g) * (g - c.g)
                + (b - c.b) * (b - c.b));
    }

    @Override
    public String toString() {
        return "[" + r + ", " + g + ", " + b + "]";
    }
}
