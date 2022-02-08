package caver.tunnels;

import java.util.Comparator;

public class TunnelCostComparator implements Comparator<Tunnel> {

    @Override
    public int compare(Tunnel t1, Tunnel t2) {
        return Double.compare(t1.getCost(), t2.getCost());
    }
}
