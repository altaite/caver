package caver.ui.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

/*
 * Class for creation of heat maps - plots of values in dependence of two
 * variables.
 */
public class MapDrawer {

    public static final double UNKNOWN = -1;
    private int[] colors_;
    private int unknownColor_;

    public MapDrawer(File palette, File unknown) throws IOException {
        BufferedImage img = ImageIO.read(palette);
        colors_ = new int[img.getWidth()];
        for (int x = 0; x < img.getWidth(); x++) {
            colors_[x] = img.getRGB(x, 0);
        }

        img = ImageIO.read(unknown);

        unknownColor_ = img.getRGB(0, 0);

    }

    public void drawStripGraph(List<Double> values, double bottom, double top,
            int width, Graphics2D g, int x, int y) throws IOException {

        for (int i = 0; i < values.size(); i++) {

            g.setColor(color(values.get(i), bottom, top));
            g.drawRect(x, y + i, width - 1, 1);

        }

    }

    public void drawHeatMap(List<List<Double>> values, double bottom,
            double top, Graphics2D g, int x, int y) throws IOException {

        for (int yi = 0; yi < values.size(); yi++) {
            for (int xi = 0; xi < values.get(yi).size(); xi++) {
                double d = values.get(yi).get(xi);
                Color c = color(d, bottom, top);
                g.setColor(c);
                g.drawRect(x + xi, y + yi, 1, 1);

            }
        }

    }

    public void drawHeatMap(double[][] values, double bottom,
            double top, Graphics2D g, int x, int y, int xZoom, int yZoom) throws IOException {

        for (int yi = 0; yi < values.length; yi++) {
            for (int xi = 0; xi < values[yi].length; xi++) {
                double d = values[yi][xi];
                Color c;
                if (UNKNOWN == d) {
                    c = new Color(unknownColor_);
                } else {
                    c = color(d, bottom, top);
                }

                g.setColor(c);

                g.fillRect(x + xi * xZoom, y + yi * yZoom, xZoom, yZoom);

            }
        }

    }

    private Color color(double value, double bottom, double top) {

        double percent = (value - bottom) / (top - bottom);
        if (percent < 0) {
            percent = 0;
        }
        if (1 < percent) {
            percent = 1;
        }
        int index = (int) Math.round(percent * (colors_.length - 1));
        Color color = new Color(colors_[index]);
        return color;
    }

    public static void main(String[] args) throws IOException {
        File f = new File("vystup.png");
        File pallette = new File("pallette.png");
        File unknown = new File("unknown.png");

        List<Double> values = new ArrayList<Double>();
        values.add(1.0);
        values.add(2.0);
        values.add(1.0);
        values.add(2.0);
        MapDrawer md = new MapDrawer(pallette, unknown);

        Random random = new Random();
        List<List<Double>> heat = new ArrayList<List<Double>>();
        for (int i = 0; i < 100; i++) {
            List<Double> l = new ArrayList<Double>();
            for (int j = 0; j < 500; j++) {
                l.add((double) random.nextInt(100));
            }
            heat.add(l);
        }

        int width = 800;
        int height = 800;
        BufferedImage img = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        md.drawStripGraph(values, 0.0, 3.0, 50, g, 0, 0);
        md.drawHeatMap(heat, 0.0, 100.0, g, 30, 0);

        g.dispose();
        ImageIO.write(img, "png", f);


    }
}
