package upgma;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MemoryConstrainedAverageLinkClustering {

    public static void main(String[] args) throws IOException {

        if (args.length < 3) {
            System.err.println("I need these arguments:");
            System.err.println("- input matrix file");
            System.err.println("- temporary file");
            System.err.println("- output tree file");
            return;
        }

        File matrix = new File(args[0]);
        File temporary = new File(args[1]);
        File tree = new File(args[2]);
        MemoryConstrainedAverageLinkClustering m =
                new MemoryConstrainedAverageLinkClustering();
        m.run(matrix, temporary, tree);
    }

    public void run(File matrix, File temporary, File tree) throws IOException {
        // single round upgma, optimized variant, allows forward fetching when stuck
        SingleRoundUPGMAOptimized sr = new SingleRoundUPGMAOptimized(matrix.getPath());

        sr.setMaxEdgesInMem(50000);
        sr.setMaxEdgesInMemForSort(6000000);
        sr.setLookup(50000);

        sr.setTempFile(temporary.getPath());

        MyTime mt = new MyTime().start();
        Cluster cluster = sr.cluster();
        mt.stop();

        BufferedWriter bw = new BufferedWriter(new FileWriter(tree));
        sr.getStringHierarchyPlain(cluster, bw);
        bw.close();
    }

    public static void test(String[] args) {

        if (args.length < 3) {
            System.err.println("I need these arguments:");
            System.err.println("- input matrix file");
            System.err.println("- temporary file");
            System.err.println("- output tree file");
            return;
        }

        String matrix = args[0];
        String temporary = args[1];

        // naive upgma
        UPGMA u = new NaiveUPGMA(matrix);

        // single round upgma, optimized variant, allows forward fetching when stuck
        SingleRoundUPGMAOptimized u_so = new SingleRoundUPGMAOptimized(matrix);

        u_so.setMaxEdgesInMem(50000);
        u_so.setMaxEdgesInMemForSort(6000000);
        u_so.setLookup(50000);

        u_so.setTempFile(temporary);
        UPGMA uso = u_so;

        Cluster c = u.cluster();

        // TIME init
        MyTime mt = new MyTime().start();

        Cluster cso = uso.cluster();

        // TIME evaluate
        mt.stop();
    }
}
