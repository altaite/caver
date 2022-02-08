package caver.ui;

/*
 * Thrown in case of illegal calculation parameters.
 */
public class SettingsException extends Exception {

    public SettingsException(String msg) {
        super(msg);
    }

    public SettingsException() {
        super();
    }
}
