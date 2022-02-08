package geometry.primitives;

import java.io.Serializable;

/*
 * Represents a sphere or a ball.
 */
public interface GeneralSphere extends Serializable {

    public Point getS();

    public double getR();
}
