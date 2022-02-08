package upgma;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DiskSupport {

    static int reusableC1;
    static int reusableC2;
    static float reusableValue;

    /*
     * Load edges from .edges format and save them on-the fly to our bin format
     */
    float resaveAllEdges(String inedges, DataOutputStream output,
            List<Cluster> clusters) {
        int saved = 0;
        float maxVal = -Float.MAX_VALUE;
        try {
            FileReader fr = new FileReader(inedges);
            BufferedReader input = new BufferedReader(fr);
            String line;
            while ((line = input.readLine()) != null) {
                if (line.length() > 6) {
                    //System.out.println("trying: " + line);
                    String[] values = line.trim().split("\\s+");
                    if (values.length > 2 && !values[0].equalsIgnoreCase("")) {
                        int c1 = Integer.parseInt(values[0]) - 1;
                        int c2 = Integer.parseInt(values[1]) - 1;
                        float val = Float.parseFloat(values[2]);

                        this.writeEdge(c1, c2, val, output);
                        saved++;

                        if (val > maxVal) {
                            maxVal = val;
                        }
                    }
                }
            }

            fr.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(EdgeLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EdgeLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return maxVal;
    }

    public void writeEdge(int i, int j, float value, DataOutputStream stream) {
        try {
            stream.writeInt(i);
            stream.writeInt(j);
            stream.writeFloat(value);
        } catch (IOException ex) {
            Logger.getLogger(DiskSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean readEdgeFast(DataInputStream is) {
        try {
            int c1 = is.readInt();
            int c2 = is.readInt();
            float value = is.readFloat();

            DiskSupport.reusableC1 = c1;
            DiskSupport.reusableC2 = c2;
            DiskSupport.reusableValue = value;
            return true;
        } catch (EOFException eof) {
            // DO NOTHING, null returned
        } catch (IOException ex) {
            Logger.getLogger(DiskSupport.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        return false;

    }

    private Edge readEdge(DataInputStream is) {
        try {
            int c1 = is.readInt();
            int c2 = is.readInt();
            float value = is.readFloat();

            Edge e = new Edge(c1, c2, value);
            return e;
        } catch (EOFException eof) {
            // DO NOTHING, null returned
        } catch (IOException ex) {
            Logger.getLogger(DiskSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public int sortEdges(String filename, String sortedfile, int maxEdgesInMemForMerge) {
        int parts = 0;
        try {
            // switch to input
            DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));

            while (true) {
                // load maxEdgesRound edges in i-th part into memory and sort
                Set<Edge> edges = this.loadEdges(maxEdgesInMemForMerge, is);
                if (edges.isEmpty()) {
                    break;
                }

                String outfile = filename + "_" + parts + ".out";
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outfile)));
                this.saveEdges(edges, out);
                out.close();

                parts++;

            }

            String[] tobeMerged = new String[parts];
            for (int i = 0; i < parts; i++) {
                tobeMerged[i] = filename + "_" + i + ".out";
            }

            String resultMerge = sortedfile;

            this.mergeStreams(tobeMerged, resultMerge);

            is.close();


        } catch (FileNotFoundException ex) {
            Logger.getLogger(DiskSupport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DiskSupport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return parts;

    }

    /**
     * Important: the method has to store edges sorted
     *
     * @param maxEdgesRound maximum number of edges to be loaded
     * @param is input stream
     * @return sorted set of edges
     */
    public Set<Edge> loadEdges(int maxEdgesRound, DataInputStream is) {
        // load maxEdgesRound from input stream
        Set<Edge> result = new TreeSet<Edge>();
        int edgesLoaded = 0;

        while (edgesLoaded < maxEdgesRound) {
            Edge e = this.readEdge(is);
            if (e == null) {
                break;
            }
            result.add(e);
            edgesLoaded++;
        }
        return result;

    }

    /**
     *
     * @param maxEdgesRound
     * @param is
     * @param edges set of edges in which edges are to be loaded
     * @param parents parents of clusters
     * @param partialValues partial values
     * @param clusters link to current clusters for determining their size
     * @param lambda maximum value of loaded edges
     * @return true if edges remain to be loaded, false if all edges were loaded
     * already
     */
    public boolean loadEdgesForSingleRound(int maxEdgesRound, DataInputStream is, Set<EdgeInterval> edges, Map<Integer, Integer> parents, Map<Edge, ERep> partialValues, List<Cluster> clusters, MyFloat lambda) {


        if (edges.size() == maxEdgesRound) {
            return false;
        }




        while (edges.size() < maxEdgesRound) {
            Edge e = this.readEdge(is);
            if (e == null) {
                return false;
            }
            lambda.setValue(e.value);

            // test rekurzivni
            Integer lastParent1 = e.c1;
            Integer lastParent2 = e.c2;

            // ANCESTORS, neznamena to nahodou vsechny predky?
            Integer parent1 = (e.c1);
            Integer parent2 = (e.c2);


            while (parent1 != null) {
                lastParent1 = parent1;
                parent1 = parents.get(parent1);

            }
            while (parent2 != null) {
                lastParent2 = parent2;
                parent2 = parents.get(parent2);

            }

            parent1 = lastParent1;
            parent2 = lastParent2;


            if (parent1.intValue() != parent2.intValue()) {
                // create EdgeInterval and insert
                //d_pi_pj = d_pi_pj plus eij, ci*cj


                EdgeInterval ei = new EdgeInterval(e);
                ei.setMaxValue(ei.getValue());
                ei.setMinValue(ei.getValue());

                Edge epij = new Edge(parent1.intValue(), parent2.intValue(), -1.f);

                if (!partialValues.containsKey(epij)) {
                    partialValues.put(epij, new ERep(0.f, 0));
                }

                // add edge and its partial values
                partialValues.get(epij).add(ei.getValue(), clusters.get(ei.c1).getSize() * clusters.get(ei.c2).getSize());

                if (!clusters.get(e.c1).isMerged() && !clusters.get(e.c2).isMerged()) {
                    edges.add(ei);
                }

            }

        }

        return true;

    }

    /**
     *
     * @param maxEdgesRound
     * @param is
     * @param edges set of edges in which edges are to be loaded
     * @param parents parents of clusters
     * @param partialValues partial values
     * @param clusters link to current clusters for determining their size
     * @param lambda maximum value of loaded edges
     * @return -2: GOT_STUCK; -1: ALL_LOADED; 0: LIMIT_REACHED; ENUMS were slow,
     * retaining int values
     */
// the same as above, just uses different edge data structure
    public int loadEdgesForSingleRound(int maxEdgesRound, DataInputStream is, SortedEdgeStructure edges, Map<Integer, Integer> parents, PartialValues partialValues, List<Cluster> clusters, MyFloat lambda) {

        boolean fastInsertFirst = false;
        if (edges.size() == 0) {
            fastInsertFirst = true;
        }
        if (edges.size() == maxEdgesRound) {
            return -2;
        }

        while (edges.size() < maxEdgesRound) {
            boolean loaded = this.readEdgeFast(is);
            if (!loaded) {
                return -1;
            }

            lambda.setValue(DiskSupport.reusableValue);

            int parent1 = clusters.get(DiskSupport.reusableC1).getTopMostParent().id;
            int parent2 = clusters.get(DiskSupport.reusableC2).getTopMostParent().id;



            if (parent1 != parent2) {
                if (!partialValues.containsERep(parent1, parent2)) {
                    partialValues.add(parent1, parent2, new ERep(0.f, 0));
                }

                partialValues.get(parent1, parent2).add(DiskSupport.reusableValue, clusters.get(DiskSupport.reusableC1).getSize() * clusters.get(DiskSupport.reusableC2).getSize());

                if (fastInsertFirst) {
                    EdgeInterval toInsert = new EdgeInterval(parent1, parent2, -1.f);
                    edges.addEdgeNoSort(toInsert);
                } else if (!edges.containsEdge(parent1, parent2)) {
                    EdgeInterval toInsert = new EdgeInterval(parent1, parent2, -1.f);
                    edges.addEdgeNoSort(toInsert);
                }

            }
        }

        return 0;

    }

    private void saveEdges(Set<Edge> edges, DataOutputStream os) {
        for (Edge e : edges) {
            this.writeEdge(e, os);
        }
    }

    public void writeEdge(Edge e, DataOutputStream os) {
        this.writeEdge(e.c1, e.c2, e.value, os);
    }

    // finds the index of the edge in prepared[] array with the lowest value
    private int getMin(Edge[] prepared) {
        int min = -1;
        float minValue = Float.MAX_VALUE;
        for (int i = 0; i < prepared.length; i++) {
            if (prepared[i] != null) {
                if (prepared[i].getValue() <= minValue) {
                    min = i;
                    minValue = prepared[i].getValue();
                }
            }
        }
        return min;
    }

    protected void mergeStreams(String[] tobeMerged, String resultMerge) throws FileNotFoundException, IOException {

        int parts = tobeMerged.length;
        // mergesort of saved files into a new large file
        DataInputStream[] streamsToMerge = new DataInputStream[parts];
        for (int i = 0; i < parts; i++) {
            streamsToMerge[i] = new DataInputStream(new BufferedInputStream(new FileInputStream(tobeMerged[i])));
        }

        DataOutputStream outMerged = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(resultMerge)));

        // preload one edge from each stream
        Edge[] prepared = new Edge[parts];
        for (int i = 0; i < parts; i++) {
            prepared[i] = this.readEdge(streamsToMerge[i]);
        }

        //while not all streams are processed, do merge
        while (true) {

            // find minimum, and replace with other from the same stream
            int minIndex = getMin(prepared);

            if (minIndex < 0) {
                break;
            }

            this.writeEdge(prepared[minIndex], outMerged);
            prepared[minIndex] = this.readEdge(streamsToMerge[minIndex]);
        }

        // all edges should be merged to the file named .merged

        // close streams
        outMerged.close();
        for (int i = 0; i < parts; i++) {
            streamsToMerge[i].close();
        }

    }

    // the same as previous, edges in removedEdges are not output
    protected void mergeStreams(String[] tobeMerged, String resultMerge,
            Set<Edge> removedEdges) throws FileNotFoundException, IOException {

        int wrote = 0;

        int parts = tobeMerged.length;
        // mergesort of saved files into a new large file
        DataInputStream[] streamsToMerge = new DataInputStream[parts];
        for (int i = 0; i < parts; i++) {
            streamsToMerge[i] = new DataInputStream(new BufferedInputStream(
                    new FileInputStream(tobeMerged[i])));
        }

        DataOutputStream outMerged = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(resultMerge)));

        // preload one edge from each stream
        Edge[] prepared = new Edge[parts];
        for (int i = 0; i < parts; i++) {
            prepared[i] = this.readEdge(streamsToMerge[i]);
        }

        //while not all streams are processed, do merge
        while (true) {

            // find minimum, and replace with other from the same stream
            int minIndex = getMin(prepared);

            if (minIndex < 0) {
                break;
            }

            if (!removedEdges.contains(prepared[minIndex])) {
                wrote++;
                this.writeEdge(prepared[minIndex], outMerged);
            }
            prepared[minIndex] = this.readEdge(streamsToMerge[minIndex]);
        }

        // all edges should be merged to the file named .merged

        // close streams
        outMerged.close();
        for (int i = 0; i < parts; i++) {
            streamsToMerge[i].close();
        }

    }

    // the same as previous, edges in removedEdges are not output and edges
    // incident to merged clusters are not output
    protected void mergeStreams(String[] tobeMerged, String resultMerge,
            Set<Edge> removedEdges, Set<Cluster> disabledClusters) throws FileNotFoundException, IOException {

        int wrote = 0;

        int parts = tobeMerged.length;
        // mergesort of saved files into a new large file
        DataInputStream[] streamsToMerge = new DataInputStream[parts];
        for (int i = 0; i < parts; i++) {
            streamsToMerge[i] = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(tobeMerged[i])));
        }

        DataOutputStream outMerged = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(resultMerge)));

        // preload one edge from each stream
        Edge[] prepared = new Edge[parts];
        for (int i = 0; i < parts; i++) {
            prepared[i] = this.readEdge(streamsToMerge[i]);
        }

        //while not all streams are processed, do merge
        while (true) {

            // find minimum, and replace with other from the same stream
            int minIndex = getMin(prepared);

            if (minIndex < 0) {
                break;
            }

            // do not output bad edges??
            boolean incident = false;
            for (Cluster c : disabledClusters) {
                if (prepared[minIndex].isIncident(c.getId())) {
                    incident = true;
                }
            }

            if (!removedEdges.contains(prepared[minIndex]) && !incident) {
                wrote++;
                this.writeEdge(prepared[minIndex], outMerged);
            }
            prepared[minIndex] = this.readEdge(streamsToMerge[minIndex]);
        }

        // all edges should be merged to the file named .merged

        // close streams
        outMerged.close();
        for (int i = 0; i < parts; i++) {
            streamsToMerge[i].close();
        }

    }

    float findPsi(String inedges, List<Cluster> clusters) {
        float maxVal = -Float.MAX_VALUE;
        try {
            DataInputStream is = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(inedges),
                    1024 * 1024));
            while (true) {
                int i1 = is.readInt();
                int i2 = is.readInt();
                float val = is.readFloat();
                if (val > maxVal) {
                    maxVal = val;
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(EdgeLoader.class.getName()).log(Level.SEVERE,
                    null, ex);
        } catch (IOException ex) {
        }
        return maxVal;

    }
}
