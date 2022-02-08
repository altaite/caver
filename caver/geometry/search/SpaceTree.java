package geometry.search;

import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;
import geometry.primitives.AnnotatedPoint;
import geometry.primitives.Point;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpaceTree<T> {
    
    KDTree<AnnotatedPoint<T>> kdTree_ = new KDTree<AnnotatedPoint<T>>(3);
    
    public void SpaceTree() {
    }
    
    public void add(Point p, T t) {
        
        AnnotatedPoint<T> ap = new AnnotatedPoint<T>(p, t);
        
        double[] coords = p.getCoordinates();
        try {
            kdTree_.insert(coords, ap);
        } catch (KeySizeException e) {
            throw new RuntimeException(e);
        } catch (KeyDuplicateException e) {
            Logger.getLogger("caver").log(Level.WARNING, "", e);
        }
    }
    
    public AnnotatedPoint<T> close(Point p, double distance) {
        try {
            if (!isEmpty()) {
                AnnotatedPoint ap = kdTree_.nearest(p.getCoordinates());
                if (ap.getPoint().distance(p) <= distance) {
                    return ap;
                }
            }
        } catch (KeySizeException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    
    private boolean isEmpty() {
        return kdTree_.size() <= 0;
    }
}
