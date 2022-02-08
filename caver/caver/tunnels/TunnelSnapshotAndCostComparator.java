package caver.tunnels;

import java.util.Comparator;

public class TunnelSnapshotAndCostComparator implements Comparator<Tunnel> {

    @Override
    public int compare(Tunnel a, Tunnel b) {
        int c = a.getSnapId().compareTo(b.getSnapId());
        if (c == 0) {
            c = Double.compare(a.getCost(), b.getCost());
        }
        return c;
    }
}
