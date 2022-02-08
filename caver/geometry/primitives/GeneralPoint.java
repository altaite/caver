package geometry.primitives;

import java.io.Serializable;

/*
 * Represents a point in 3D space. Some methods works with the point as it was a
 * vector.
 *
 * Immutable.
 */
public interface GeneralPoint extends Serializable {

    public double squaredSize();

    public double size();

    public double distance(Point p);

    public double squaredDistance(Point p);

    /*
     * Dot product of a vector. Skalarni soucin.
     */
    public double dot(Point p);

    public double getX();

    public double getY();

    public double getZ();

    public double[] getCoordinates();
}
