package caver.util;

import java.util.logging.Logger;

/*
 * A modifiable integer for counting number of events.
 */
public class CaverCounter {

    private int value;

    public CaverCounter(int first) {
        value = first;
    }

    private void increase() {
        if (Integer.MAX_VALUE <= value) {
            Logger.getLogger("caver").warning("Counter overflow.");
        } else {
            value++;
        }
    }

    public int get() {
        int v = value;
        increase();
        return v;
    }
}
