package caver.ui;

import algorithms.clustering.ClusterId;
import algorithms.clustering.Clustering;
import algorithms.clustering.Clusters;
import algorithms.clustering.layers.AverageSurface;
import algorithms.clustering.layers.LayeredTunnel;
import algorithms.clustering.layers.LayeredTunnels;
import algorithms.search.DijkstraTunnelComputation;
import algorithms.search.TunnelComputation;
import algorithms.search.Voids;
import algorithms.triangulation.SphereSpaceTriangulator;
import algorithms.triangulation.VoronoiDiagram;
import caver.CalculationSettings;
import caver.Clock;
import caver.Printer;
import caver.tunnels.Tunnel;
import caver.tunnels.TunnelCostComparator;
import caver.tunnels.Tunnels;
import caver.util.CaverCounter;
import caver.util.HotSpotWizardMyUtil;
import chemistry.MolecularSystem;
import chemistry.pdb.PdbFileProcessor;
import chemistry.pdb.PdbLine;
import chemistry.pdb.PdbUtil;
import chemistry.pdb.SnapId;
import geometry.primitives.NumberedSphere;
import geometry.primitives.Point;
import geometry.primitives.Sphere;
import java.io.*;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.*;

/*
 * Main class for running a Caver computation.
 */
public class Launcher {

    private CalculationSettings cs_;
    private int after, before;

    public Launcher(CalculationSettings settings) {
        this.cs_ = settings;
    }

    public void mergePdbFiles(SortedMap<SnapId, File> files) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(
                cs_.getTrajectoryFile()));

        for (SnapId snap : files.keySet()) {

            BufferedReader br = new BufferedReader(new FileReader(
                    files.get(snap)));
            String line;
            bw.write("MODEL        " + snap.getNumber());
            bw.newLine();

            while (null != (line = br.readLine())) {
                if (PdbLine.isCoordinateLine(line)) {
                    bw.write(line);
                    bw.newLine();
                }
            }

            bw.write("ENDMDL");
            bw.newLine();
        }
        bw.close();
    }

    public void findTunnels(SortedMap<SnapId, File> files)
            throws IOException,
            CalculationException {

        Printer.println("Settings loaded from:\n"
                + cs_.getConfigFile(), Printer.NORMAL);

        Printer.println("\nBasic parameters:", Printer.NORMAL);

        if (null != cs_.getStartingPoint()) {
            Printer.println("starting_point_coordinates " + cs_.getStartingPoint().toString(),
                    Printer.NORMAL);
        }
        if (null != cs_.getStartingAtoms()
                && !cs_.getStartingAtoms().isEmpty()) {
            Printer.println("starting_point_atom " + cs_.iterableToString(
                    cs_.getStartingAtoms(), " "), Printer.NORMAL);
        }
        if (null != cs_.getStartingResidues()
                && !cs_.getStartingResidues().isEmpty()) {
            Printer.println("starting_point_residue " + cs_.iterableToString(
                    cs_.getStartingResidues(), " "), Printer.NORMAL);
        }

        Printer.println("probe_radius " + cs_.getProbeRadius(),
                Printer.NORMAL);

        Printer.println("shell_radius " + cs_.getShellRadius(),
                Printer.NORMAL);

        Printer.println("shell_depth " + cs_.getShellDepth(),
                Printer.NORMAL);

        Printer.println("frame_weighting_coefficient "
                + cs_.getFrameLayersSettings().getWeightingCoefficient(),
                Printer.NORMAL);

        Printer.println("frame_clustering_threshold "
                + cs_.getFrameClusteringThreshold(),
                Printer.NORMAL);

        Printer.println("", Printer.NORMAL);

        File[] tunnelFiles = cs_.getTunnelsDir().listFiles();



        if (cs_.protectTunnelsIfMoreSnapshotsThan() < tunnelFiles.length) {
            throw new CalculationException("Directory " + cs_.getTunnelsDir()
                    + " contains computed tunnels from " + tunnelFiles.length
                    + " snapshots. If you really want to compute all tunnels "
                    + "again, please delete it manually. I do not dare to "
                    + "delete result of so long computation. "
                    + "If you wanted to continue work with those tunnels, "
                    + "set 'load_tunnels' to 'yes' in config.txt.");
        } else {
            cs_.cleanDirectory(cs_.getTunnelsDir());
            cs_.cleanDirectory(cs_.getEdgesDir());
        }
        cs_.getCorridorFile().delete();
        cs_.getVoidFile().delete();
        cs_.getVoidFilePrecursor().delete();

        CaverCounter counter = new CaverCounter(1);
        for (SnapId snap : files.keySet()) {
            try {

                Printer.println("*** Processing " + snap + " ***", Printer.NORMAL);
                File file = files.get(snap);

                cs_.initRandom(deriveNumber(file.getName()));

                PdbFileProcessor pfp = cs_.getPdbFileProcessor(file);

                MolecularSystem ms = cs_.createMolecularSystem(pfp);

                if (ms.isEmpty()) {
                    String msg =
                            "There are no atoms, please check atom filters "
                            + "and PDB files.";
                    throw new CalculationException(msg);
                }



                Clock.start("approximate and shatter");
                List<Sphere> shattered = shatter(ms.getSpheresOneRadiiApproximation(
                        cs_.getDissimilarRadiusTolerance(),
                        cs_.getNumberOfSpheres(),
                        cs_.addCentralSphere(),
                        cs_.getRandom()));
                if (cs_.saveApproximation() && cs_.isAdmin()) {
                    PdbUtil.saveSpheres(shattered, cs_.getApproximationFile(file));
                }

                Clock.stop("approximate and shatter");
                Clock.start("Voronoi Diagram construction");
                VoronoiDiagram vd = constructVoronoiDiagram(shattered);
                shattered = null;
                Clock.stop("Voronoi Diagram construction");

                if (false) {
                    assert vd.check();
                }

                Point startGuess = null;
                if (cs_.doVoids()) {
                    Printer.println("Automaticed geometric idenfication of "
                            + "starting point.");
                    Voids voids = new Voids(vd);
                    voids.run();
                    startGuess = voids.getClosest(ms.getCenter(), 1.4);
                    //voids.save(cs_);
                    //voids.savePoints(cs_.getDeepPointsFile());
                }

                Point start = cs_.createStartingPoint(pfp);
                if (null == start) {
                    if (null != startGuess) {
                        start = startGuess;
                    } else {
                        throw new RuntimeException("No starting point specified "
                                + "in " + cs_.getConfigFile());
                    }
                }

                TunnelComputation tc = new DijkstraTunnelComputation();


                Clock.start("compute tunnels");

                Tunnels tunnels;
                if (cs_.getWaypointCount() <= 1) {
                    tunnels = tc.computeTunnelsBlock(cs_, vd, start,
                            SnapId.create(pfp.getFile()), counter);

                } else {
                    double proteinR = ms.getProteinRadius(start);
                    Printer.println("Protein radius: " + proteinR);
                    tunnels = tc.computeTunnels(cs_, vd, start, proteinR,
                            SnapId.create(pfp.getFile()), counter);
                }

                if (cs_.isAdmin()) {
                    vd.saveTree(cs_.getCorridorFile());
                    vd.saveVoid(cs_.getVoidFilePrecursor(), snap);
                }
                int total = tunnels.size();
                Clock.stop("compute tunnels");

                Printer.println(tunnels.size() + " tunnels found in "
                        + snap);

                if (cs_.doFrameClustering()) {
                    Clock.start("redundant tunnels removal (frame clustering)");

                    List<Tunnel> ts = tunnels.getTunnels();
                    AverageSurface surface = null;
                    if (cs_.doAverageSurfaceFrame()) {
                        surface = new AverageSurface(
                                ts, tunnels.getVoronoiOrigin(), cs_);
                    }
                    LayeredTunnels lts = new LayeredTunnels(
                            tunnels.getVoronoiOrigin(),
                            surface, ts, // just tunnels for exact clutering
                            false, cs_.doAverageSurfaceFrame(),
                            cs_.getFrameLayersSettings(), cs_);

                    tunnels.cluster(lts, cs_);
                    Printer.println((total - tunnels.size())
                            + " redundant tunnels removed.", Printer.NORMAL);
                    Clock.stop("redundant tunnels removal (frame clustering)");
                }

                int out = tunnels.filter(cs_.getMaxNumberOfTunnels());
                if (0 < out) {
                    Printer.println(out + " tunnels removed because of "
                            + "number_of_tunnels parameter.", Printer.NORMAL);
                }

                Printer.println(tunnels.size() + " tunnels stored.",
                        Printer.NORMAL);
                tunnels.assignPriorities(); // provisional, tunnels disapears also lower




                tunnels.save(snap, cs_);
            } catch (Exception e) {
                Logger.getLogger("caver").log(
                        Level.SEVERE, "computeTunnels()", e);
            }
        }
        Printer.println("", Printer.NORMAL);
        if (cs_.generateVoronoi() && cs_.isAdmin()) {
            PdbUtil.finalizeFile(PdbUtil.Visualizer.PyMOL,
                    cs_.getVoidFilePrecursor(), cs_.getVoidFile());
        }
    }

    private void printTransformationTable() {
        String n = "relative_range";
        String t = "point_importance";
        Printer.println("Layers: " + cs_.getLayersCount());
        Printer.println("Importance of tunnel points in tunnel-tunnel "
                + "distance calculation");
        double d = 0;
        for (int i = 0; i < cs_.getLayersCount(); i++) {
            d += 1.0 / cs_.getLayersCount();
            n += " " + d;
            t += " " + LayeredTunnel.transformation(d,
                    cs_.getGlobalLayersSettings().getWeightingCoefficient());
        }
        Printer.println(n);
        Printer.println(t);

        Printer.println();

    }

    public Clusters cluster(SortedMap<SnapId, File> files) throws IOException,
            CalculationException {

        cs_.initRandom();

        cs_.startProducingPoints(true);

        if (cs_.isTunnelsDirEmpty()) {
            throw new CalculationException("Computed tunnels missing in "
                    + cs_.getTunnelsDir() + ", set load_tunnels to 'no'.");
        }

        SortedSet<SnapId> snaps = (SortedSet<SnapId>) files.keySet();

        printTransformationTable();

        SortedMap<SnapId, Point> frameToStart = new TreeMap<SnapId, Point>();

        List<Tunnel> allTunnels = new ArrayList<Tunnel>();
        SortedMap<SnapId, Tunnels> tunnelsByFrame =
                new TreeMap<SnapId, Tunnels>();
        for (SnapId snap : files.keySet()) {
            File f = cs_.getTunnelsFile(snap);
            if (!f.exists()) {
                Logger.getLogger("caver").log(Level.WARNING, "No file with "
                        + "tunnels {0}" + " exists, clustering will continue "
                        + "without this snapshot.", f);
                continue;
            }

            File tf = cs_.getTunnelsFile(snap);
            Printer.println("Loading tunnels from " + tf + ".", Printer.NORMAL);
            Tunnels tunnels = Tunnels.create(tf, cs_);

            tunnelsByFrame.put(snap, tunnels);
            Printer.println("Loaded " + tunnels.size() + " tunnels for "
                    + snap + ".", Printer.NORMAL);

            int out = tunnels.filter(cs_.getMaxNumberOfTunnels());
            if (0 < out) {
                Printer.println(out + " tunnels discarded according to "
                        + "number_of_tunnels parameter.");
            }
            out = tunnels.filter(cs_.getProbeRadius());
            if (0 < out) {
                Printer.println(out + " tunnels discarded according to "
                        + "probe_radius parameter.");
            }

            frameToStart.put(snap, tunnels.getVoronoiOrigin());

            if (cs_.swap()) {
                List<Tunnel> ts = tunnels.getTunnels();
                for (Tunnel t : ts) {
                    t.deleteEdges();
                }
            }

            allTunnels.addAll(tunnels.getTunnels());

            Printer.println(allTunnels.size() + " tunnels successfully loaded.",
                    Printer.NORMAL);
        }
        Printer.println("", Printer.NORMAL);

        Collections.sort(allTunnels, new TunnelCostComparator());

        if (cs_.saveAllTunnels()) {
            Visualization viz = new Visualization();
            viz.visualizeTunnels(allTunnels,
                    cs_.getVisualizationTunnelSamplingStep(),
                    cs_.visualizeMaxTunnels(),
                    cs_.getTunnelsVisualizationFile());
        }

        cs_.setFrameToStart(frameToStart);

        cs_.setTunnelsByFrame(tunnelsByFrame);

        int tunnelCount = allTunnels.size();

        if (allTunnels.isEmpty()) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "No tunnels for clustering, computation was not finished. "
                    + "Is the starting point where it should be? Try to run "
                    + "view_timeless.py in PyMOL, are origins points in desired "
                    + "starting place? ");
            return new Clusters(cs_);
        }

        int index = 1;
        for (Tunnel t : allTunnels) {
            t.setNumber(index++);
        }
        Point averageOrigin = Tunnel.computeAverageOrigin(allTunnels);
        cs_.setAverageOrigin(averageOrigin);

        Point averageVoronoiOrigin = new Point(0, 0, 0);
        for (Tunnels tunnels : tunnelsByFrame.values()) {
            averageVoronoiOrigin =
                    averageVoronoiOrigin.plus(tunnels.getVoronoiOrigin());
        }
        averageVoronoiOrigin = averageVoronoiOrigin.divide(
                tunnelsByFrame.size());
        cs_.setAverageVoronoiOrigin(averageVoronoiOrigin);

        Printer.println("All tunnels size: " + allTunnels.size());

        AverageSurface surface = null;
        if (cs_.doAverageSurfaceGlobal()
                && (!cs_.loadClusteringTree() || cs_.doApproximateClustering())) {
            Printer.println("Computing average surface for "
                    + allTunnels.size() + " tunnels.", Printer.NORMAL);

            surface = new AverageSurface(allTunnels, averageVoronoiOrigin, cs_);

            Printer.println("Average surface represented by "
                    + surface.size() + " points.", Printer.NORMAL);
        }

        List<Tunnel> tunnelsExactly; // tunnels to be clustered by MCUPGMA
        if (!cs_.doApproximateClustering() || allTunnels.size()
                <= cs_.getExactClusteringLimit()) {
            tunnelsExactly = allTunnels;
            allTunnels = new ArrayList<Tunnel>();
        } else {
            tunnelsExactly = new ArrayList<Tunnel>();
            if (null != cs_.getExactMinBottleneck()) {

                Printer.println("All tunnels with bottleneck radius at least "
                        + cs_.getExactMinBottleneck() + " will be used for "
                        + "training set.", Printer.NORMAL);

                List<Integer> is = new ArrayList<Integer>();
                for (int i = 0; i < allTunnels.size(); i++) {

                    if (cs_.getExactMinBottleneck()
                            <= allTunnels.get(i).getBottleneck().getR()) {
                        is.add(i);

                    }
                }
                Collections.sort(is);
                for (int i = is.size() - 1; 0 <= i; i--) {
                    tunnelsExactly.add(allTunnels.remove((int) is.get(i)));
                }

                Printer.println(tunnelsExactly.size() + " tunnels chosen for "
                        + "training set, " + allTunnels.size()
                        + " will be classified.", Printer.NORMAL);

            } else {
                if (null == cs_.getExactClusteringPool()) { // totally random
                    Printer.println(cs_.getExactClusteringLimit() + " training "
                            + "tunnels will be sampled randomly.", Printer.NORMAL);
                    while (tunnelsExactly.size() < cs_.getExactClusteringLimit()) {
                        int r = cs_.getRandom().nextInt(allTunnels.size());
                        tunnelsExactly.add(allTunnels.remove(r));
                    }
                } else if (cs_.getExactClusteringPool()
                        <= cs_.getExactClusteringLimit()) { // cheapest tunnels
                    Printer.println(cs_.getExactClusteringLimit()
                            + " cheapest tunnels will be used for "
                            + "training set.", Printer.NORMAL);
                    while (tunnelsExactly.size() < cs_.getExactClusteringLimit()) {
                        int i = 0;
                        tunnelsExactly.add(allTunnels.remove(i));
                    }
                } else { // choose randomly from cheaper tunnels
                    Printer.println(cs_.getExactClusteringLimit()
                            + " training tunnels will be randomly sampled from  "
                            + cs_.getExactClusteringPool()
                            + " cheapest tunnels out of " + allTunnels.size()
                            + " tunnels.",
                            Printer.NORMAL);
                    int pool = cs_.getExactClusteringPool();
                    while (tunnelsExactly.size() < cs_.getExactClusteringLimit()) {
                        int r = cs_.getRandom().nextInt(pool--);
                        tunnelsExactly.add(allTunnels.remove(r));
                    }
                }
            }
            // now in only tunnels for clustering by classifier are in allTunnel
        }

        Clustering clustering = new Clustering(averageVoronoiOrigin,
                tunnelsExactly, cs_);

        if (CalculationSettings.ClusteringMethod.CALCULATE_MATRIX
                == cs_.getClusteringMethod()) {

            Printer.println("Saving matrix of pairwise tunnel similarities.",
                    Printer.NORMAL);
            LayeredTunnels lts = new LayeredTunnels(averageVoronoiOrigin,
                    surface, allTunnels, // just tunnels for exact clutering
                    true, cs_.doAverageSurfaceGlobal(),
                    cs_.getGlobalLayersSettings(), cs_);
            clustering.computeMatrix(lts, cs_.getMatrixFile());
            return null;
        }

        Printer.println("Going to cluster " + tunnelsExactly.size()
                + " tunnels.");
        Clusters clusters = null;

        if (CalculationSettings.ClusteringMethod.AVERAGE_LINK
                == cs_.getClusteringMethod()) {

            if (!cs_.loadTrainingSet()) {

                Printer.println("Using internal average link clustering");

                if (0 == tunnelsExactly.size()) {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "No tunnels to cluster. Run launch.py to"
                            + " check starting point position or try "
                            + "decreasing probe radius.");
                    clusters = new Clusters(cs_);
                } else if (1 == tunnelsExactly.size()) {
                    clusters = new Clusters(cs_);
                    clusters.addTunnel(ClusterId.create(1),
                            tunnelsExactly.get(0), false);
                } else {

                    if (!cs_.loadClusteringTree()) {
                        Printer.println("Computing distance matrix using "
                                + "weighting_coefficient "
                                + cs_.getGlobalLayersSettings().
                                getWeightingCoefficient() + ".",
                                Printer.NORMAL);



                        LayeredTunnels lts = new LayeredTunnels(
                                averageVoronoiOrigin, surface,
                                tunnelsExactly,
                                // just tunnels for exact clutering
                                true, cs_.doAverageSurfaceGlobal(),
                                cs_.getGlobalLayersSettings(), cs_);


                        cs_.getMatrixFile().delete();

                        clustering.computeMatrix(lts, cs_.getMatrixFile());

                        Printer.println("Performing average link hierarchical "
                                + "clustering to create " + cs_.getTreeFile()
                                + ".", Printer.NORMAL);

                        cs_.getClusteringTemproraryFile().delete();
                        cs_.getTreeFile().delete();

                        clustering.memoryConstrainedAverageLink(
                                cs_.getMatrixFile(),
                                cs_.getClusteringTemproraryFile(),
                                cs_.getTreeFile());
                        cs_.getClusteringTemproraryFile().delete();
                        cs_.getMatrixFile().delete();
                        if (cs_.stopAfterClusterTree()) {
                            return null;
                        }
                    } else {
                        Printer.println("Loading old cluster tree from "
                                + cs_.getTreeFile(), Printer.NORMAL);
                        if (!cs_.getTreeFile().exists()) {
                            // user wants to load but provides no file
                            String msg = cs_.getTreeFile() + " missing,"
                                    + " set 'load_cluster_tree' to 'no'";
                            Printer.suggests(msg);
                            throw new CalculationException(msg);
                        }
                    }



                    LayeredTunnels lts = new LayeredTunnels(
                            averageVoronoiOrigin, surface,
                            tunnelsExactly,
                            true, cs_.doAverageSurfaceGlobal(),
                            cs_.getGlobalLayersSettings(), cs_);

                    clusters = clustering.loadTree(cs_.getTreeFile(),
                            cs_.getClusteringThreshold(),
                            lts);

                    Printer.println(clustering.getTunnelNumber()
                            + " tunnels clustered into " + clusters.size()
                            + " clusters using clustering_threshold "
                            + cs_.getClusteringThreshold() + ".\n",
                            Printer.NORMAL);

                }
            }
        } else {
            throw new RuntimeException("Unknown clustering method (parameter"
                    + " clustering in config.txt).");
        }

        if (cs_.doApproximateClustering() && !cs_.loadTrainingSet()) {
            Printer.println("Transforming each of the sampled "
                    + tunnelsExactly.size()
                    + " tunnels to "
                    + cs_.getLayersCount() + " representative points in"
                    + " order to create training data...",
                    Printer.NORMAL);
            LayeredTunnels lts = new LayeredTunnels(averageVoronoiOrigin,
                    surface, tunnelsExactly, // just tunnels for exact clutering
                    true, cs_.doAverageSurfaceGlobal(),
                    cs_.getGlobalLayersSettings(), cs_);
            clustering.saveTrainingData(clusters, lts,
                    cs_.getTrainingFile(), snaps);
        }

        if (cs_.doApproximateClustering() && !allTunnels.isEmpty()) {

            List<Tunnel> tunnelsToClassify;
            if (cs_.loadTrainingSet()) {
                tunnelsToClassify = new ArrayList<Tunnel>();
                tunnelsToClassify.addAll(allTunnels);
                tunnelsToClassify.addAll(tunnelsExactly);
            } else {
                tunnelsToClassify = allTunnels;
            }

            Printer.println(tunnelsExactly.size() + " tunnels clustered exactly "
                    + "into " + clusters.size() + " clusters, "
                    + allTunnels.size() + " tunnels will be "
                    + "added using " + cs_.getClassifier() + " classifier.",
                    Printer.NORMAL);

            Printer.println("Transforming each yet unclustered tunnel to "
                    + cs_.getLayersCount() + " representative points...",
                    Printer.NORMAL);

            LayeredTunnels lts = new LayeredTunnels(averageVoronoiOrigin,
                    surface, tunnelsToClassify, // just tunnels for classification
                    false, cs_.doAverageSurfaceGlobal(),
                    cs_.getGlobalLayersSettings(), cs_);
            Clock.start("classify");
            Printer.println("Classifying...", Printer.NORMAL);
            clusters = clustering.classify(tunnelsToClassify, lts, clusters);
            Clock.stop("classify");

            Printer.println(allTunnels.size() + " tunnels were added.",
                    Printer.IMPORTANT);

            clustering = null;
            allTunnels = null;
            tunnelsExactly = null;
        }

        if (null == clusters) { // failure
            clusters = new Clusters(cs_);
            // null indicates user interuption of computation,
            // this object with zero clusters indicates unexpected failure
        }

        Printer.println(clusters.getTunnels().size() + " tunnels out of "
                + tunnelCount + " were clustered.");
        return clusters;

    }

    public void postprocess(SortedMap<SnapId, File> files, Clusters clusters)
            throws IOException, CalculationException {

        SortedSet<SnapId> snaps = (SortedSet<SnapId>) files.keySet();
        Printer.println("Clusters contains " + clusters.getTunnels().size()
                + " tunnels.");

        if (cs_.isCheapestTunnelInSnapshot()) {
            clusters.leaveCheapestTunnelPerSnapshot();
            Printer.println("Leaving only the cheapest tunnel per cluster "
                    + "per snapshot - " + clusters.getTunnels().size()
                    + " tunnels kept.", Printer.NORMAL);
        } else if (cs_.isRandomTunnelInSnapshot()) {

            clusters.leaveRandomTunnelPerSnapshot(cs_.getRandom());
            Printer.println("Leaving only one random tunnel per cluster "
                    + "per snapshot - " + clusters.getTunnels().size()
                    + " tunnels kept.", Printer.NORMAL);
        }

        before = clusters.size();
        clusters.leaveBest(cs_.getMaxOutputClusters(),
                cs_.getThroughputBestFraction(),
                (SortedSet<SnapId>) files.keySet());
        after = clusters.size();

        clusters.computeTunnelPriorities();

        Printer.println("Snapshots: " + snaps.size());

        Printer.println("Statistical analysis of results started.", Printer.NORMAL);

        clusters.calculateStatistics(snaps,
                cs_.getThroughputBestFraction());
        clusters.computePriorities(); // to set relevance of each cluster

    }

    public void output(SortedMap<SnapId, File> files, Clusters clusters)
            throws IOException, CalculationException {

        cleanDirectories();

        Printer.println("Statistics saving...");
        saveStatistics(clusters, cs_.getFrameToStart(),
                cs_.getAverageOrigin(), cs_.getAverageVoronoiOrigin(), files);


        Printer.println("Statistics save."); // Visualization

        Printer.println("Visualizations started.", Printer.NORMAL);
        Clock.start("saving visualization");
        saveVisualization(
                cs_.getTunnelsByFrame(), clusters, cs_.getFrameToStart());
        Clock.stop("saving visualization");
        Printer.println("Visualizations finished.", Printer.NORMAL);

    }

    private void subsample(Clusters clusters) {
        int removed = 0;
        if (cs_.getVisualizationSubsampling()
                == CalculationSettings.Sampling.RANDOM) {
            Printer.println("Cluster subsampling for visualization: random.",
                    Printer.IMPORTANT);
            removed = clusters.subsampleRandom(
                    cs_.getMaxVisualizableTunnelsPerCluster(), cs_.getRandom());

        } else if (cs_.getVisualizationSubsampling()
                == CalculationSettings.Sampling.CHEAPEST) {
            Printer.println("Cluster subsampling for visualization: cheapest.",
                    Printer.IMPORTANT);
            removed = clusters.subsampleCheapest(
                    cs_.getMaxVisualizableTunnelsPerCluster());
        } else {
            Printer.println("Cluster subsampling for visualization: none.",
                    Printer.IMPORTANT);
        }
        if (0 < removed) {
            Printer.println("For visualization, " + removed + " tunnels were "
                    + " removed from too big clusters, resulting in "
                    + clusters.getTunnels().size() + " tunnels.",
                    Printer.NORMAL);
        }
    }

    public void saveVisualization(SortedMap<SnapId, Tunnels> tunnels,
            Clusters clusters, SortedMap<SnapId, Point> frameToStart)
            throws IOException {

        cleanVisualizationDirs();

        Visualization visualization = new Visualization();
        visualization.originsToPdb(tunnels, false, cs_.getOriginsPdb());
        visualization.originsToPdb(tunnels, true, cs_.getVoronoiOriginsPdb());

        if (cs_.saveDynamicsVisualization()) {
            visualization.clustersToPdbDynamical(clusters, cs_, frameToStart);
        }
        subsample(clusters);
        visualization.clustersToPdbTimeless(clusters, cs_, frameToStart);

        cs_.setLoadTrajectory(false);
        cs_.prepareVisualizationScript(
                cs_.getPymolScriptTemplate(),
                cs_.getPymolScript());

        cs_.setLoadTrajectory(true);
        cs_.prepareVisualizationScript(
                cs_.getPymolScriptTemplate(),
                cs_.getPymolTrajectoryScript());
        cs_.setLoadTrajectory(false);

        cs_.prepareVisualizationScript(
                cs_.getVisualizationTimelessScriptTemplate(),
                cs_.getPymolTimelessScript());

        cs_.prepareVisualizationScript(
                cs_.getPymolPluginScriptTemplate(),
                cs_.getPymolPluginScript());

        cs_.prepareVisualizationScript(
                cs_.getVmdScriptTemplate(),
                cs_.getVmdScript());


        cs_.prepareVisualizationScript(
                cs_.getVmdScriptTemplate(),
                cs_.getVmdScript());

        cs_.prepareVisualizationScript(
                cs_.getVmdTimelessScriptTemplate(),
                cs_.getVmdTimelessScript());

        cs_.prepareVisualizationScript(
                cs_.getVmdLoadStructuresTemplate(),
                cs_.getVmdLoadStructures());

        cs_.prepareVisualizationScript(
                cs_.getVmdLoadStructureTemplate(),
                cs_.getVmdLoadStructure());

        cs_.prepareVisualizationScript(
                cs_.getVmdRadiiScriptTemplate(),
                cs_.getVmdRadiiScript());

        if (cs_.getPointsScriptTemplate().exists()) {
            cs_.prepareVisualizationScript(
                    cs_.getPointsScriptTemplate(),
                    cs_.getPointsScript());
        }

        cs_.prepareVisualizationScript(
                cs_.getZonesScriptTemplate(),
                cs_.getZonesScript());

        cs_.prepareBatch(cs_.getVmdBatTemplate(),
                cs_.getVmdBat());
        cs_.getVmdBat().setExecutable(true);
        cs_.prepareBatch(cs_.getVmdTimelessBatTemplate(),
                cs_.getVmdTimelessBat());
        cs_.getVmdTimelessBat().setExecutable(true);

        cs_.prepareBatch(cs_.getVmdShTemplate(),
                cs_.getVmdSh());
        cs_.getVmdSh().setExecutable(true);
        cs_.prepareBatch(cs_.getVmdTimelessShTemplate(),
                cs_.getVmdTimelessSh());
        cs_.getVmdTimelessSh().setExecutable(true);

        HotSpotWizardMyUtil.copyFile(cs_.getPymolRgbTemplate(),
                cs_.getPymolRgb());
        if (cs_.getVmdRgbTemplate().exists()) {
            HotSpotWizardMyUtil.copyFile(cs_.getVmdRgbTemplate(),
                    cs_.getVmdRgb());
        }

        if (cs_.isAdmin()) {

            HotSpotWizardMyUtil.copyFile(cs_.getVoidScriptTemplate(),
                    cs_.getVoidScript());
        }

    }

    public void saveStatistics(Clusters clusters,
            SortedMap<SnapId, Point> frameToStart,
            Point averageOrigin, Point averageVoronoiOrigin,
            SortedMap<SnapId, File> files) throws IOException {


        SortedSet<SnapId> snaps = (SortedSet<SnapId>) files.keySet();

        Statistics s = new Statistics(cs_);


        if (cs_.computeErrors()) {
            Clock.start("error bound");
            s.computeRadiusErrorBound(clusters);
            Clock.stop("error bound");
        }


        if (cs_.generateSummary()) {
            Clock.start("statistics clusters");
            Printer.println("Saving summary information.", Printer.NORMAL);
            s.saveSummary(clusters, cs_.getSummaryFile());

            if (cs_.savePreciseSummary()) {
                s.savePreciseSummary(clusters, cs_.getPreciseSummaryFile());
            }

            Clock.stop("statistics clusters");
        }
        if (cs_.generateProfileHeatMaps() && !clusters.isEmpty()) {
            Clock.start("statistics profile heat maps");
            Printer.println("Saving profile heat maps.", Printer.NORMAL);

            s.saveProfileHeatMaps(snaps, clusters, averageOrigin,
                    averageVoronoiOrigin);
            Clock.stop("statistics profile heat maps");
        }

        if (cs_.generateBottleneckHeatMap() && !clusters.isEmpty()) {
            Clock.start("statistics bottleneck heat maps");
            Printer.println("Saving bottleneck heat maps.", Printer.NORMAL);

            s.saveBottleneckHeatMap(snaps, clusters, averageOrigin,
                    averageVoronoiOrigin);
            Clock.stop("statistics bottleneck heat maps");
        }

        //Clock.start("statistics profiles");
        /*
         * Printer.println("Saving tunnel profiles.", Printer.NORMAL);
         * s.saveTunnelProfiles(clusters); Clock.stop("statistics profiles");
         */
        if (cs_.generateTunnelCharacteristics()) {
            Clock.start("tunnel characteristics");
            Printer.println("Saving tunnel characteristics.", Printer.NORMAL);
            s.saveTunnelCharacteristics(clusters);
            Clock.stop("tunnel characteristics");
        }
        if (cs_.computeTunnelResidues()) {
            Clock.start("statistics tunnel-lining atoms");
            Printer.println("Saving tunnel-lining residues and atoms.",
                    Printer.NORMAL);
            s.saveResidueClusterTouches(clusters);
            Clock.stop("statistics tunnel-lining atoms");
        }
        if (cs_.computeBottleneckResidues()) {
            Printer.println("Computing bottleneck residues...");
            Printer.println("Saving bottlenecks.", Printer.NORMAL);
            s.saveBottleneckResidues(clusters);
            Printer.println("Bottleneck residues computed.");
        }

        if (cs_.generateTunnelProfiles()) {
            Clock.start("tunnel profiles");
            Printer.println("Saving tunnel profiles.", Printer.NORMAL);
            s.saveTunnelProfiles(clusters, averageOrigin);
            Clock.stop("tunnel profiles");
        }

        if (cs_.experimental()) {
            Clock.stop("throughput statistics");
            Printer.println("Saving throughput statistics.", Printer.NORMAL);
            s.saveThroughputStatistics(clusters);
            Clock.stop("throughput statistics");
        }

        if (cs_.generateHistrograms()) {
            Clock.start("histograms");
            Printer.println("Saving histograms.", Printer.NORMAL);
            s.saveThroughputHistograms(clusters);
            s.saveBottleneckHistograms(clusters);
            Clock.stop("histograms");
        }

        Printer.println("Statistical analysis finished.", Printer.NORMAL);
    }

    private void cleanDirectories() {
        cs_.cleanDirectory(cs_.getClusterPdbDir());
        cs_.cleanDirectory(cs_.getClusterTimelessDir());
    }

    private void cleanVisualizationDirs() {
        cs_.cleanDirectory(cs_.getClusterRadiiDir());
    }

    private boolean areSpheresNonGeneral(Collection<Sphere> spheres) {
        Set<Double> ds = new HashSet<Double>();
        for (Sphere s : spheres) {
            for (double d : s.getS().getCoordinates()) {
                if (ds.contains(d)) {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "areSpheresNonGeneral {0}", d);
                    return false;
                } else {
                    ds.add(d);
                }
            }
        }
        return true;
    }

    public List<Sphere> shatter(List<Sphere> spheres) {

        Logger.getLogger("caver").log(Level.FINE, "Shattering {0} spheres.",
                spheres.size());

        double shatter = 0;
        long count = 0;

        List<Sphere> ss = new ArrayList<Sphere>(); // shattered spheres

        for (Sphere s : spheres) {

            Point p = Point.createShattered(
                    s.getS(), cs_.getMaxShatter(), cs_.getRandom());

            ss.add(new Sphere(p, s.getR()));

            shatter += p.distance(s.getS());
            count++;
        }

        Printer.println("Average shatter distance " + (shatter / count));

        assert areSpheresNonGeneral(ss) : "Spheres are not in general position.";

        return ss;
    }

    /*
     * Solves non-general positions by ranodm shattering of coordinates.
     */
    public VoronoiDiagram constructVoronoiDiagram(List<Sphere> spheres) {

        SortedMap<Integer, NumberedSphere> numbered =
                new TreeMap<Integer, NumberedSphere>();
        for (int i = 0; i < spheres.size(); i++) {
            Sphere s = spheres.get(i);
            numbered.put(i, new NumberedSphere(
                    i, s.getS(), s.getR()));
        }

        SphereSpaceTriangulator pt = new SphereSpaceTriangulator(cs_);
        VoronoiDiagram vd = pt.triangulate(numbered);

        Printer.println("Spheres in approximated system: " + spheres.size());
        Printer.println("Vertices in Voronoi diagram: " + vd.size());
        return vd;
    }

    /*
     * Method for estimation of optimal bounding box of a molecule. Can help to
     * think about a radius of a tunnel if the structure of transported molecule
     * is known.
     */
    public void findBoundingBox() {
        BoundingBox bb = new BoundingBox(cs_.getBoundingBoxN());
        File file = cs_.getBoundingBoxFile();

        PdbFileProcessor pfp = cs_.getPdbFileProcessor(file);
        MolecularSystem ms = cs_.createMolecularSystem(pfp);

        bb.find(ms.getSpheres());
    }

    private void compute() throws IOException, CalculationException {

        Printer.listenOnce(Printer.IMPORTANT);
        Printer.println("Caver computation started.");

        if (cs_.findBoundingBox()) {
            findBoundingBox();
            return;
        }

        Clock.start("all");

        SortedMap<SnapId, File> files = cs_.getTrajectoryFiles(
                cs_.getTimeSparsity(), cs_.getFirstFrame(), cs_.getLastFrame());

        if (files.size() == 0) {
            Logger.getLogger("caver").log(Level.WARNING, "No PDB files found in {0} from {1}. snapshot to {2}. snapshot using every {3} snapshot.", new Object[]{cs_.getPdbDir(), cs_.getFirstFrame(), cs_.getLastFrame(), cs_.getTimeSparsity()});
            System.exit(1);
        }


        if (cs_.generateTrajectory()) {
            Printer.println("Merging all PDB files into one multimodel PDB file.",
                    Printer.NORMAL);
            mergePdbFiles(files);
        }

        Iterator<SnapId> it = files.keySet().iterator();
        for (int i = 0; i < files.size() / 2; i++) {
            it.next();
        }

        cs_.copyPdbRepresentant(files.get(it.next()));

        if (!cs_.loadTunnels()) {

            Clock.start("find tunnels");
            findTunnels(files);
            Clock.stop("find tunnels");
        }
        if (cs_.stopAfterTunnels()) {
            return;
        }

        Clock.start("cluster tunnels");
        Clusters clusters = cluster(files);
        if (cs_.stopAfterClusterTree()) {
            // also cluster(files) ended prematurely
            return;
        }
        Clock.stop("cluster tunnels");



        Clock.start("output analyses");
        if (null != clusters) {
            postprocess(files, clusters);
            output(files, clusters);
        }

        if (after < before) {
            Printer.println("Only characteristics and visualization of "
                    + after + " best clusters generated (out of "
                    + before + " clusters).",
                    Printer.NORMAL);
        }

        Clock.stop("output analyses");
        Clock.stop("all");

        BufferedWriter bw = new BufferedWriter(new FileWriter(cs_.getTimesFile()));
        Clock.print(bw);
        bw.close();


    }

    public void run() throws IOException, CalculationException {
        if (cs_.getLogFile().exists()) {
            cs_.getLogFile().delete();
        }
        Printer.setFile(cs_.getLogFile());
        Logger logger = Logger.getLogger("caver");
        while (logger.getHandlers().length > 0) {
            logger.removeHandler(logger.getHandlers()[0]);
        }
        logger.setLevel(Level.FINE);
        FileHandler handler = null;
        try {
            handler = new FileHandler(cs_.getWarningLog().getPath());
            handler.setLevel(Level.WARNING);
            Formatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);

            logger.addHandler(handler);
        } catch (IOException e) {
            System.err.println("Log file initialization failed: "
                    + e.getMessage());
        }

        Printer.println("Using installation data directory: " + cs_.getInstalationDirectory());
        Printer.println("Using PDB files from: " + cs_.getPdbDir());

        try {
            compute();
            Printer.saveSuggestions(cs_.getAdvicesFile());
        } catch (Exception e) {
            Logger.getLogger("caver").log(Level.SEVERE, "Unexpected.", e);
        }

        handler.flush();
        handler.close();

        if (cs_.getWarningLog().exists() && 0 < cs_.getWarningLog().length()) {

            Printer.listenOnce(Printer.IMPORTANT);
            Printer.println("Calculation finished with warnings, summary is in "
                    + cs_.getWarningLog() + " (size " + cs_.getWarningLog().length()
                    + ").");
        } else {
            Printer.println("\nFinished successfully.", Printer.IMPORTANT);
        }


    }

    private static void printHelp() {
        System.out.println("Parameters\n"
                + "-home installation directory\n"
                + "-pdb directory with PDB files\n"
                + "-conf configuration file\n"
                + "-out output directory\n"
                + "\n"
                + "If -conf is ommited, home/config.txt will be used.\n"
                + "If -out is ommited, home/out will be used.");

    }

    private static boolean contains(String what, String[] where) {
        for (String s : where) {
            if (s.trim().equals(what.trim())) {
                return true;
            }
        }
        return false;
    }

    private int deriveNumber(String s) {
        int i = 0;
        for (char c : s.toCharArray()) {
            i += c;
        }
        return i;
    }

    public static void main(String[] args)
            throws IOException, CalculationException {

        File homeDir = null;
        File pdbDir = null;
        File configurationFile = null;
        File outputDir = null;
        CalculationSettings cs = new CalculationSettings();
        if (cs.getLogFile().exists()) {
            cs.getLogFile().delete();
        }
        Printer.setFile(cs.getLogFile());
        try {
            if (!contains("-home", args)) {
                printHelp();
                throw new InvalidArgumentsException("\nParameter -home is missing.");
            }
            if (!contains("-pdb", args)) {
                printHelp();
                throw new InvalidArgumentsException("\nParameter -pdb is missing.");
            }
            if (args.length < 4) {
                printHelp();
                throw new InvalidArgumentsException(args.length + " provided, 4 "
                        + "were expected.");
            }
            for (int i = 0; i < args.length / 2; i++) {
                if ("-home".equals(args[i * 2])) {
                    homeDir = new File(args[i * 2 + 1].trim());
                } else if ("-pdb".equals(args[i * 2])) {
                    pdbDir = new File(args[i * 2 + 1].trim());
                } else if ("-conf".equals(args[i * 2])) {
                    configurationFile = new File(args[i * 2 + 1].trim());
                } else if ("-out".equals(args[i * 2])) {
                    outputDir = new File(args[i * 2 + 1].trim());
                }
            }
            if (null == configurationFile) {
                configurationFile = new File(
                        homeDir + File.separator + "config.txt");
            }
            if (null == outputDir) {
                outputDir = new File(
                        homeDir + File.separator + "out");
            }

            cs.setBinDirectory(
                    new File(homeDir + File.separator + "bin"));
            cs.setPdbDir(pdbDir);
            cs.setConfigFiles(configurationFile,
                    new File(configurationFile.getAbsolutePath() + ".adv"));
            cs.setOutputDirectory(outputDir);
            cs.readFile();

        } catch (Exception e) {
            e.printStackTrace();
            printHelp();
            throw new RuntimeException(e);
        }

        Launcher run = new Launcher(cs);
        run.run();


    }
}
