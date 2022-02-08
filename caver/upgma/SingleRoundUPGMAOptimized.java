package upgma;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleRoundUPGMAOptimized extends UPGMA {

    private int maxEdgesInMem = 100000;
    private int maxEdgesInMemForSort = 100000;
    private int lookup = 1000;
    // if the input file with edges is already in binary format
    private boolean inputIsBinary = false;
    private String tempFile = "w:/upgma.txt";
    // our binary file format for storing edges;

    /**
     * UPGMA constructor
     *
     * @param string textual file with edges; on each line, whitespace separated
     * int int float
     */
    public SingleRoundUPGMAOptimized(String string) {
        this(string, false);
    }

    /**
     * UPGMA constructor
     *
     * @param string edges file which contains binary edges (int int float) or
     * textual edges
     * @param binary true if the input file is binary, false for textual file
     */
    public SingleRoundUPGMAOptimized(String string, boolean binary) {
        this.edgesFile = string;
        this.inputIsBinary = binary;

        if (inputIsBinary) {
            // load items from binary form 0-based file, no switch
            List<Item> localItems = EdgeLoader.loadItemsBinary(edgesFile);
            for (Item i : localItems) {
                this.addItem(i);
            }

        } else {
            // load items from 1-based file, switch to 0-based
            List<Item> localItems = EdgeLoader.loadItems(edgesFile);
            for (Item i : localItems) {
                this.addItem(i);
            }
        }
    }

    public void setInputBinary() {
        this.inputIsBinary = true;
    }

    public void setMaxEdgesInMemForSort(int maxEdgesInMemForSort) {
        this.maxEdgesInMemForSort = maxEdgesInMemForSort;
    }

    public void setMaxEdgesInMem(int edges) {
        this.maxEdgesInMem = edges;
    }

    public void setTempFile(String tempFile) {
        this.tempFile = tempFile;
    }

    @Override
    public Cluster cluster() {

        // create one-item clusters
        for (Item i : items) {
            Cluster c = new Cluster();
            c.addItem(i);
            c.setId(clusters.size());
            clusters.add(c);
        }

        String inedges = edgesFile;
        String infile = tempFile;
        String sortedfile = infile + ".sortedFirst";

        DiskSupport ds = new DiskSupport();

        float maxEdgePsi = 0.f;

        if (!this.inputIsBinary) {
            try {
                DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(infile)));
                // onfly computation of distances
                // load edges
                maxEdgePsi = ds.resaveAllEdges(inedges, output, clusters);
                output.close();
            } catch (Exception e) {
                Logger.getLogger("caver").log(Level.SEVERE, "MCUPGMA", e);
            }
        } else {
            // edges already in binary format, only determine psi
            maxEdgePsi = ds.findPsi(inedges, clusters);

        }

        Logger.getLogger("caver").log(Level.FINEST, "psi: {0}", maxEdgePsi);

        int parts = 0;
        if (!this.inputIsBinary) {
            parts = ds.sortEdges(infile, sortedfile, this.maxEdgesInMemForSort);
        } else {
            parts = ds.sortEdges(inedges, sortedfile, maxEdgesInMemForSort);
        }

        // delete sorted parts
        for (int i = 0; i < parts; i++) {
            Tools.delete(infile + "_" + i + ".out");
        }

        // delete original unsorted large file
        Tools.delete(infile);

        // keep only sorted large file, switch it to original name
        Tools.renameTo(sortedfile, infile);



        // all edges sorted and saved as "infile"
        SortedEdgeStructure kEdges = new SortedEdgeStructure();

        /*
         * parents no longer needed, but methods require them to be passed as
         * arguments therefore left here as null link
         */
        Map<Integer, Integer> parents = null;

        // partial values for single round UPGMA
        PartialValues partialValues = new PartialValues();


        Cluster lastMerged = null;
        DataInputStream is;
        try {
            // stream with infile
            is = new DataInputStream(new BufferedInputStream(
                    new FileInputStream(infile), 1024 * 1024));


            int round = -1;
            int areThereMoreEdges = 0;
            //load lambda
            MyFloat myLambda = new MyFloat(-Float.MAX_VALUE);

            while (areThereMoreEdges == 0) // round
            {
                // i-th round
                round++; // round now denotes the number of re-loading edges into memory up to the limit M

                areThereMoreEdges = ds.loadEdgesForSingleRound(maxEdgesInMem, is, kEdges, parents, partialValues, clusters, myLambda);


                if (areThereMoreEdges == -2) {
                    maxEdgesInMem += lookup;
                    areThereMoreEdges = ds.loadEdgesForSingleRound(maxEdgesInMem, is, kEdges, parents, partialValues, clusters, myLambda);
                }

                /*
                 * if (areThereMoreEdges == -1) { System.out.println("all " +
                 * kEdges.size() + " edges in memory"); }
                 */

                float lambda = myLambda.getValue();
                // update uij, lij so that merges are possible
                for (EdgeInterval ein : kEdges) {

                    if (ein.getValue() >= 0) {
                        continue;
                    }

                    Cluster cin1 = clusters.get(ein.c1);
                    Cluster cin2 = clusters.get(ein.c2);

                    ERep ere = partialValues.getOrdered(ein.c1, ein.c2);

                    int multiplier = cin1.getSize() * cin2.getSize();
                    int multiMinus = multiplier - ere.count;
                    float lower = (ere.value + lambda * multiMinus) / (multiplier);
                    float upper = (ere.value + maxEdgePsi * multiMinus) / (multiplier);

                    ein.setMinValue(lower);
                    ein.setMaxValue(upper);
                    if (lower == upper) {
                        ein.setValue(upper);
                    }
                }

                // sort edges for searching
                kEdges.rebuild();

                while (true) // edges exist in the round which can be processed
                {
                    if (kEdges.size() == 0) {
                        break;
                    }

                    if (kEdges.size() < this.maxEdgesInMem - 1) {
                        this.maxEdgesInMem = kEdges.size();
                    }


                    EdgeInterval remove = null; // so far found minimum

                    EdgeInterval edgeExists = kEdges.getFirstEdge(); // candidate for removal


                    float uij = edgeExists.getMaxValue();

                    boolean condition = true;

                    // try to use reversed order:
                    for (EdgeInterval testCont : kEdges) {

                        float lrs = testCont.getMinValue();

                        if (uij > lrs || lrs > lambda) {
                            condition = false;
                            break;
                        }

                    }

                    if (condition) {
                        remove = edgeExists;
                    } else {
                        break;
                    }

                    // test all clusters, merge
                    Cluster ci = clusters.get(remove.c1);
                    Cluster cj = clusters.get(remove.c2);

                    Cluster merged = Cluster.mergeClusters(ci, cj);
                    merged.setMergeDist(remove.getMaxValue());
                    merged.setId(clusters.size()); // set id and then add, i.e. for size 0 we add index 0 and insert, then size will be 1 and newly inserted will get 1
                    clusters.add(merged);

                    // traverse all leaf clusters and set topmost parent to "merged"
                    merged.setTopMostParents(merged);


                    lastMerged = merged;

                    Logger.getLogger("caver").log(Level.FINEST,
                            "JOINED: {0} <- {1} {2}",
                            new Object[]{merged.getId(),
                                ci.getId(), cj.getId()});

                    kEdges.removeEdgeFirst(remove.c1, remove.c2);

                    partialValues.removeOrdered(remove.c1, remove.c2);

                    Set<Integer> interestingClusters = kEdges.getIntersected(
                            remove.c1, remove.c2);

                    for (int interes : interestingClusters) {

                        Cluster cl = clusters.get(interes);

                        ERep rep_il = partialValues.get(cl.id, remove.c1);
                        ERep rep_jl = partialValues.get(cl.id, remove.c2);

                        ERep rep_kl = ERep.addNullAllowed(rep_il, rep_jl);

                        partialValues.addOrdered(cl.id, merged.getId(), rep_kl);

                        int multiplier = cl.getSize() * merged.getSize();
                        int multMinusCount = multiplier - rep_kl.count;
                        float l_kl = (rep_kl.value + lambda * multMinusCount)
                                / multiplier;
                        float u_kl = (rep_kl.value + maxEdgePsi * multMinusCount)
                                / multiplier;

                        float evalue = (u_kl == l_kl) ? u_kl : -1.f;
                        EdgeInterval ekl = new EdgeInterval(cl.id,
                                merged.getId(), evalue, l_kl, u_kl);

                        kEdges.addEdgeWhichDidNotExistOrdered(ekl);

                        if (remove.c1 <= cl.id) {
                            kEdges.removeEdgeOrdered(remove.c1, cl.id);
                            partialValues.removeOrdered(remove.c1, cl.id);
                        } else {
                            kEdges.removeEdgeOrdered(cl.id, remove.c1);
                            partialValues.removeOrdered(cl.id, remove.c1);
                        }

                        if (remove.c2 <= cl.id) {
                            kEdges.removeEdgeOrdered(remove.c2, cl.id);
                            partialValues.removeOrdered(remove.c2, cl.id);
                        } else {
                            kEdges.removeEdgeOrdered(cl.id, remove.c2);
                            partialValues.removeOrdered(cl.id, remove.c2);
                        }

                    }




                } //eof round in which some edges were loaded
            } // eof round cycling

            // close input stream after all rounds ended
            is.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SingleRoundUPGMAOptimized.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SingleRoundUPGMAOptimized.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lastMerged;
    }

    void setLookup(int i) {
        this.lookup = i;
    }
}
