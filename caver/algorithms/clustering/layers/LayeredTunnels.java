package algorithms.clustering.layers;

import algorithms.clustering.Matrix;
import caver.CalculationSettings;
import caver.LayersSettings;
import caver.tunnels.Tunnel;
import geometry.primitives.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of tunnels that are represented by the same number of points. Only
 * the points with same position are then compared when evaluating pairwise
 * distances of tunnels, resulting in efficient computation and metric space.
 *
 * For more details see the publication CAVER 3.0: A Tool for the Analysis of
 * Transport Pathways in Dynamic Protein Structures
 */
public class LayeredTunnels implements Matrix, Iterable<LayeredTunnel> {

    private Point source_; // starting point common to all tunnels
    private List<LayeredTunnel> layeredTunnels = new ArrayList<LayeredTunnel>();
    private boolean DEBUG = false;
    private int n_;
    private AverageSurface surface_;
    private CalculationSettings cs_;
    private LayersSettings ls_;

    public enum DistanceType {

        N3L, // length + 3 cooridnates per each layer, euclidian + man
        N3 // 3 cooridnates per each layer, euclidian + manhattan
    }

    /*
     * Important: tunnel ordering must be preserved for whole lifetime of this
     * object.
     */
    public LayeredTunnels(Point source, AverageSurface surface,
            Iterable<Tunnel> tunnels,
            boolean showPoints, boolean doAverageSurface,
            LayersSettings ls, CalculationSettings cs) {

        cs_ = cs;
        ls_ = ls;
        source_ = source;
        surface_ = surface;

        if (doAverageSurface && showPoints && cs.saveSurfaceVisualization()) {
            surface_.save(cs.getSurfaceFile(), tunnels, cs);
            surface_.saveDefinition(cs.getSurfaceDefinitionFile());
        }

        n_ = cs.getLayersCount();

        for (Tunnel t : tunnels) {
            LayeredTunnel lt =
                    new LayeredTunnel(source_, surface_, n_, t,
                    doAverageSurface, ls_, cs);
            layeredTunnels.add(lt);

        }
        if (cs_.hadNoLayers()) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "There were problems in finding tunnel representing points.",
                    new Object[]{ls.getExcludeStartZone(),
                        ls.getExcludeEndZone(),
                        ls.getMinMiddleZone()});

        }
        if (DEBUG) {
            checkLayers();
        }
    }

    @Override
    public int size() {
        return this.layeredTunnels.size();
    }

    public int getTnunelIndex(Tunnel t) {
        for (int i = 0; i < layeredTunnels.size(); i++) {
            if (layeredTunnels.get(i).getTunnel().equals(t)) {
                return i;
            }
        }
        throw new RuntimeException();
    }

    public int getLayersCount() {
        return n_;
    }

    private void checkLayers() {
        for (LayeredTunnel lt : layeredTunnels) {
            for (int i = 0; i < lt.size(); i++) {
                if (lt.x[i] == 0) {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "lt.x[i]");
                }
                if (lt.y[i] == 0) {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "lt.y[i]");
                }
                if (lt.z[i] == 0) {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "lt.z[i]");
                }
            }
        }

    }

    @Override
    public Iterator<LayeredTunnel> iterator() {
        return layeredTunnels.iterator();
    }

    public LayeredTunnel createLayerdTunnel(Tunnel t) {
        LayeredTunnel lt =
                new LayeredTunnel(source_, surface_, cs_.getLayersCount(),
                t, cs_.doAverageSurfaceGlobal(), ls_, cs_);
        return lt;
    }

    public float getDistance(int x, LayeredTunnel b) {

        LayeredTunnel a = layeredTunnels.get(x);

        double sum = 0;
        int counter = 0;
        int min = Math.min(a.size(), b.size());

        for (int i = 0; i < min; i++) {
            double xx = a.x[i] - b.x[i];
            xx *= xx;
            double yy = a.y[i] - b.y[i];
            yy *= yy;
            double zz = a.z[i] - b.z[i];
            zz *= zz;
            sum += Math.sqrt(xx + yy + zz);
            counter++;
        }

        double d = sum / counter;
        return (float) d;

    }

    @Override
    public float getDistance(int x, int y) {
        LayeredTunnel a = layeredTunnels.get(x);
        LayeredTunnel b = layeredTunnels.get(y);

        double sum = 0;
        int counter = 0;
        int min = Math.min(a.size(), b.size());

        for (int i = 0; i < min; i++) {
            double xx = a.x[i] - b.x[i];
            xx *= xx;
            double yy = a.y[i] - b.y[i];
            yy *= yy;
            double zz = a.z[i] - b.z[i];
            zz *= zz;
            sum += Math.sqrt(xx + yy + zz);
            counter++;
        }
        double d = sum / counter;
        return (float) d;
    }
}
