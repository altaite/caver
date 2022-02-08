package caver.ui;

import algorithms.clustering.Cluster;
import algorithms.clustering.Clusters;
import caver.CalculationSettings;
import caver.Printer;
import caver.tunnels.Tunnel;
import caver.tunnels.Tunnels;
import caver.ui.animation.PdbTunnelAnimator;
import chemistry.pdb.PdbLine;
import chemistry.pdb.SnapId;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;

/**
 *
 * Class for producing saving results - files for visualization and tables.
 *
 */
public class Visualization {

    private int maxClusters = 999;

    public Visualization() {
    }

    public void saveTunnels() {
    }

    public void visualizeTunnels(List<Tunnel> tunnels, double sampling, int max,
            File file) {
        try {

            List<Tunnel> subsample = new ArrayList<Tunnel>();
            Random random = new Random();
            subsample.addAll(tunnels);
            while (max < subsample.size()) {
                subsample.remove(random.nextInt(subsample.size()));
            }

            Printer.println("Visualizing " + subsample.size() + " unprocessed "
                    + "tunnels out of " + tunnels.size(), Printer.IMPORTANT);

            int serial = 1;
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            for (Tunnel t : subsample) {
                List<Sphere> spheres = t.computeProfile(sampling);
                boolean first = true;
                for (Sphere s : spheres) {
                    PdbLine pdbLine = new PdbLine(
                            serial, "H", "TUN", "RES",
                            'T',
                            s.getS().getX(),
                            s.getS().getY(),
                            s.getS().getZ());
                    pdbLine.setTemperatureFactor(s.getR());
                    ps.println(pdbLine.getPdbString());
                    if (!first) {
                        ps.println(String.format("CONECT%5d%5d", serial - 1, serial));
                    } else {
                        first = false;
                    }
                    serial++;
                }
            }
            ps.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clustersToPdbDynamical(Clusters clusters, CalculationSettings cs_,
            SortedMap<SnapId, Point> frameToStart)
            throws IOException {

        for (Cluster cluster : clusters.computePriorities()) {
            if (maxClusters < cluster.getPriority()) {
                continue;
            }

            PdbTunnelAnimator a = new PdbTunnelAnimator(frameToStart);

            List<Tunnel> tunnels = cluster.getTunnels();

            for (int i = 0; i < tunnels.size(); i++) {
                Tunnel t = tunnels.get(i);

                String resi;
                if (maxClusters < t.getPriority()) {
                    resi = String.valueOf(maxClusters);
                } else {
                    resi = String.valueOf(t.getPriority());
                }

                a.addSphereChain(t.getSnapId(), //snap
                        t.computeProfile(
                        cs_.getVisualizationTunnelSamplingStep()), //spheres
                        'T', //chain
                        resi,
                        "" // segi
                        );
            }
            a.saveDynamicalVisualization(cs_, cluster.getPriority());
        }
    }

    public void clustersToPdbTimeless(Clusters clusters, CalculationSettings cs_,
            SortedMap<SnapId, Point> frameToStart)
            throws IOException {

        for (Cluster cluster : clusters.computePriorities()) {
            if (maxClusters < cluster.getPriority()) {
                continue;
            }

            PdbTunnelAnimator a = new PdbTunnelAnimator(frameToStart);

            List<Tunnel> tunnels = cluster.getTunnels();

            for (int i = 0; i < tunnels.size(); i++) {
                Tunnel t = tunnels.get(i);

                String resi;
                if (maxClusters < t.getPriority()) {
                    resi = String.valueOf(maxClusters);
                } else {
                    resi = String.valueOf(t.getPriority());
                }

                a.addSphereChain(t.getSnapId(), //snap
                        t.computeProfile(
                        cs_.getVisualizationTunnelSamplingStep()), //spheres
                        'T', //chain
                        resi,
                        "" // segi
                        );
            }
            String clusterFileId = CalculationSettings.fill(cluster.getPriority(), 3);
            a.save(cs_, clusterFileId);
        }
    }

    public void originsToPdb(SortedMap<SnapId, Tunnels> tunnels, boolean voronoi,
            File file)
            throws IOException {
        PrintStream ps = new PrintStream(new FileOutputStream(file));
        int serial = 1;
        for (SnapId snap : tunnels.keySet()) {
            ps.println("MODEL        " + snap.getNumber());
            Point origin;
            if (voronoi) {
                origin = tunnels.get(snap).getVoronoiOrigin();
            } else {
                origin = tunnels.get(snap).getOrigin();
            }
            PdbLine pdbLine = new PdbLine(
                    serial, "H", "FIL", "1",
                    'S',
                    origin.getX(),
                    origin.getY(),
                    origin.getZ());
            ps.println(pdbLine.getPdbString());
            serial++;
            ps.println("ENDMDL");

        }
    }
}
