package algorithms.triangulation.qhull;

public class Edge {

    Vertex start;
    Vertex middle;
    Vertex end;
    Facet inside;
    Facet outside;

    public Edge(Vertex start, Vertex end, Facet outside) {
        this.start = start;
        this.end = end;
        this.outside = outside;
    }

    public Edge(Vertex start, Vertex middle, Vertex end, Facet outside) {
        this.start = start;
        this.middle = middle;
        this.end = end;
        this.outside = outside;
    }
}