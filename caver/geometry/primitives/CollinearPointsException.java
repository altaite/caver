package geometry.primitives;

/*
 * Thrown when three or more points are found to be collinear in algorithm which
 * does not support such degenerate case.
 */
public class CollinearPointsException extends Exception {

    public CollinearPointsException() {
        super();
    }
}
