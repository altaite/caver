package geometry.primitives;

/*
 * A Point and instance of class T associated with it.
 */
public final class AnnotatedPoint<T> {

    private Point p;
    private T t;

    public AnnotatedPoint(Point p, T t) {
        this.p = p;
        this.t = t;
    }

    public Point getPoint() {
        return p;
    }

    public T getAnnotation() {
        return t;
    }
}
