package caver.tunnels;

import java.util.Comparator;

public class TunnelCsvComparator implements Comparator<Tunnel> {

    @Override
    public int compare(Tunnel a, Tunnel b) {
        int c = new Integer(a.getCluster().getPriority()).compareTo(
                b.getCluster().getPriority());
        if (0 == c) {
            c = a.getSnapId().compareTo(b.getSnapId());
        }
        if (0 == c) {
            c = new Double(a.getCost()).compareTo(b.getCost());
        }
        if (0 == c) {
            c = new Double(b.getBottleneck().getR()).compareTo(
                    a.getBottleneck().getR());
        }
        if (0 == c) {
            c = new Double(a.getLength()).compareTo(b.getLength());
        }

        if (0 == c) {
            c = new Integer(a.getNumber()).compareTo(b.getNumber());
        }
        return c;
    }
}
