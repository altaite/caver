package algorithms.triangulation;

import algorithms.triangulation.qhull.Facet;
import algorithms.triangulation.qhull.OptimizedVector;
import algorithms.triangulation.qhull.QuickHull4D;
import algorithms.triangulation.qhull.Vertex;
import caver.CalculationSettings;
import caver.Clock;
import caver.Printer;
import geometry.primitives.NumberedSphere;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Calculates Delaunay triangulation of a set of 3D points.
 */
public class SphereSpaceTriangulator {

    private static int NULL = -1;
    private CalculationSettings cs_;

    public SphereSpaceTriangulator(CalculationSettings cs) {
        cs_ = cs;
    }

    public VoronoiDiagram triangulate(SortedMap<Integer, NumberedSphere> spheres) {

        Printer.println("Going to triangulate " + spheres.size() + " points.");

        Clock.start("voronoi diagram construction: procedure");
        Clock.start("voronoi diagram construction: 1. pre");

        List<Tetrahedron> tetrahedrons_ = new ArrayList<Tetrahedron>();

        Vertex[] vertices = new Vertex[spheres.size()];

        Iterator<NumberedSphere> it = spheres.values().iterator();
        for (int i = 0; i < spheres.size(); i++) {
            NumberedSphere s = it.next();
            vertices[i] = new Vertex(s.getS().getX(), s.getS().getY(),
                    s.getS().getZ());
            vertices[i].addDimension();
            vertices[i].index = s.getId();
        }

        Set<Integer> numbers = new HashSet<Integer>();
        for (Vertex v : vertices) {
            assert !numbers.contains(v.index) : v.index;
            numbers.add(v.index);
        }
        Clock.stop("voronoi diagram construction: 1. pre");
        Clock.start("voronoi diagram construction: 2. tri");

        QuickHull4D chull4 = new QuickHull4D();
        OptimizedVector faces = chull4.build4D(vertices);
        
        chull4 = null;
        int j = 0;
        Clock.stop("voronoi diagram construction: 2. tri");
        Clock.start("voronoi diagram construction: 3. tetra");

        Logger.getLogger("caver").log(Level.FINE, "Tetrahedrons: {0}", faces.elementCount);

        for (int i = 0; i < faces.elementCount; i++) {
            Facet f = (Facet) faces.elementAt(i);
            if (f.isDiscarded() || f.normalW > 0.0f) {
                f.index = NULL;
                continue;
            }
            f.index = j;

            NumberedSphere[] tetraSpheres = new NumberedSphere[4];
            for (int k = 0; k < 4; k++) {
                tetraSpheres[k] = spheres.get(f.corner[k].index);
            }

            Tetrahedron t = new Tetrahedron(j, tetraSpheres);
            tetrahedrons_.add(t);
            j++;
        }

        spheres = null;
        int count = j;
        Clock.stop("voronoi diagram construction: 3. tetra");
        Clock.start("voronoi diagram construction: 4. vd cr");
        j = 0;
        Printer.println("Creating Voronoi diagram with " + count + " vertices.");

        VoronoiDiagram vd = VoronoiDiagram.create(count);

        Sphere[] greatest = new Sphere[count];
        Clock.stop("voronoi diagram construction: 4. vd cr");
        Clock.start("voronoi diagram construction: 4b. greatest");
        for (int i = 0; i < faces.elementCount; i++) {
            Facet f = (Facet) faces.elementAt(i);
            if (NULL == f.index) {
                continue;
            }

            Tetrahedron t = tetrahedrons_.get(j);
            greatest[j] = t.getGreatestSphere();
            vd.setPoint(f.index, greatest[j]);
            j++;
        }
        Clock.stop("voronoi diagram construction: 4b. greatest");

        Clock.start("voronoi diagram construction: 5. gates");

        j = 0;

        for (int i = 0; i < faces.elementCount; i++) {

            Facet f1 = (Facet) faces.elementAt(i);

            if (NULL == f1.index) {
                continue;
            }

            Tetrahedron t1 = tetrahedrons_.get(j);
            for (int k = 0; k < 4; k++) {

                Facet f2 = f1.neighbor[k];

                if (f2.normalW < 0.0f && f1.index < f2.index) { // geometry && undirected edges
                    Tetrahedron t2 = tetrahedrons_.get(f2.index);

                    Sphere s1 = greatest[f1.index]; // widest point A
                    Sphere s2 = greatest[f2.index]; // widest point B

                    // defines width in every point of line AB
                    NumberedSphere[] intersection = t2.intersection(t1);

                    if (cs_.isAdmin() && intersection.length > 3) {

                        String ids = "";
                        for (NumberedSphere s :t1.vertices_) {
                            ids += s.getId() +" ";
                        }
                        for (NumberedSphere s :t2.vertices_) {
                            ids+= s.getId() + " ";
                        }
                        Printer.warn("Suspicious tetrahedron intersection: " +
                                t1.getId() +  " "+ t2.getId() + " " + ids);

                    }

                    NumberedSphere limiting = chooseLimitingSphere(intersection);

                    Point p1 = s1.getS();
                    Point p2 = s2.getS();

                    int n1 = f1.index;
                    int n2 = f2.index;

                    VE segment;
                    if (cs_.getProbeRadius() <= s1.getR() 
                            && cs_.getProbeRadius() <= s2.getR()) {

                        segment = new VE(n1, n2, p1, p2,
                                limiting.getSphere(), cs_.getPassingFunction());

                        if (segment.getBottleneck().getR() < cs_.getProbeRadius()) {
                            segment = VE.getBlockedEdge();
                        }
                    } else {
                        segment = VE.getBlockedEdge();

                    }
                    vd.connect(n1, n2, segment);
                    vd.connect(n2, n1, segment);

                }
            }
            j++;
        }

        Clock.stop("voronoi diagram construction: 5. gates");

        Clock.start("voronoi diagram construction: 6. test");
        
        Clock.stop("voronoi diagram construction: 6. test");
        Clock.stop("voronoi diagram construction: procedure");
        return vd;
    }

    public Set<NumberedSphere> as(NumberedSphere[] a) {
        Set<NumberedSphere> set = new HashSet<NumberedSphere>();
        set.addAll(Arrays.asList(a));
        return set;
    }

    private NumberedSphere chooseLimitingSphere(NumberedSphere[] spheres) {
        NumberedSphere best = null;
        for (NumberedSphere s : spheres) {

            if (null == best) {
                best = s;
            } else if (best.getR() == s.getR()) {
                if (s.getId() < best.getId()) {
                    best = s;
                }
            } else if (best.getR() < s.getR()) {
                best = s;
            }
        }
        return best;
    }
}
