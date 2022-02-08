package caver.ui.animation;

import caver.CalculationSettings;
import chemistry.pdb.PdbLine;
import chemistry.pdb.SnapId;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Animation of linear structures composed of spheres and lines.
 *
 * There can be different linear structures with different lengths in each
 * snapshot.
 *
 */
public class PdbTunnelAnimator {

    SortedMap<SnapId, List<Chain>> chains_ =
            new TreeMap<SnapId, List<Chain>>();
    // slots for tunnels
    int[] size_;
    int[] first_;
    int[] last_;
    SortedMap<SnapId, Point> frameToStart_;

    public PdbTunnelAnimator(SortedMap<SnapId, Point> frameToStart) {

        frameToStart_ = frameToStart;

        for (SnapId snap : frameToStart.keySet()) {
            chains_.put(snap, new ArrayList<Chain>());
        }

    }

    public void addSphereChain(SnapId snap, List<Sphere> spheres, char chain,
            String resi, String segi) {
        Chain c = new Chain(spheres, chain, segi, resi);

        if (chains_.containsKey(snap)) {
            chains_.get(snap).add(c);

        } else {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Warning: snapshot {0} will not be displayed.", snap);
        }
    }

    // Each chain of spheres in snapshot has its own slot. Slot is a sequence
    // of serial atom numbers reserverd to represent the chain in all snapshots.
    private void assignSerials() {
        // in each snapshot, sort chains by their length
        // max. number of chains in one snapshot
        int n = 0;
        for (SnapId snap : chains_.keySet()) {
            List<Chain> chains = this.chains_.get(snap);
            if (n < chains.size()) {
                n = chains.size();
            }
        }

        size_ = new int[n];
        first_ = new int[n];
        last_ = new int[n];

        //       size1 size2
        // snap1 slot1 slot2 
        // snap2 slot1 slot2         
        // estimate greatest chain in each slot over all snapshots
        // chains are sorted (see above) to achieve memory effectivity
        for (SnapId snap : chains_.keySet()) {
            List<Chain> chains = this.chains_.get(snap);
            for (int i = 0; i < chains.size(); i++) {
                if (size_[i] < chains.get(i).size()) {
                    size_[i] = chains.get(i).size();
                }
            }
        }

        int serial = 1;
        for (int i = 0; i < n; i++) {
            first_[i] = serial;
            last_[i] = serial + size_[i] - 1;
            serial += size_[i];
        }

    }

    /*
     * Saves everything into one snapshots.
     * Less problems, but no animation in PyMOL or VMD
     */
    public void save(CalculationSettings cs, String clusterId)
            throws IOException {

        int part = 1;

        File file = cs.getTimelessClusterFile(clusterId, part++);
        PrintStream ps = new PrintStream(new FileOutputStream(file));
        int serial = 1;

        boolean skipped = false;
        for (SnapId snap : chains_.keySet()) {

            List<Chain> chains = chains_.get(snap);
            for (Chain c : chains) {
                for (int i = 0; i < c.size(); i++) {

                    if (99999 < serial) {
                        ps.close();
                        file = cs.getTimelessClusterFile(clusterId, part++);
                        ps = new PrintStream(new FileOutputStream(file));
                        serial = 1;
                    }

                    Sphere s = c.get(i);
                    PdbLine pdbLine = new PdbLine(
                            serial, "H", "FIL", c.getResi(),
                            c.getChain(),
                            s.getS().getX(),
                            s.getS().getY(),
                            s.getS().getZ());
                    pdbLine.setTemperatureFactor(s.getR());
                    ps.println(pdbLine.getPdbString());
                    if (0 < i) {
                        ps.println(String.format("CONECT%5d%5d", serial - 1, serial));
                    }

                    serial++;

                }
                ps.println();
            }
        }
        ps.close();
        if (skipped) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Skipped some tunnels for a cluster, "
                    + "99999 atoms reached.");
        }
    }

    /*
     * PyMOL, VMD - animation
     *
     * Each snapshot must contain atoms 1..n, script for radius update in
     * VMD exploits this.
     *
     */
    public void saveDynamicalVisualization(CalculationSettings cs,
            int clusterPriority) throws IOException {

        File pdbFile = cs.getClusterPdbFile(clusterPriority);
        File radiiFile = cs.getClusterRadiiFile(clusterPriority);

        assignSerials();

        PrintStream psPdb = new PrintStream(new FileOutputStream(pdbFile));
        PrintStream psRadii = new PrintStream(new FileOutputStream(radiiFile));

        boolean first = true;

        for (SnapId snap : chains_.keySet()) {

            Sphere dummy = new Sphere(frameToStart_.get(snap), 0.5);

            String modelS = "MODEL        " + snap.getNumber();
            psPdb.println(modelS);

            List<Chain> chains = chains_.get(snap);

            
            String dummyRes = "1";

            for (int slot = 0; slot < size_.length; slot++) {

                Chain chain = null;
                if (slot < chains.size()) {
                    chain = chains.get(slot);
                }

                // number of fake spheres at starting point
                int dummies = size_[slot];
                if (null != chain) {
                    // even residues should be consistent over snapshots 
                    // for PyMOL
                    dummies -= chain.size();
                }

                for (int i = 0; i < size_[slot]; i++) {
                    int index = i - dummies;
                    int serial = first_[slot] + i;
                    Sphere sphere;
                    char c = 'T';
                    String residue = dummyRes;
                    if (null != chain) {
                        residue = chain.getResi();
                    }
                    if (null != chain && dummies <= i) {
                        sphere = chain.get(index);
                        c = chain.getChain();
                        residue = chain.getResi();
                    } else {
                        sphere = dummy;
                    }
                    PdbLine pdbLine = new PdbLine(
                            serial, "H", "FIL", residue,
                            c,
                            sphere.getS().getX(),
                            sphere.getS().getY(),
                            sphere.getS().getZ());
                    pdbLine.setTemperatureFactor(sphere.getR());
                    psPdb.println(pdbLine.getPdbString());
                    pdbLine.setR(sphere.getR());

                    double r = (double) Math.round(sphere.getR() * 100) / 100;

                    String rs = ("" + r);
                    if (4 < rs.length()) {
                        rs = rs.substring(0, 4);
                    }
                    while (rs.length() < 4) {
                        rs = rs + "0";
                    }
                    psRadii.print(rs);
                }
                psPdb.println();
            }
            // connect them
            for (int slot = 0; slot < size_.length; slot++) {
                for (int i = 0; i < size_[slot] - 1; i++) {
                    int serial = first_[slot] + i;
                    String connectS = String.format("CONECT%5d%5d", serial, serial + 1);
                    psPdb.println(connectS);
                }
                psPdb.println();
            }
            if (first) {
                first = false;
            }

            psPdb.println("ENDMDL");
            psRadii.println();
        }
        psPdb.close();
    }
}

class ChainComparator implements Comparator<Chain> {

    @Override
    public int compare(Chain a, Chain b) {
        return new Integer(a.size()).compareTo(b.size());
    }
}
