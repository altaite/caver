package algorithms.search;

/*
 * Tunnels are identified as cheapest paths in graph. The interface CostFunction
 * provides methods for evaluation of costs of edges. The cost should reflect
 * the probability that small molecule can pass through the site described by
 * arguments of getCost method, i.e. the higher the cost, the lower the
 * probability.
 *
 */
public interface CostFunction {

    public double getCost(double x, double y, double z, double r, double l);

    /*
     * Cost of passage through tunnel segment of radius r and length l.
     */
    public double getCost(double r, double l);

    public void printGraph();

    public boolean overflow(double radius);
}
