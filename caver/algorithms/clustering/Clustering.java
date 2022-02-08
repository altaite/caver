package algorithms.clustering;

import algorithms.clustering.layers.LayeredTunnel;
import algorithms.clustering.layers.LayeredTunnels;
import caver.CalculationSettings;
import caver.Printer;
import caver.tunnels.Tunnel;
import caver.tunnels.TunnelCostComparator;
import caver.ui.CalculationException;
import chemistry.pdb.SnapId;
import geometry.primitives.Point;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import upgma.MemoryConstrainedAverageLinkClustering;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 *
 * The class associates algorithms to perform hierarchical clustering of
 * tunnels.
 *
 */
public class Clustering {

    private static int unId = 999999;
    Point source_;
    private Tunnel[] tunnels_;
    private CalculationSettings cs_;
    public static final String[] colors_ = {"red", "green", "blue", "cyan",
        "magenta", "yellow", "brown", "brightorange",
        "lightteal", "limon", "darksalmon", "lime", "purpleblue"};

    public Clustering(Point source, Collection<Tunnel> tunnels,
            CalculationSettings settings) throws CalculationException {

        if (0 == tunnels.size()) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "No tunnels in cluster .");
        }

        source_ = source;
        cs_ = settings;
        tunnels_ = new Tunnel[tunnels.size()];
        int i = 0;
        for (Tunnel t : tunnels) {
            tunnels_[i++] = t;
        }
    }

    /*
     * Performs simple clustering procedure (see below) and returns one tunnel
     * representant for each cluster.
     *
     * Starts with cheapest tunnel and iteratively removes all tunnels within
     * distance cs_.getFrameClusteringThreshold(). Then continues with the next
     * remaining cheapest tunnel until all tunnels are either sorted out or
     * reported as cluster representant.
     *
     * The algorithm is meant to be used for removal of redundant tunnels with
     * the scope of single molecular dynamics snapshot.
     *
     */
    public SortedSet<Tunnel> bestStaysClustering(LayeredTunnels layered)
            throws IOException {

        Map<Tunnel, Integer> indeces = new HashMap<Tunnel, Integer>();
        for (int i = 0; i < tunnels_.length; i++) {
            indeces.put(tunnels_[i], i);
        }

        List<Tunnel> sorted = new ArrayList<Tunnel>();
        sorted.addAll(Arrays.asList(tunnels_));
        Collections.sort(sorted, new TunnelCostComparator());
        SortedSet<Tunnel> cores = new TreeSet<Tunnel>();

        while (!sorted.isEmpty()) {

            Tunnel core = sorted.remove(0);
            cores.add(core);
            int a = indeces.get(core);

            List<Integer> redundant = new ArrayList<Integer>();
            for (int i = 0; i < sorted.size(); i++) {
                Tunnel t = sorted.get(i);
                int b = indeces.get(t);
                float d = layered.getDistance(a, b);
                if (d < cs_.getFrameClusteringThreshold()) {
                    redundant.add(i);
                }
            }
            for (int i = redundant.size() - 1; 0 <= i; i--) {
                int r = redundant.get(i);
                sorted.remove(r);
            }

        }

        return cores;
    }


    /*
     * Loads tree in the MCUPGMA program file format. For the program itself,
     * see
     *
     * Loewenstein Y, Portugaly E, Fromer M, Linial M. Efficient algorithms for
     * accurate hierarchical clustering of huge datasets: tackling the entire
     * protein space. Bioinformatics 2008 24: i41-i49; Presented at ISMB 2008,
     * Toronto.
     *
     * http://www.protonet.cs.huji.ac.il/mcupgma/
     */
    public Clusters loadTree(File treeFile, double cut, LayeredTunnels ls)
            throws IOException {
        Clusters clusters = new Clusters(cs_);
        BufferedReader br = new BufferedReader(new FileReader(treeFile));
        String line;
        double dist = 0;
        int merges = 0;
        for (int i = 0; i < tunnels_.length; i++) {
            clusters.addTunnel(ClusterId.create(i), tunnels_[i], false);
        }
        double last = 0;
        boolean noViolation = true;
        while (null != (line = br.readLine())
                && dist < cut && noViolation) {
            if (line.trim().length() == 0) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line, "\t ;");
            ClusterId[] childs = new ClusterId[2];
            for (int i = 0; i < 2; i++) {
                childs[i] = ClusterId.create(
                        Integer.parseInt(st.nextToken()) - 1);
            }
            last = dist;
            dist = Double.parseDouble(st.nextToken());
            if (dist < last) {
                noViolation = false;
                Printer.warn("Tree is not valid, only merging operations "
                        + "with distances up to " + last + " were used, "
                        + "clustering granularity might be smaller "
                        + "than desired.");
            } else if (dist < cut) {
                ClusterId parent =
                        ClusterId.create(Integer.parseInt(st.nextToken()) - 1);

                if (cs_.checkClustering()) {
                    if (merges > 0) {

                        double d = clusters.getDistance(childs, ls);
                        //Printer.warn(d +" - " + dist);
                        if (Math.abs(d - dist) > 0.00001) {
                            Printer.warn(
                                    "Clustering error " + d + " - " + dist
                                    + " in merge " + merges);
                        }
                    }
                }
                clusters.merge(childs, parent);

                merges++;

            }
        }
        return clusters;
    }

    /*
     * Computes pairwise distances for all pair of tunnels from layered.
     */
    public void computeMatrix(LayeredTunnels layered,
            File matrixFile) throws IOException {
        Printer.println("Calculating distance matrix");


        int nan = 0;
        BufferedWriter bw = new BufferedWriter(
                new FileWriter(matrixFile));
        for (int x = 0; x < layered.size(); x++) {
            for (int y = 0; y < layered.size(); y++) {
                if (x < y) {
                    float d = layered.getDistance(x, y);
                    if (Float.isNaN(d)) {
                        nan++;
                    }
                    bw.write((x + 1) + "\t" + (y + 1) + "\t" + d + "\n");
                }
            }
        }
        bw.close();
        if (0 < nan) {
            throw new RuntimeException("There are " + nan
                    + " overflows in matrix.");
        }
    }

    /*
     * Runs the Java implementation of MCUPGMA algorithm, for more details about
     * the algorithm see * Loewenstein Y, Portugaly E, Fromer M, Linial M.
     * Efficient algorithms for accurate hierarchical clustering of huge
     * datasets: tackling the entire protein space. Bioinformatics 2008 24:
     * i41-i49; Presented at ISMB 2008, Toronto.
     *
     * For its alternative C++ implementation see
     * http://www.protonet.cs.huji.ac.il/mcupgma/
     */
    public void memoryConstrainedAverageLink(
            File matrixFile,
            File clusteringTemporaryFile,
            File treeFile)
            throws IOException {

        MemoryConstrainedAverageLinkClustering m =
                new MemoryConstrainedAverageLinkClustering();
        m.run(matrixFile, clusteringTemporaryFile, treeFile);
    }

    public void saveTrainingData(Clusters clusters, LayeredTunnels layered,
            File file, SortedSet<SnapId> snaps)
            throws IOException {

        clusters.calculateStatistics(snaps,
                cs_.getThroughputBestFraction());
        clusters.computePriorities();

        int N = cs_.getMinTrainingTunnels();

        Set<Cluster> suitable = new HashSet<Cluster>();
        for (Cluster c : clusters.getClusters()) {
            if (N <= c.size() && c.getPriority()
                    <= cs_.getMaxTrainingClusters()) {
                suitable.add(c);
            }
        }

        String ids = "";
        for (Cluster c : suitable) {
            ids += c.getId() + ",";
        }
        ids += "X";

        if (suitable.isEmpty()) {
            throw new RuntimeException("Only one cluster for training. "
                    + "Please decrease distance_threshold or set "
                    + "greater min_training_tunnels.");
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write("@relation tunnels\n");
        for (int i = 0; i < layered.getLayersCount(); i++) {
            bw.write("@attribute X" + i + " numeric\n");
            bw.write("@attribute Y" + i + " numeric\n");
            bw.write("@attribute Z" + i + " numeric\n");
        }

        bw.write("@attribute length numeric\n");

        bw.write("@attribute cluster {" + ids + "}\n");
        bw.write("@data\n");
        for (LayeredTunnel lt : layered) {
            Tunnel t = lt.getTunnel();
            Cluster c = t.getCluster();

            if (lt.size() != layered.getLayersCount()) {
                Logger.getLogger("caver").log(Level.WARNING,
                        "ARFF: layeres number not constant.");
            }

            for (int i = 0; i < lt.size(); i++) {
                bw.write(
                        lt.getX(i) + "," + lt.getY(i) + "," + lt.getZ(i) + ",");
            }
            bw.write(lt.getTunnel().getLength() + ",");

            if (suitable.contains(c)) {
                bw.write(c.getId().get() + "\n");
            } else {
                bw.write("X\n");
            }

        }
        bw.close();
    }

    public static Instances load(File in) throws IOException {
        try {
            DataSource source = new DataSource(in.getPath());
            Instances inst = source.getDataSet();
            if (inst.classIndex() == -1) {
                inst.setClassIndex(inst.numAttributes() - 1);
            }
            return inst;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static String getValue(Instances inst, int i, int a) {
        Attribute A = inst.attribute(a);
        double value = inst.instance(i).value(A);
        int index = (int) Math.round(value);
        return inst.attribute(a).value(index);
    }

    public static String getClassValue(Instances inst, int i) {
        return getValue(inst, i, inst.numAttributes() - 1);
    }

    public Classifier loadClassifier() {
        try {
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(cs_.getClassifierFile()));
            Classifier cls = (Classifier) ois.readObject();
            ois.close();
            return cls;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveClassifier(Classifier cls) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(cs_.getClassifierFile()));
            oos.writeObject(cls);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Clusters classify(List<Tunnel> tunnels, LayeredTunnels layered,
            Clusters clustersOld) throws IOException {

        try {

            int count = 0;
            Instances data = load(cs_.getTrainingFile());

            Clusters clusters = new Clusters(cs_);

            Classifier cls;


            Printer.println("Training on " + data.numInstances()
                    + " tunnels.");
            if ("knn".equals(cs_.getClassifier().toLowerCase())) {
                cls = new weka.classifiers.lazy.IBk();
            } else if ("svm".equals(cs_.getClassifier().toLowerCase())) {
                cls = new weka.classifiers.functions.SMO();
            } else if ("naive_bayes".equals(cs_.getClassifier().toLowerCase())) {
                cls = new weka.classifiers.bayes.NaiveBayes();
            } else if ("logistic_regression".equals(
                    cs_.getClassifier().toLowerCase())) {
                cls = new weka.classifiers.functions.Logistic();
            } else {
                Logger.getLogger("caver").log(Level.WARNING,
                        "Unknown classifier {0}, using k-means.",
                        cs_.getClassifier());
                cls = new weka.classifiers.lazy.IBk();
            }

            if (cs_.evaluateClassification()) {
                Evaluation eval = new Evaluation(data);
                eval.crossValidateModel(
                        cls, data, 2, new Random(cs_.getSeed()));
                Printer.println(eval.toSummaryString(), Printer.NORMAL);
                Printer.println(eval.toMatrixString(), Printer.NORMAL);
            }

            cls.buildClassifier(data);

            Instance instance = new Instance(data.firstInstance());

            data.delete();

            Printer.println("Classifying " + tunnels.size() + " tunnels."
                    + Printer.NORMAL);
            Printer.println(
                    "Classifying " + layered.size() + " layered tunnels.");


            for (Tunnel t : clustersOld.getTunnels()) {
                Cluster c = t.getCluster();
                if (cs_.getMinTrainingTunnels() <= c.size()
                        && c.getPriority() <= cs_.getMaxTrainingClusters()) {
                    clusters.addTunnel(c.getId(), t, false);
                }
            }

            for (LayeredTunnel lt : layered) {

                int attIndex = 0;
                for (int i = 0; i < lt.size(); i++) {
                    instance.setValue(attIndex++, lt.getX(i));
                    instance.setValue(attIndex++, lt.getY(i));
                    instance.setValue(attIndex++, lt.getZ(i));
                }
                instance.setValue(attIndex++, lt.getTunnel().getLength());
                if (0 == lt.getTunnel().getLength()) {

                    Logger.getLogger("caver").log(Level.WARNING,
                            "Tunnel legnth 0 at classification.");
                }

                data.add(instance);
                instance.setDataset(data);

                instance.setClassMissing();

                double d = cls.classifyInstance(instance);

                String clusterS = instance.classAttribute().value(
                        (int) Math.round(d));
                int clusterN;
                if (!clusterS.equals("X")) {
                    clusterN = Integer.parseInt(clusterS);
                    boolean added = clusters.addTunnel(
                            ClusterId.create(clusterN), lt.getTunnel(),
                            true && cs_.checkDistance());
                    if (!added) {
                        clusters.addTunnel(ClusterId.create(unId),
                                lt.getTunnel(), false);
                    }


                } else {
                    if (cs_.generateUnclassifiedCluster()) {
                        clusters.addTunnel(ClusterId.create(unId),
                                lt.getTunnel(), false);
                    }

                    count++;
                }
            }



            Printer.println(count + " tunnels classified as small clusters.");
            Printer.println("Clustered tunnels after classification "
                    + clusters.getTunnels().size());

            return clusters;

        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

    public int getTunnelNumber() {
        return tunnels_.length;
    }
}
