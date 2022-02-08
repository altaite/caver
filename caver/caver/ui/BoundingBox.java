package caver.ui;

import caver.Printer;
import geometry.platonic.PlatonicSolid;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.util.Arrays;
import java.util.List;

/*
 * Class for computation of optimal bounding box of a molecular structure,
 * typicaly a small molecule transported through a tunnel. Can help to think
 * about a radius of a tunnel if the structure of transported molecule is known.
 */
public class BoundingBox {

    private double n_;
    int optimizeCoordinate = 0;

    public BoundingBox(int n) {
        n_ = n;
    }

    public void find(List<Sphere> spheres) {

        double[] bestBox = new double[3];
        Arrays.fill(bestBox, Double.MAX_VALUE);

        for (int x = 0; x < n_; x++) {
            for (int y = 0; y < n_; y++) {
                //for (int z = 0; z < n; z++) {

                double[] lo = new double[3];
                double[] hi = new double[3];
                Arrays.fill(hi, Double.NEGATIVE_INFINITY);
                Arrays.fill(lo, Double.MAX_VALUE);

                Sphere[] ss = new Sphere[spheres.size()];
                for (int i = 0; i < spheres.size(); i++) {
                    Point p = PlatonicSolid.rotate(spheres.get(i).getS(), 0,
                            (double) x / n_ * Math.PI * 2);
                    p = PlatonicSolid.rotate(p, 1,
                            (double) y / n_ * Math.PI * 2);
                    ss[i] = new Sphere(p, spheres.get(i).getR());
                }


                for (Sphere s : ss) {
                    Point p = s.getS();
                    double r = s.getR();
                    for (int i = 0; i < 3; i++) {
                        double q = p.getCoordinates()[i];
                        if (q - r < lo[i]) {
                            lo[i] = q - r;
                        }
                        if (hi[i] < q + r) {
                            hi[i] = q + r;
                        }
                    }

                }

                double[] ds = new double[3];
                for (int i = 0; i < 3; i++) {
                    ds[i] = hi[i] - lo[i];
                }
                Arrays.sort(ds);
                double middle = ds[optimizeCoordinate];
                if (middle < bestBox[0]) {
                    bestBox = ds;
                }
            }
        }

        Printer.println("Best bounding box:");
        for (int i = 0; i < 3; i++) {
            Printer.println(bestBox[i]);
        }
        Printer.println("Radius " + (bestBox[optimizeCoordinate] / 2));

    }
}
