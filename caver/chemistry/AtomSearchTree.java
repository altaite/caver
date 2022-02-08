package chemistry;

import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;
import geometry.primitives.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for efficient identification of atoms within a specified distance from
 * a point.
 *
 */
public class AtomSearchTree {

    private KDTree<Atom> kdTree_;
    private double maxR_ = 1.2; // just start, is updated in constructor
    private String warning = "Warning: atom and residue contacts"
            + " might be wrong.";

    public AtomSearchTree(Collection<Atom> atoms) {
        int n = atoms.size();
        kdTree_ = new KDTree<Atom>(3);
        for (Atom a : atoms) {
            double[] coords = a.getCenter().getCoordinates();
            if (maxR_ < a.getRadius()) {
                maxR_ = a.getRadius();
            }
            try {
                kdTree_.insert(coords, a);
            } catch (KeySizeException e) {
                Logger.getLogger("caver").log(Level.SEVERE, warning, e);
            } catch (KeyDuplicateException e) {
                Logger.getLogger("caver").log(Level.SEVERE, warning, e);
            }
        }
    }

    public Set<Atom> getAtomsWithinDistanceFromPoint(Point p, double distance) {
        Set<Atom> collisions = new HashSet<Atom>();
        List<Atom> atoms;

        try {
            atoms = kdTree_.nearestEuclidean(
                    p.getCoordinates(), distance + maxR_);

            for (Atom a : atoms) {
                Point ac = a.getCenter();
                double d = a.getRadius() + distance;
                if (ac.squaredDistance(p) <= d * d) {
                    collisions.add(a);
                }
            }

        } catch (KeySizeException e) {
            Logger.getLogger("caver").log(Level.SEVERE, warning, e);
        }

        return collisions;
    }
}
