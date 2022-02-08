package caver;

/*
 * Manages parameters related to tunnel distance computation as implemented in
 * package algorithms.clustering.layers.
 */
public class LayersSettings {

    private double excludeStartZone_ = 2;
    private double excludeEndZone_ = 0;
    private double minMiddleZone_ = 5;
    private double weightingCoefficient_ = 1;

    private LayersSettings() {
    }

    public LayersSettings(double start, double end, double middle,
            double weighting) {
        excludeStartZone_ = start;
        excludeEndZone_ = end;
        minMiddleZone_ = middle;
        weightingCoefficient_ = weighting;
    }

    public double getExcludeStartZone() {
        return excludeStartZone_;
    }

    public double getExcludeEndZone() {
        return excludeEndZone_;
    }

    public double getMinMiddleZone() {
        return minMiddleZone_;
    }

    public double getWeightingCoefficient() {
        return weightingCoefficient_;
    }

    public void setExcludeStartZone(double excludeStartZone_) {
        this.excludeStartZone_ = excludeStartZone_;
    }

    public void setExcludeEndZone(double excludeEndZone_) {
        this.excludeEndZone_ = excludeEndZone_;
    }

    public void setMinMiddleZone(double minMiddleZone_) {
        this.minMiddleZone_ = minMiddleZone_;
    }

    public void setWeightingCoefficient(double weightingCoefficient_) {
        this.weightingCoefficient_ = weightingCoefficient_;
    }
}
