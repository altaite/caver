package algorithms.clustering.statistics;

import chemistry.Atom;
import chemistry.ResidueId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Class for holding records about atoms one residue, which are touching one
 * (dynamic) tunnel.
 *
 */
public class ClusterAtomContacts {

    private Map<Atom, Integer> touches =
            new HashMap<Atom, Integer>();
    // mapping snapshot -> atoms lining the tunnels of the cluster
    private ResidueId ri;
    private String residueName;
    private int sideChainCount;
    private int backboneCount;
    private int totalCount;


    /*
     * By calling, the it is registered that the atom is in contact distance of
     * a tunnel of the cluster. Must be called only once for each snapshot and
     * cluster pair.
     */
    public final void touch(Atom atom) {
        if (ri == null) {
            ri = atom.getResidueId();
        } else {
            if (!ri.equals(atom.getResidueId())) {
                Logger.getLogger("caver").log(Level.WARNING,
                        "Warning: lists of residues "
                        + "lining tunnels might be incorect. touch_1");
            }
        }

        if (residueName == null) {
            residueName = atom.getResidue().getName();
        } else {
            if (!residueName.equals(atom.getResidue().getName())) {
                Logger.getLogger("caver").log(Level.WARNING,
                        "Warning: lists of residues "
                        + "lining tunnels might be incorect. touch_2");
            }
        }

        if (!touches.containsKey(atom)) {
            touches.put(atom, 1);
        } else {
            touches.put(atom, touches.get(atom) + 1);
        }

    }


    /*
     * Similar to touch(). Must be called only once for each snapshot
     */
    public final void touchBackbone() {
        backboneCount++;
    }

    /*
     * Similar to touch(). Must be called only once for each snapshot
     */
    public final void touchSideChain() {
        sideChainCount++;
    }

    public final void touchTotal() {
        totalCount++;
    }

    public String getResidueName() {
        return residueName;
    }

    public ResidueId getResidueId() {
        return ri;
    }
    /*
     * In how many snaphosts some atom of this residue lines the tunnel?
     */

    public int getCount() {
        return touches.keySet().size();
    }

    public int getSideChainCount() {
        return sideChainCount;
    }

    /*
     * In how many snaphosts some side chain atom of this residue lines the
     * tunnel?
     */
    public int getTotalCount() {
        return totalCount;
    }

    public int getBackboneCount() {
        return backboneCount;
    }

    private class CountComparator implements Comparator<AtomCounter> {

        public int compare(AtomCounter a, AtomCounter b) {
            int r = -1 * new Integer(a.getCount()).compareTo(b.getCount());
            if (0 == r) {
                r = new Integer(
                        a.getAtom().getSerialNumber()).compareTo(
                        b.getAtom().getSerialNumber());
            }
            return r;
        }
    }

    public String getAtomCounts() {
        Map<Atom, AtomCounter> atomCounts = new HashMap<Atom, AtomCounter>();

        for (Atom a : touches.keySet()) {
            atomCounts.put(a, new AtomCounter(a, touches.get(a)));
        }
        SortedSet<AtomCounter> sorted = new TreeSet<AtomCounter>(new CountComparator());
        sorted.addAll(atomCounts.values());
        StringBuilder sb = new StringBuilder();
        for (AtomCounter ac : sorted) {
            sb.append(ac.getCount());
            sb.append(":");
            sb.append(ac.getAtom().getName());
            sb.append("_");
            sb.append(ac.getAtom().getSerialNumber());
            sb.append(" ");
        }
        return sb.toString();
    }
}
