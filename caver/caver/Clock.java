/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package caver;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * For measuring how much time computer spents over a defined lines of code.
 * Times for repeated calling of lines are summed up.
 */
public class Clock {

    private static SortedMap<String, Long> starts = new TreeMap<String, Long>();
    private static SortedMap<String, Long> stops = new TreeMap<String, Long>();
    private static SortedMap<String, Long> sums = new TreeMap<String, Long>();

    public static void start(String s) {
        starts.put(s, System.nanoTime());
    }

    public static void stop(String s) {
        long stop = System.nanoTime();
        stops.put(s, stop);

        long dt = stop - starts.get(s);
        if (!sums.containsKey(s)) {
            sums.put(s, dt);
        } else {
            sums.put(s, sums.get(s) + dt);
        }
    }

    public static void print(BufferedWriter bw) {
        try {
            long max = Long.MIN_VALUE;
            for (String key : starts.keySet()) {
                if (stops.containsKey(key)) {
                    long dt = stops.get(key) - starts.get(key);
                    if (max < dt) {
                        max = dt;
                    }
                }
            }

            bw.write("== Times in milliseconds (fraction) ==\n");
            for (String key : sums.keySet()) {
                try {
                    long dt = sums.get(key);
                    bw.write(key + ": " + nanoToMili(dt)
                            + " (" + ((double) (100 * dt / max) / 100) + ")\n");
                } catch (NullPointerException e) {
                    bw.write("Unclosed timer string " + key);
                    bw.write(stops.containsKey(key) + "\n");
                    bw.write(starts.get(key) + "\n");
                    bw.write(stops.get(key) + "\n");
                }
            }
        } catch (IOException e) {
            Logger.getLogger("caver").log(Level.WARNING, "Clock error.", e);
        }
    }

    public static String nanoToMili(long nano) {
        return (nano / 1000000) + "." + (nano % 1000000);
    }
}
