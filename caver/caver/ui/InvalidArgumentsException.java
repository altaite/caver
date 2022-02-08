package caver.ui;

/**
 * Thrown if results in the middle of calculation do not allow to continue.
 * Not for unexpected exception, parameter parsing error etc.
 */

public class InvalidArgumentsException extends Exception {
        public InvalidArgumentsException(String msg) {
            super(msg);
        }
}
