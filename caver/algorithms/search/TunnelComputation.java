package algorithms.search;

import algorithms.triangulation.VoronoiDiagram;
import caver.CalculationSettings;
import caver.tunnels.Tunnels;
import caver.ui.CalculationException;
import caver.util.CaverCounter;
import chemistry.pdb.SnapId;
import geometry.primitives.Point;
import java.io.IOException;

public interface TunnelComputation {

    public Tunnels computeTunnels(CalculationSettings cs, VoronoiDiagram vd,
            Point start, double proteinR, SnapId snapId, CaverCounter counter)
            throws IOException, CalculationException;

    public Tunnels computeTunnelsBlock(CalculationSettings cs, VoronoiDiagram vd,
            Point origin, SnapId snapId, CaverCounter counter) throws IOException;
}
