package caver.ui;

/**
 *
 * Thrown if results in the middle of calculation do not allow to continue.
 * Not for unexpected exception, parameter parsing error etc.
 *
 */

public class CalculationException extends Exception {
        public CalculationException(String msg) {
            super(msg);
        }
}
