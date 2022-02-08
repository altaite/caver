package algorithms.clustering.statistics;

public class StatisticsColumn {

    private String shortHeader;
    private int width;

    public StatisticsColumn(String shortHeader, int width) {
        this.shortHeader = shortHeader;
        this.width = width;
    }

    public String getShortHeader() {
        return shortHeader;
    }

    public int getWidth() {
        return width;
    }
}
