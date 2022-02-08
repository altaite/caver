package upgma;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetricRandomTester {

    public static void main(String[] args) {
        BufferedWriter bw = null;
        try {

            String edgesBinaryFile = "V:/_data_eva/testRandom100k/matrix.edges.bin";
            String tempfile = "V:/_data_eva/testRandom100k/tmp.txt";
            String treefile = "V:/_data_eva/testRandom100k/tree.txt";

            SingleRoundUPGMAOptimized sr = new SingleRoundUPGMAOptimized(
                    edgesBinaryFile, true);


            sr.setMaxEdgesInMem(50000);
            sr.setMaxEdgesInMemForSort(6000000);
            sr.setLookup(50000);
            sr.setTempFile(tempfile);
            MyTime mt = new MyTime().start();
            Cluster cluster = sr.cluster();
            mt.stop();
            System.out.println("MCUPGMA time: " + mt.timeSeconds());
            bw = new BufferedWriter(new FileWriter(treefile));
            sr.getStringHierarchyPlain(cluster, bw);
            bw.close();


        } catch (IOException ex) {
            Logger.getLogger(MetricRandomTester.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
}
