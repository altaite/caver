package upgma;

public class MyTime {

    private long start, stop = 0;

    public MyTime start() {
        start = System.currentTimeMillis();
        return this;
    }

    public void stop() {
        stop = System.currentTimeMillis();
    }

    public long time() {
        return stop - start;
    }

    public String timeSeconds() {
        return String.valueOf((stop - start) / 1000.0f) + " seconds";
    }
}
