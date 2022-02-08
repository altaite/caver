package algorithms.triangulation.qhull;

public class QuickHull4D {

    public static long stage1;
    public static long stage2;
    public static long stage3;
    public static long stage4;
    public static long stage5;
    public static long stage6;

    private void locateMaxAndMin(Vertex[] vertices) {
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].coordinates[0] > vertices[0].coordinates[0]) {
                Vertex temp = vertices[0];
                vertices[0] = vertices[i];
                vertices[i] = temp;
            }
            if (vertices[i].coordinates[0] < vertices[1].coordinates[0]) {
                Vertex temp = vertices[1];
                vertices[1] = vertices[i];
                vertices[i] = temp;
            }
            if (vertices[i].coordinates[1] > vertices[2].coordinates[1]) {
                Vertex temp = vertices[2];
                vertices[2] = vertices[i];
                vertices[i] = temp;
            }
            if (vertices[i].coordinates[1] < vertices[3].coordinates[1]) {
                Vertex temp = vertices[3];
                vertices[3] = vertices[i];
                vertices[i] = temp;
            }
        }
    }

    private void visit(Facet face, OptimizedVector discardedVertices, 
            OptimizedVector horizon, Vertex vertex, int sequence) {
        discardedVertices.append(face.associatedVertices);

        for (int i = 0; i < 4; i++) {
            Facet neighbor = face.neighbor[i];
            if (!neighbor.discarded) {
                if (neighbor.lastVisit < sequence) {
                    neighbor.lastVisit = sequence;
                    if (neighbor.outside4D(vertex)) {
                        neighbor.discarded = true;
                        visit(neighbor, discardedVertices, horizon, 
                                vertex, sequence);
                    } else {
                        Edge edge = new Edge(face.corner[i], 
                                face.corner[((i + 1) % 4)], 
                                face.corner[((i + 2) % 4)], neighbor);

                        horizon.addElement(edge);
                    }
                } else {
                    Edge edge = new Edge(face.corner[i],
                            face.corner[((i + 1) % 4)], 
                            face.corner[((i + 2) % 4)], neighbor);

                    horizon.addElement(edge);
                }
            }
        }
    }

    public OptimizedVector build4D(Vertex[] vertices) {
        OptimizedVector faces = new OptimizedVector(1000);

        locateMaxAndMin(vertices);
        Facet face1;
        faces.addElement(face1 = new Facet(vertices[0], vertices[1],
                vertices[2], vertices[3]));
        Facet face2;
        faces.addElement(face2 = new Facet(vertices[0], vertices[2],
                vertices[1], vertices[3]));

        Vertex middle = new Vertex((vertices[0].coordinates[0] +
                vertices[1].coordinates[0] + vertices[2].coordinates[0] 
                + vertices[3].coordinates[0]) / 4.0D, 
                (vertices[0].coordinates[1] + vertices[1].coordinates[1] + 
                vertices[2].coordinates[1] + vertices[3].coordinates[1]) 
                / 4.0D, (vertices[0].coordinates[2] +
                vertices[1].coordinates[2] + vertices[2].coordinates[2] + 
                vertices[3].coordinates[2]) / 4.0D, 
                (vertices[0].coordinates[3] + vertices[1].coordinates[3] +
                vertices[2].coordinates[3] + vertices[3].coordinates[3]) /
                4.0D);

        for (int i = 0; i < 4; i++) {
            face1.neighbor[i] = face2;
            face2.neighbor[i] = face1;
        }

        for (int i = 4; i < vertices.length; i++) {
            if (!face1.add4D(vertices[i])) {
                face2.add4D(vertices[i]);
            }
        }

        OptimizedVector discardedVertices = new OptimizedVector(100);

        OptimizedVector horizon = new OptimizedVector(20);

        for (int i = 0; i < faces.elementCount; i++) {
            int oldFaces = faces.elementCount;
            discardedVertices.elementCount = 0;
            Facet selected = (Facet) faces.elementAt(i);

            if ((selected.discarded) || 
                    (selected.associatedVertices.elementCount == 0)) {
                continue;
            }
            Vertex vertex = selected.extreme4D();

            horizon.elementCount = 0;
            selected.discarded = true;
            selected.lastVisit = (++Facet.seq);
            visit(selected, discardedVertices, horizon, vertex, Facet.seq);
            for (int iii = 0; iii < horizon.elementCount; iii++) {
                Edge e = (Edge) horizon.elementData[iii];
                Facet newFacet = new Facet(e.start, e.middle, e.end, vertex, middle);

                newFacet.neighbor[0] = e.outside;
                e.inside = newFacet;
                for (int co = 0; co < 4; co++) {
                    if (((e.outside.corner[co] != e.start) ||
                            (((e.outside.corner[((co + 1) % 4)] != e.end) 
                            || (e.outside.corner[((co + 2) % 4)] != e.middle))
                            && ((e.outside.corner[((co + 1) % 4)] != e.middle)
                            || (e.outside.corner[((co + 2) % 4)] != e.end))))
                            && ((e.outside.corner[co] != e.middle) || 
                            (((e.outside.corner[((co + 1) % 4)] != e.start) 
                            || (e.outside.corner[((co + 2) % 4)] != e.end))
                            && ((e.outside.corner[((co + 1) % 4)] != e.end) 
                            || (e.outside.corner[((co + 2) % 4)] != e.start))))
                            && ((e.outside.corner[co] != e.end) || 
                            (((e.outside.corner[((co + 1) % 4)] != e.middle) 
                            || (e.outside.corner[((co + 2) % 4)] != e.start)
                            ) && ((e.outside.corner[((co + 1) % 4)] != e.start)
                            || (e.outside.corner[((co + 2) % 4)]
                            != e.middle))))) {
                        continue;
                    }

                    e.outside.neighbor[co] = newFacet;
                    break;
                }

                faces.addElement(newFacet);
            }

            for (int iii = oldFaces; iii < faces.elementCount; iii++) {
                Facet current = (Facet) faces.elementData[iii];

                for (int f = 1; f < 4; f++) {
                    for (int y = iii + 1; (y < faces.elementCount)
                            && (current.neighbor[f] == null); y++) {
                        Facet testFace = (Facet) faces.elementData[y];
                        for (int u = 1; u < 4; u++) {
                            if (((testFace.corner[u] != current.corner[f]) ||
                                    (((testFace.corner[((u + 1) % 4)] != current.corner[((f + 2) % 4)]) || (testFace.corner[((u + 2) % 4)] != current.corner[((f + 1) % 4)])) && ((testFace.corner[((u + 1) % 4)] != current.corner[((f + 1) % 4)]) || (testFace.corner[((u + 2) % 4)] != current.corner[((f + 2) % 4)])))) && ((testFace.corner[u] != current.corner[((f + 1) % 4)]) || (((testFace.corner[((u + 1) % 4)] != current.corner[f]) || (testFace.corner[((u + 2) % 4)] != current.corner[((f + 2) % 4)])) && ((testFace.corner[((u + 1) % 4)] != current.corner[((f + 2) % 4)]) || (testFace.corner[((u + 2) % 4)] != current.corner[f])))) && ((testFace.corner[u] != current.corner[((f + 2) % 4)]) || (((testFace.corner[((u + 1) % 4)] != current.corner[((f + 1) % 4)]) || (testFace.corner[((u + 2) % 4)] != current.corner[f])) && ((testFace.corner[((u + 1) % 4)] != current.corner[f]) || (testFace.corner[((u + 2) % 4)] != current.corner[((f + 1) % 4)]))))) {
                                continue;
                            }

                            current.neighbor[f] = testFace;
                            testFace.neighbor[u] = current;

                            break;
                        }

                    }

                }

            }

            for (int currentPoint = 0; currentPoint < discardedVertices.elementCount; currentPoint++) {
                Vertex tmpVertex = (Vertex) discardedVertices.elementData[currentPoint];
                if (tmpVertex == vertex) {
                    continue;
                }
                tmpVertex.isAssigned = false;
                for (int fc = oldFaces - 1; fc < faces.elementCount; fc++) {
                    Facet tmpFace = (Facet) faces.elementData[fc];
                    if ((!tmpFace.discarded)
                            && ((tmpVertex.isAssigned = tmpFace.add4D(tmpVertex)))) {
                        break;
                    }
                }
            }
        }
        return faces;
    }
}
