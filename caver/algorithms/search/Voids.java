package algorithms.search;

import algorithms.triangulation.VoronoiDiagram;
import caver.CalculationSettings;
import chemistry.pdb.PdbLine;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 *
 * Collection of all voids in the macromolecule.
 */
public class Voids {

    VoronoiDiagram vd_;
    boolean[] visited_;
    List<Void> voids;

    public Voids(VoronoiDiagram vd) {
        vd_ = vd;
    }

    public void run() {
        {
            Set<Integer> outer = vd_.getOuterNodes();

            LinkedList<Integer> fifo = new LinkedList<Integer>(outer);

            visited_ = new boolean[vd_.size()];

            while (!fifo.isEmpty()) {
                int node = fifo.poll();
                visited_[node] = true;

                for (int i = 0; i < 4; i++) {
                    int neighbor = vd_.getNeighbour(node, i);
                    if (!vd_.valid(neighbor)) {
                        continue;
                    }

                    double rNode = vd_.getSphere(node).getR();
                    double rNeighbor = vd_.getSphere(neighbor).getR();


                    if (!visited_[neighbor]
                            && (rNeighbor < rNode || 3 <= rNeighbor)) {// TODO outer shell
                        fifo.push(neighbor);

                    }
                }
            }
        }

        voids = new ArrayList<Void>();

        for (int u = 0; u < vd_.size(); u++) {
            if (visited_[u]) {
                continue;
            }
            double r = vd_.getSphere(u).getR();
            if (r < 1.4) {
                continue;
            }

            LinkedList<Integer> fifo = new LinkedList<Integer>();
            Void space = new Void();
            fifo.push(u);
            visited_[u] = true;
            while (!fifo.isEmpty()) {
                int v = fifo.poll();
                space.add(vd_.getSphere(v));

                for (int i = 0; i < 4; i++) {
                    int w = vd_.getNeighbour(v, i);
                    if (!vd_.valid(w)) {
                        continue;
                    }
                    if (!visited_[w] && 1.4 <= vd_.getBottleneckByGate(v, i)) {
                        fifo.push(w);
                        visited_[w] = true;
                    }

                }

            }
            voids.add(space);

        }

        Collections.sort(voids);

    }

    public void save(CalculationSettings cs) {
        int voidId = 1;
        for (Void v : voids) {
            File file = cs.getVoidsFile(voidId++);
            Set<Sphere> spheres = v.getSpheres();
            int serial = 1;
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                for (Sphere s : spheres) {
                    if (0.9 <= s.getR()) {
                        Point p = s.getS();


                        PdbLine pl = new PdbLine(serial, "H", "VOI", "RRR", 'V',
                                p.getX(), p.getY(), p.getZ());
                        pl.setTemperatureFactor(s.getR());
                        bw.write(pl.getPdbString() + "\n");
                        serial++;
                    }
                }
                bw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Point getClosest(Point t, double minR) {
        return voids.get(0).getClosest(t, minR);
    }

    public void savePoints(File file) {
        int serial = 1;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (Void v : voids) {
                if (1.4 <= v.getR()) {
                    Point p = v.getP();

                    PdbLine pl = new PdbLine(serial, "H", "VOI", "RRR", 'V',
                            p.getX(), p.getY(), p.getZ());
                    pl.setTemperatureFactor(v.getR());
                    bw.write(pl.getPdbString() + "\n");
                    serial++;
                }
            }
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
