package caver;

import algorithms.clustering.layers.LayeredTunnel;
import algorithms.clustering.statistics.Histogram;
import algorithms.search.CostFunction;
import algorithms.search.TimeCostFunction;
import caver.tunnels.Tunnels;
import caver.ui.SettingsException;
import caver.util.FileOperations;
import caver.util.HotSpotWizardMyUtil;
import caver.util.PdbFileWriter;
import chemistry.*;
import chemistry.pdb.*;
import chemistry.pdb.exceptions.AtomNotFoundException;
import chemistry.pdb.exceptions.ResidueNotFoundException;
import geometry.primitives.Point;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class centralizing all calculation parameters, directories and files.
 */
public class CalculationSettings { // Calculation Settings

    private AdvancedCalculationSettings acs_;
    private boolean experimental = false; // really experimental features
    private double clusteringThreshold = 3.5;
    private boolean frameClustering = true;
    private File cf_;
    private File acf_;
    private File clusterFile;
    private File detailsTemplateFile;
    private File pdbDir_;
    private File installationDir_;
    private File outDir_;
    private File pymolDir_;
    private File vmdDir_;
    private File vmdScriptsDir_;
    private File analysisDir_; // derived statistics, except summary.txt file
    private File profileHeatMapsDir_;
    private File profileAverageImagesDir_;
    private File profileAverageCsvDir_;
    private File profileCsvDir_;
    private File bottleneckHeatMapsDir_;
    private File bottleneckCsvDir_;
    private File dataDir_;
    private File histogramsDir_;
    private File edgesDir_;
    private Set<Integer> startingAtoms_ = new TreeSet<Integer>();
    private Set<String> startingResidues_ = new TreeSet<String>();
    private Point startingPoint_ = null;
    private int maxNumberOfTunnels = 10000;
    private boolean loadTunnels = false;
    private boolean loadClusterTree = false;
    private double shellRadius = 3.0;
    private double shellDepth = 4.0;
    private double probeRadius = 0.9;
    private int maxOutputClusters = 999;
    private int maxVisualizableTunnelsPerCluster = 5000;
    private Integer seed_ = null;
    private boolean cheapestTunnelInSnapshot = true;
    private boolean randomTunnelInSnapshot = false;
    private Sampling visualizationSubsampling = Sampling.RANDOM;
    private boolean computeTunnelResidues = false;
    private int optimizationSampleSize = 100;
    private double contactDistance = 3.0;
    private double bottleneckContactDistance = 3.0;
    private double inflatingRadius = 5;
    private boolean inflateActivated = false;
    private File externalTriangulationPath = null;
    private boolean oneRadiusApproximation = true;
    private int timeSparsity = 1;
    private int firstFrame = 1;
    private int lastFrame = 100000;
    private double maxShatter = 0.001;
    private double dissimilarRadiusTolerance = 1.01;
    private double minDissimilarRadiusTolerance = 1.01;
    private Random random;
    private double profileTunnelSamplingStep = 0.5;
    private double visualizationTunnelSamplingStep = 1;
    private boolean saveDynamicsVisualization = false;
    private File pdbRepresentant;
    private CostFunction passingFunction = null;
    private double costFunctionExponent = 2;
    private double maxCostFunctionExponent = 100;
    private boolean generateVoronoi = false;
    private double desiredRadius = 5;
    private double defaultMaxDistance = 3;
    private double maxDistance = defaultMaxDistance;
    private double maxLimitingRadius = 100;
    private boolean saveApproximation = false;
    private boolean automaticShellRadius = false;
    private double bottleneckMultiplier = 2;
    private int balls = 12;
    private boolean addCentralSphere = false;
    private int hierarchicalClusteringLimit = 20000;
    private int minTrainingTunnels = 1;
    private int layersCount = 10;
    private boolean doApproximateClustering = false;
    private LayeredTunnel.SpaceTransformation spaceTransformation =
            LayeredTunnel.SpaceTransformation.linear;
    private LayeredTunnel.SpaceTransformation frameSpaceTransformation =
            LayeredTunnel.SpaceTransformation.linear;
    private double frameClusteringThreshold = 1;
    private String classifier = "knn";
    private int maxTrainingClusters = 15;
    private boolean generateUnclassifiedCluster = false;
    private double originProtectionRadius = 4;
    private int waypointCount = 0;
    private double lengthImportance = 1;
    private boolean admin = false;
    private boolean computeBottleneckResidues = false;
    private double correctionShift = 0.3;
    private boolean correctApproximation = true;
    private int correctionGridSize = 20;
    private Set<String> excludeResiduesNamed = new HashSet<String>();
    private Set<String> includeResiduesNamed = new HashSet<String>();
    private Set<ResidueStringRange> excludeResiduesIded = new HashSet<ResidueStringRange>();
    private Set<ResidueStringRange> includeResiduesIded = new HashSet<ResidueStringRange>();
    private Set<AtomRange> excludeAtomsNumbered = new HashSet<AtomRange>();
    private Set<AtomRange> includeAtomsNumbered = new HashSet<AtomRange>();
    private boolean includeAll = false;
    private boolean otherIncludePresent = false;
    private String vmdPath = "\"C:/Program Files/University of Illinois/VMD/vmd.exe\"";
    private String vmdLinuxPath = "vmd";
    private boolean findBoundingBox = false;
    private int boundingBoxN = 1000;
    private File boundingBoxFile;
    private Histogram throughputHistogram = new Histogram(0, 1, 10);
    private Histogram bottleneckHistogram = new Histogram(0, 2, 20);
    private double throughputBestPercent = 0.1;
    private boolean generateTrajectory = false;
    private Double profileHeatMapResolution = null;
    private Double bottleneckHeatMapResolution = null;
    private int maxHeatMapWidth = 10000000;
    private double heatMapLow = 1;
    private double heatMapHigh = 2;
    private int heatMapElementX = 20;
    private int heatMapElementY = 10;
    private double bottleneckHeatMapLow = 1;
    private double bottleneckHeatMapHigh = 2;
    private int bottleneckHeatMapElementX = 10;
    private int bottleneckHeatMapElementY = 10;
    private boolean stopAfterTunnels = false;
    private boolean stopAfterClusterTree = false;
    private Point averageVoronoiOrigin_;
    private Point averageOrigin_;
    private SortedMap<SnapId, Tunnels> tunnelsByFrame_;
    private SortedMap<SnapId, Point> frameToStart_;
    private boolean generateProfileHeatMap = false;
    private boolean generateBottleneckHeatMap = false;
    private LayersSettings fls = // frame
            new LayersSettings(0.0, 0.0, 5.0, 1.0);
    private LayersSettings gls = // global
            new LayersSettings(2.0, 0.0, 5.0, 1.0);
    private boolean saveZones = true;
    private boolean loadTrajectory = false;
    private boolean checkVisibility = false;
    private double visibilityProbeRadius = 0.0;
    private boolean saveErrorProfiles = false;
    private boolean computeErrors = false;
    private boolean generateSummary = true;
    private boolean generateTunnelCharacteristics = true;
    private boolean generateTunnelProfiles = true;
    private boolean generateHistograms = false;
    private boolean averageSurfaceGlobal = true;
    private boolean averageSurfaceFrame = true;
    private double averageSurfaceSmoothnessAngle = 10;
    private double averageSurfaceSmoothness =
            angleToDistance(averageSurfaceSmoothnessAngle);
    private boolean saveSurfaceVisualization = false;
    private PdbFileWriter deadEndWriter;
    private PdbFileWriter deadStartWriter;
    private double averageSurfaceDensityAngle = 5;
    private double averageSurfaceDensity =
            angleToDistance(averageSurfaceDensityAngle);
    private PdbFileWriter layerWriter;
    private boolean producePoints = false;
    public boolean saveLayerPoints = false;
    private boolean saveBeforeFilter = false;
    private boolean saveAfterFilter = false;
    private double averageSurfaceTunnelSamplingStep = 0.5;
    private double averageSurfacePointMinDist = 0.1;
    private boolean swap = true;
    private boolean saveSwap = true;
    private boolean evaluateClassification = false;
    private Integer exactClusteringPool = null;
    private boolean loadTrainingSet = false;
    private boolean visualizeUnprocessedTunnels = false;
    private int visualizeMaxTunnels = 10000;
    private boolean checkDistance = false;
    private Double exactMinBottleneck = null;
    private double standardDeviationMultiplier = 3;
    private int clusterCharacterizationSample = 100;
    private int outlierCharacterizationSample = 100;
    private boolean savePreciseSummary = true;
    private double minCostFunctionRadius = 0.1;
    private boolean protect = true;
    private boolean checkClustering = false;
    private String seps = "\t ,;";
    private boolean doVoids = false;
    private int protectTunnelsIfMoreSnapshotsThan = 200;
    private double minTunnelLength = 1;
    private boolean noLayers = false;
    private double minSampling = 0.001;

    public enum ClusteringMethod {

        SINGLE_LINK, AVERAGE_LINK, CALCULATE_MATRIX, LOAD_TREE
    };

    public enum Sampling {

        RANDOM, CHEAPEST, NONE
    };
    private ClusteringMethod clusteringMethod = ClusteringMethod.AVERAGE_LINK;

    public Random getRandom() {
        return random;
    }

    // if seed_ was not initialized by config seed parameter,
    //  random is generated just once by Random()
    public final void initRandom(int seed) {

        if (null != seed_) {
            random = new Random(seed_ + seed);
        }
    }

    public final void initRandom() {

        if (null != seed_) {
            random = new Random(seed_);
        }
    }

    public int getSeed() {
        return seed_;
    }

    public static String fill(int n, int digits) {
        StringBuilder sb = new StringBuilder(Integer.toString(n));
        while (sb.length() < digits) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

    public CalculationSettings() {
    }

    public void setBinDirectory(File dir) {
        installationDir_ = dir;
    }

    public File getInstallationDirectory() {
        return installationDir_;
    }

    public void setPdbDirectory(File dir) {
        pdbDir_ = dir;
    }

    public void setConfigFiles(File f, File af) {
        cf_ = f;
        acf_ = af;
    }

    public void setOutputDirectory(File dir) {
        outDir_ = dir;
        analysisDir_ = new File(dir + File.separator + "analysis");
        histogramsDir_ = new File(analysisDir_ + File.separator + "histograms");
        dataDir_ = new File(dir + File.separator + "data");
        pymolDir_ = new File(outDir_ + File.separator + "pymol");
        vmdDir_ = new File(outDir_ + File.separator + "vmd");
        vmdScriptsDir_ = new File(vmdDir_ + File.separator + "scripts");
        profileHeatMapsDir_ = new File(analysisDir_ + File.separator + "profile_heat_maps");
        bottleneckHeatMapsDir_ = new File(analysisDir_ + File.separator + "bottleneck_heat_maps");
        bottleneckCsvDir_ = new File(analysisDir_ + File.separator + "bottleneck_heat_maps");

        profileAverageImagesDir_ = new File(profileHeatMapsDir_ + File.separator + "average_images");

        profileCsvDir_ = new File(profileHeatMapsDir_ + File.separator + "csv");
        profileAverageCsvDir_ = new File(profileCsvDir_ + File.separator + "average_csv");

        if (!outDir_.exists()) {
            outDir_.mkdir();
        }

        if (!analysisDir_.exists()) {
            analysisDir_.mkdir();
        }

        if (!dataDir_.exists()) {
            dataDir_.mkdir();
        }

        if (!pymolDir_.exists()) {
            pymolDir_.mkdir();
        }

        if (!vmdDir_.exists()) {
            vmdDir_.mkdir();
        }

        if (!vmdScriptsDir_.exists()) {
            vmdScriptsDir_.mkdir();
        }

        clusterFile = new File(dataDir_ + File.separator
                + "clusters.txt");

        if (!getClusterPdbDir().exists()) {
            getClusterPdbDir().mkdir();
        }

        if (!getTunnelsDir().exists()) {
            getTunnelsDir().mkdir();
        }

        edgesDir_ = new File(dataDir_ + File.separator + "tunnel_edges");

        if (!edgesDir_.exists()) {
            edgesDir_.mkdir();
        }

        if (!histogramsDir_.exists()) {
            histogramsDir_.mkdir();
        }

    }

    public void prepare(File dir) {
        if (dir.exists()) {
            FileOperations.deleteDirectory(dir);
        }
        dir.mkdir();
    }

    private void createParameterDependentDirectories() {
        if (generateProfileHeatMap) {
            prepare(profileHeatMapsDir_);
            prepare(profileAverageImagesDir_);
            prepare(profileCsvDir_);
            prepare(profileAverageCsvDir_);
        }
        if (generateBottleneckHeatMap) {
            prepare(bottleneckHeatMapsDir_);
            prepare(bottleneckCsvDir_);
        }
    }

    public void setPdbDir(File dir) {
        this.pdbDir_ = dir;
    }

    private boolean enabled(String value) throws SettingsException {
        if (value.trim().toLowerCase().equals("yes")) {
            return true;
        } else if (value.trim().toLowerCase().equals("no")) {
            return false;
        } else {
            throw new SettingsException();
        }
    }

    private void aboveZero(double v) throws SettingsException {
        if (v < 0) {
            throw new SettingsException();
        }
    }

    private void checkSampling(double v) throws SettingsException {
        if (v < minSampling) {
            throw new SettingsException();
        }
    }

    public final void readFile() throws IOException, SettingsException {
        BufferedReader br = new BufferedReader(new FileReader(getConfigFile()));
        String line;
        while (null != (line = br.readLine())) {
            if (line.trim().length() == 0 || line.trim().startsWith("#")) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line, " \t=");
            String name = st.nextToken();
            String rest = line.substring(name.length()).trim();

            name = name.trim();
            String value;
            if (st.hasMoreTokens()) {
                value = st.nextToken().trim();
            } else {
                value = "";
            }

            try {
                if ("time_sparsity".equals(name)) {
                    timeSparsity = Integer.parseInt(value);
                    if (timeSparsity < 1) {
                        timeSparsity = 1;
                    }
                } else if ("first_frame".equals(name)) {
                    firstFrame = Integer.parseInt(value);
                } else if ("last_frame".equals(name)) {
                    lastFrame = Integer.parseInt(value);
                } else if ("layers_count".equals(name)) {
                    layersCount = Integer.parseInt(value);
                } else if ("clustering_threshold".equals(name)) {
                    clusteringThreshold = Double.parseDouble(value);
                } else if ("starting_point_atom".equals(name)) {
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    while (ist.hasMoreTokens()) {
                        startingAtoms_.add(Integer.parseInt(
                                ist.nextToken().trim()));
                    }
                } else if ("starting_point_residue".equals(name)) {
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    while (ist.hasMoreTokens()) {
                        startingResidues_.add(ist.nextToken().trim());
                    }
                } else if ("starting_point_coordinates".equals(name)) {
                    double x = Double.parseDouble(value);
                    double y = Double.parseDouble(st.nextToken());
                    double z = Double.parseDouble(st.nextToken());
                    startingPoint_ = new Point(x, y, z);
                } else if ("max_number_of_tunnels".equals(name)) {
                    maxNumberOfTunnels = Integer.parseInt(value);
                } else if ("load_tunnels".equals(name)) {
                    loadTunnels = enabled(value);
                } else if ("load_cluster_tree".equals(name)) {
                    loadClusterTree = enabled(value);
                } else if ("shell_radius".equals(name)) {
                    shellRadius = Double.parseDouble(value);
                } else if ("shell_depth".equals(name)) {
                    shellDepth = Double.parseDouble(value);
                } else if ("probe_radius".equals(name)) {
                    probeRadius = Double.parseDouble(value);
                } else if ("max_output_clusters".equals(name)) {
                    maxOutputClusters = Integer.parseInt(value);
                } else if ("visualize_tunnels_per_cluster".equals(name)) {
                    maxVisualizableTunnelsPerCluster = Integer.parseInt(value);
                } else if ("visualization_subsampling".equals(name)) {
                    if ("random".equals(value)) {
                        visualizationSubsampling = Sampling.RANDOM;
                    } else if ("cheapest".equals(value)) {
                        visualizationSubsampling = Sampling.CHEAPEST;
                    } else if ("none".equals(value)) {
                        visualizationSubsampling = Sampling.NONE;
                    } else {
                        throw new SettingsException("Only values random, "
                                + "cheapest and none are allowed.");
                    }
                } else if ("one_tunnel_in_snapshot".equals(name)) {
                    if ("cheapest".equals(value) || "yes".equals(value)) {
                        cheapestTunnelInSnapshot = true;
                        randomTunnelInSnapshot = false;
                    } else if ("random".equals(value)) {
                        cheapestTunnelInSnapshot = false;
                        randomTunnelInSnapshot = true;
                    } else if ("no".equals(value)) {
                        cheapestTunnelInSnapshot = false;
                        randomTunnelInSnapshot = false;
                    } else {
                        throw new SettingsException();
                    }
                } else if ("seed".equals(name)) {
                    if (value.trim().equals("random")) {
                        seed_ = null;
                    } else {
                        seed_ = Integer.parseInt(value);
                        initRandom(seed_);
                    }
                } else if ("compute_tunnel_residues".equals(name)) {
                    computeTunnelResidues = enabled(value);
                } else if ("max_end_sphere_radius".equals(name)) {
                    Printer.error("Deprecated parameter "
                            + "max_end_sphere_radius ignored.");
                } else if ("residue_contact_distance".equals(name)) {
                    contactDistance = Double.parseDouble(value);
                } else if ("bottleneck_contact_distance".equals(name)) {
                    bottleneckContactDistance = Double.parseDouble(value);
                } else if ("clustering".equals(name)) {
                    if ("average_link".equals(value)) {
                        clusteringMethod = ClusteringMethod.AVERAGE_LINK;
                    } else if ("matrix".equals(value)) {
                        clusteringMethod = ClusteringMethod.CALCULATE_MATRIX;
                    } else if ("load".equals(value)) {
                        clusteringMethod = ClusteringMethod.LOAD_TREE;
                    } else {
                        throw new SettingsException();
                    }
                } else if ("inflating_activated".equals(name)) {
                    if ("yes".equals(value)) {
                        inflateActivated = enabled(value);
                    }
                } else if ("inflating_radius".equals(name)) {
                    this.inflatingRadius = new Double(value);
                } else if ("external_triangulation_path".equals(name)) {
                    externalTriangulationPath = new File(value);
                    if (!externalTriangulationPath.exists()) {
                        Printer.error("Warning: path to external "
                                + "triangulation does not exist:" + value);
                    }
                } else if ("one_radius_approximation".equals(name)) {
                    oneRadiusApproximation = enabled(value);
                } else if ("max_shatter".equals(name)) {
                    maxShatter = Double.parseDouble(value);
                } else if ("dissimilar_radius_tolerance".equals(name)) {
                    dissimilarRadiusTolerance = Double.parseDouble(value);
                    if (dissimilarRadiusTolerance < minDissimilarRadiusTolerance) {
                        Printer.warn("dissimilar_radius_tolerance increased "
                                + "from " + dissimilarRadiusTolerance + " to "
                                + dissimilarRadiusTolerance);
                        dissimilarRadiusTolerance = minDissimilarRadiusTolerance;
                    }
                } else if ("profile_tunnel_sampling_step".equals(name)) {
                    profileTunnelSamplingStep = Double.parseDouble(value);
                    checkSampling(profileTunnelSamplingStep);
                } else if ("visualization_tunnel_sampling_step".equals(name)) {
                    visualizationTunnelSamplingStep = Double.parseDouble(value);
                    checkSampling(visualizationTunnelSamplingStep);
                } else if ("save_dynamics_visualization".equals(name)) {
                    saveDynamicsVisualization = enabled(value);
                } else if ("cost_function_exponent".equals(name)) {
                    costFunctionExponent = Double.parseDouble(value);
                    if (maxCostFunctionExponent < costFunctionExponent) {
                        throw new SettingsException();
                    }
                    if (costFunctionExponent < 0) {
                        throw new SettingsException();
                    }
                } else if ("desired_radius".equals(name)) {
                    desiredRadius = Double.parseDouble(value);
                } else if ("max_distance".equals(name)) {
                    maxDistance = Double.parseDouble(value);
                } else if ("save_voronoi".equals(name)) {
                    generateVoronoi = enabled(value);
                } else if ("generate_summary".equals(name)) {
                    generateSummary = enabled(value);
                } else if ("generate_tunnel_characteristics".equals(name)) {
                    generateTunnelCharacteristics = enabled(value);
                } else if ("max_limiting_radius".equals(name)) {
                    maxLimitingRadius = Double.parseDouble(value);
                } else if ("save_approximation".equals(name)) {
                    saveApproximation = enabled(value);
                } else if ("frame_clustering".equals(name)) {
                    frameClustering = enabled(value);
                } else if ("automatic_shell_radius".equals(name)) {
                    automaticShellRadius = enabled(value);
                } else if ("automatic_shell_radius_bottleneck_multiplier".equals(name)) {
                    bottleneckMultiplier = Double.parseDouble(value);
                } else if ("number_of_approximating_balls".equals(name)) {
                    balls = Integer.parseInt(value);
                } else if ("add_central_sphere".equals(name)) {
                    addCentralSphere = enabled(value);
                } else if ("cluster_by_hierarchical_clustering".equals(name)) {
                    hierarchicalClusteringLimit = Integer.parseInt(value);
                } else if ("min_training_tunnels".equals(name)) {
                    minTrainingTunnels = Integer.parseInt(value);
                } else if ("do_approximate_clustering".equals(name)) {
                    doApproximateClustering = enabled(value);
                } else if ("space_transformation".equals(name)) {
                    if (value.startsWith("linear")) {
                        spaceTransformation = LayeredTunnel.SpaceTransformation.linear;
                    } else if (value.startsWith("exponential")) {
                        spaceTransformation = LayeredTunnel.SpaceTransformation.exponential;
                    } else {
                        throw new SettingsException();
                    }
                } else if ("weighting_coefficient".equals(name)) {

                    gls.setWeightingCoefficient(Double.parseDouble(value));
                } else if ("frame_space_transformation".equals(name)) {
                    if (value.startsWith("linear")) {
                        frameSpaceTransformation = LayeredTunnel.SpaceTransformation.linear;
                    } else if (value.startsWith("exponential")) {
                        frameSpaceTransformation = LayeredTunnel.SpaceTransformation.exponential;
                    } else {
                        throw new SettingsException();
                    }
                } else if ("frame_weighting_coefficient".equals(name)) {
                    fls.setWeightingCoefficient(Double.parseDouble(value));
                } else if ("frame_clustering_threshold".equals(name)) {
                    frameClusteringThreshold = Double.parseDouble(value);
                } else if ("classifier".equals(name)) {
                    classifier = value.trim();
                } else if ("max_training_clusters".equals(name)) {
                    maxTrainingClusters = Integer.parseInt(value);

                } else if ("generate_unclassified_cluster".equals(name)) {
                    generateUnclassifiedCluster = enabled(value);
                } else if ("starting_point_protection_radius".equals(name)) {
                    originProtectionRadius = Double.parseDouble(value);
                } else if ("length_importance".equals(name)) {
                    lengthImportance = Double.parseDouble(value);
                } else if ("waypoints".equals(name)) {
                    waypointCount = Integer.parseInt(value);
                } else if ("admin".equals(name)) {
                    admin = enabled(value);
                } else if ("compute_bottleneck_residues".equals(name)) {
                    computeBottleneckResidues = enabled(value);
                } else if ("exclude_residue_names".equals(name)) {
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    while (ist.hasMoreTokens()) {
                        String rn = ist.nextToken().trim();
                        List<String> names = expandResidueName(rn);
                        excludeResiduesNamed.addAll(names);
                    }
                } else if ("include_residue_names".equals(name)) {
                    otherIncludePresent = true;
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    while (ist.hasMoreTokens()) {
                        String rn = ist.nextToken().trim();
                        List<String> names = expandResidueName(rn);
                        includeResiduesNamed.addAll(names);
                    }
                } else if ("exclude_residue_ids".equals(name)) {
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    while (ist.hasMoreTokens()) {
                        String s = ist.nextToken().trim().toUpperCase();
                        ResidueStringRange r = getResidueRange(s);
                        excludeResiduesIded.add(r);
                    }
                } else if ("include_residue_ids".equals(name)) {
                    otherIncludePresent = true;
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    while (ist.hasMoreTokens()) {
                        String s = ist.nextToken().trim().toUpperCase();
                        ResidueStringRange r = getResidueRange(s);
                        includeResiduesIded.add(r);
                    }
                } else if ("exclude_atom_numbers".equals(name)) {
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    while (ist.hasMoreTokens()) {
                        String s = ist.nextToken().trim();
                        AtomRange r = getAtomRange(s);
                        excludeAtomsNumbered.add(r);
                    }
                } else if ("include_atom_numbers".equals(name)) {
                    otherIncludePresent = true;
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    while (ist.hasMoreTokens()) {
                        String s = ist.nextToken().trim();
                        AtomRange r = getAtomRange(s);
                        includeAtomsNumbered.add(r);
                    }
                } else if ("include".equals(name)) {
                    if (value.equals("*")) {
                        includeAll = true;
                    } else {
                        throw new SettingsException();
                    }
                } else if ("path_to_vmd".equals(name)) {
                    vmdPath = rest;
                    vmdLinuxPath = rest;
                } /*else if ("path_to_vmd_linux".equals(name)) {
                    vmdLinuxPath = rest;
                }*/ else if ("bounding_box_file".equals(name)) {
                    findBoundingBox = true;
                    boundingBoxFile = new File(rest);
                } else if ("bounding_box_n".equals(name)) {
                    boundingBoxN = Integer.parseInt(value);
                } else if ("bottleneck_histogram".equals(name)) {
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    double left = Double.parseDouble(ist.nextToken());
                    double right = Double.parseDouble(ist.nextToken());
                    int n = Integer.parseInt(ist.nextToken());
                    bottleneckHistogram = new Histogram(left, right, n);
                } else if ("throughput_histogram".equals(name)) {
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    double left = Double.parseDouble(ist.nextToken());
                    double right = Double.parseDouble(ist.nextToken());
                    int n = Integer.parseInt(ist.nextToken());
                    throughputHistogram = new Histogram(left, right, n);
                } else if ("throughput_best_fraction".equals(name)) {
                    throughputBestPercent = Double.parseDouble(value);
                } else if ("generate_trajectory".equals(name)) {
                    generateTrajectory = enabled(value);
                } else if ("generate_tunnel_profiles".equals(name)) {
                    generateTunnelProfiles = enabled(value);
                } else if ("generate_histograms".equals(name)) {
                    generateHistograms = enabled(value);
                } else if ("profile_heat_map_resolution".equals(name)) {
                    profileHeatMapResolution = Double.parseDouble(value);
                } else if ("bottleneck_heat_map_resolution".equals(name)) {
                    bottleneckHeatMapResolution = Double.parseDouble(value);



                } else if ("max_heat_map_width".equals(name)) {
                    maxHeatMapWidth = Integer.parseInt(value);
                } else if ("profile_heat_map_range".equals(name)) {
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    heatMapLow = Double.parseDouble(ist.nextToken());
                    heatMapHigh = Double.parseDouble(ist.nextToken());
                } else if ("profile_heat_map_element_size".equals(name)) {
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    heatMapElementX = Integer.parseInt(ist.nextToken());
                    heatMapElementY = Integer.parseInt(ist.nextToken());

                } else if ("bottleneck_heat_map_range".equals(name)) {
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    bottleneckHeatMapLow = Double.parseDouble(ist.nextToken());
                    bottleneckHeatMapHigh = Double.parseDouble(ist.nextToken());
                } else if ("bottleneck_heat_map_element_size".equals(name)) {
                    StringTokenizer ist = new StringTokenizer(rest, seps);
                    bottleneckHeatMapElementX = Integer.parseInt(ist.nextToken());
                    bottleneckHeatMapElementY = Integer.parseInt(ist.nextToken());
                } else if ("stop_after".equals(name)) {
                    if (value.startsWith("tunnels")) {
                        stopAfterTunnels = true;
                    } else if (value.startsWith("cluster_tree")) {
                        stopAfterClusterTree = true;
                    } else if (value.startsWith("never")) {
                        stopAfterTunnels = false;
                        stopAfterClusterTree = false;
                    } else {
                        throw new SettingsException();
                    }
                } else if ("generate_profile_heat_map".equals(name)) {
                    generateProfileHeatMap = enabled(value);
                } else if ("generate_bottleneck_heat_map".equals(name)) {
                    generateBottleneckHeatMap = enabled(value);
                } else if ("exclude_start_zone".equals(name)) {
                    gls.setExcludeStartZone(Double.parseDouble(value));
                } else if ("exclude_end_zone".equals(name)) {
                    gls.setExcludeEndZone(Double.parseDouble(value));
                } else if ("min_middle_zone".equals(name)) {
                    double d = Double.parseDouble(value);
                    if (d <= 0) {
                        throw new SettingsException("Value must be greater "
                                + "than zero.");
                    }
                    gls.setMinMiddleZone(d);
                } else if ("frame_exclude_start_zone".equals(name)) {
                    fls.setExcludeStartZone(Double.parseDouble(value));
                } else if ("frame_exclude_end_zone".equals(name)) {
                    fls.setExcludeEndZone(Double.parseDouble(value));
                } else if ("frame_min_middle_zone".equals(name)) {
                    double d = Double.parseDouble(value);
                    if (d <= 0) {
                        throw new SettingsException("Value must be greater "
                                + "than zero.");
                    }
                    fls.setMinMiddleZone(d);
                } else if ("average_surface_smoothness".equals(name)) {
                    averageSurfaceSmoothness = Double.parseDouble(value);
                } else if ("average_surface_smoothness_angle".equals(name)) {
                    double a = Double.parseDouble(value);
                    averageSurfaceSmoothness = angleToDistance(a);
                } else if ("average_surface_tunnel_sampling_step".equals(name)) {
                    averageSurfaceTunnelSamplingStep = Double.parseDouble(value);
                    aboveZero(averageSurfaceTunnelSamplingStep);
                } else if ("average_surface_point_min_dist".equals(name)) {
                    averageSurfacePointMinDist = Double.parseDouble(value);
                } else if ("average_surface_point_min_angle".equals(name)) {
                    double a = Double.parseDouble(value);
                    averageSurfacePointMinDist = angleToDistance(a);
                } else if ("save_average_surface".equals(name)) {
                    saveSurfaceVisualization = enabled(value);
                } else if ("check_visibility".equals(name)) {
                    checkVisibility = enabled(value);
                } else if ("visibility_probe_radius".equals(name)) {
                    visibilityProbeRadius = Double.parseDouble(value);
                } else if ("compute_errors".equals(name)) {
                    computeErrors = enabled(value);
                } else if ("save_error_profiles".equals(name)) {
                    saveErrorProfiles = enabled(value);
                } else if ("average_surface_global".equals(name)) {
                    averageSurfaceGlobal = enabled(value);
                } else if ("average_surface_frame".equals(name)) {
                    averageSurfaceFrame = enabled(value);
                } else if ("save_zones".equals(name)) {
                    saveZones = enabled(value);
                } else if ("average_surface_visualization_density".equals(name)) {
                    averageSurfaceDensity = Double.parseDouble(value);
                } else if ("save_before_filter".equals(name)) {
                    saveBeforeFilter = enabled(value);
                } else if ("save_after_filter".equals(name)) {
                    saveAfterFilter = enabled(value);
                } else if ("save_layer_points".equals(name)) {
                    saveLayerPoints = enabled(value);
                } else if ("swap".equals(name)) {
                    if ("yes".equals(value)) {
                        swap = true;
                        saveSwap = true;
                    } else if ("maybe".equals(value)) {
                        swap = false;
                        saveSwap = true;
                    } else {
                        swap = false;
                        saveSwap = false;
                    }
                } else if ("evaluate_classification".equals(name)) {
                    evaluateClassification = enabled(value);
                } else if ("exact_clustering_pool".equals(name)) {
                    if ("all".equals(value)) {
                        exactClusteringPool = null;
                    } else {
                        exactClusteringPool = Integer.parseInt(value);
                    }
                } else if ("load_training_set".equals(name)) {
                    loadTrainingSet = enabled(value);
                } else if ("visualize_unprocessed_tunnels".equals(name)) {
                    visualizeUnprocessedTunnels = enabled(value);
                } else if ("check_distance".equals(name)) {
                    checkDistance = enabled(value);
                } else if ("visualize_max_tunnels".equals(name)) {
                    if (value.trim().equals("all")) {
                        visualizeMaxTunnels = Integer.MAX_VALUE;
                    } else {
                        visualizeMaxTunnels = Integer.parseInt(value);
                    }
                } else if ("exact_min_bottleneck".equals(name)) {
                    exactMinBottleneck = Double.parseDouble(value);
                } else if ("standard_deviation_multiplier".equals(name)) {
                    standardDeviationMultiplier = Double.parseDouble(value);
                } else if ("cluster_characterization_sample".equals(name)) {
                    clusterCharacterizationSample = Integer.parseInt(value);
                } else if ("outlier_characterization_sample".equals(name)) {
                    outlierCharacterizationSample = Integer.parseInt(value);
                } else if ("save_precise_summary".equals(name)) {
                    savePreciseSummary = enabled(value);
                } else if ("min_cost_function_radius".equals(name)) {
                    minCostFunctionRadius = Double.parseDouble(value);
                } else if ("protect".equals(name)) {
                    protect = enabled(value);
                } else if ("check_clustering".equals(name)) {
                    checkClustering = enabled(value);
                } else if ("protect_tunnels_if_more_snapshots_than".equals(name)) {
                    protectTunnelsIfMoreSnapshotsThan = Integer.parseInt(value);
                } else if ("do_voids".equals(name)) {
                    doVoids = enabled(value);
                } else if ("min_tunnel_length".equals(name)) {
                    minTunnelLength = Double.parseDouble(value);
                } else {
                    Logger.getLogger("caver").log(Level.WARNING, "Unrecognized "
                            + "parameter " + "{0} in {1}", new Object[]{
                                name, getConfigFile()});
                }
            } catch (SettingsException e) {
                throw new SettingsException("Unsupported value '" + value
                        + "' for name '" + name + "' in " + getConfigFile()
                        + ". " + e.getMessage());
            }
        }
        br.close();

        if (null == seed_) {
            random = new Random();
            // TODO print seed, add parameter single seed to allow reproduction
            Printer.out("Using random seed.");
        }

        createParameterDependentDirectories();

        if (null == profileHeatMapResolution) {
            profileHeatMapResolution = getProfileTunnelSamplingStep();
        }

        if (null == bottleneckHeatMapResolution) {
            bottleneckHeatMapResolution = getProfileTunnelSamplingStep();
        }

        passingFunction = new TimeCostFunction(
                costFunctionExponent, maxLimitingRadius, getMinCostFunctionRadius());

        passingFunction.printGraph();
        if (passingFunction.overflow(0.1)) {
            Logger.getLogger("caver").log(Level.SEVERE, "Cost function "
                    + "overflow. Decrease max_limiting_radius or"
                    + " cost_function_exponent.");
        }

        acs_ = new AdvancedCalculationSettings(acf_);
    }

    private List<String> expandResidueName(String name) {
        String[] shortNames = {
            "Ala",
            "Cys",
            "Asp",
            "Glu",
            "Phe",
            "Gly",
            "His",
            "Ile",
            "Lys",
            "Leu",
            "Met",
            "Asn",
            "Pro",
            "Gln",
            "Arg",
            "Ser",
            "Thr",
            "Val",
            "Trp",
            "Tyr"
        };

        List<String> a = new ArrayList<String>();
        if ("20_AA".equals(name.toUpperCase())) {
            for (String s : shortNames) {
                a.add(s.toUpperCase());
            }
        } else {
            a.add(name.toUpperCase());
        }
        return a;
    }

    public final File getConfigFile() {
        return cf_;
    }

    public final File getPdbDir() {
        return pdbDir_;
    }

    public final File getInstalationDirectory() {
        return installationDir_;
    }

    public final File getOutputDirectory() {
        return outDir_;
    }

    public final File getTunnelsDir() {
        return new File(dataDir_ + File.separator + "tunnels");
    }

    public boolean isTunnelsDirEmpty() {
        return (0 == getTunnelsDir().listFiles().length);
    }

    public final File getVoronoiDiagramFile() {
        return new File(dataDir_ + File.separator + "voronoi.pdb");
    }

    public final File getClassifierFile() {
        return new File(dataDir_ + File.separator + "classifier.obj");
    }

    public final File getAtomsFile() {
        return new File(analysisDir_ + File.separator + "atoms.txt");
    }

    public final File getResidueFile() {
        return new File(analysisDir_ + File.separator + "residues.txt");
    }

    public final File getBottleneckResidueFile() {
        return new File(analysisDir_ + File.separator + "bottlenecks.csv");
    }

    public final File getSummaryFile() {
        return new File(getOutputDirectory().getPath() + File.separator
                + "summary.txt");
    }

    public final boolean savePreciseSummary() {
        return savePreciseSummary;
    }

    public final File getPreciseSummaryFile() {
        return new File(getOutputDirectory().getPath() + File.separator
                + "summary_precise_numbers.csv");
    }

    public final File getTunnelCharacteristicsFile() {
        return new File(analysisDir_ + File.separator + "tunnel_characteristics.csv");
    }

    public final File getTunnelProfilesFile() {
        return new File(analysisDir_ + File.separator + "tunnel_profiles.csv");
    }

    public final File getClusteringFile() {
        return clusterFile;
    }

    public final File getTunnelsFile(SnapId id) {
        return new File(getTunnelsDir() + File.separator + "tunnels_" + id + ".obj");
    }

    public final File getEdgesFile(SnapId id) {
        return new File(getEdgesDir() + File.separator + "edges_" + id + ".dat");
    }

    public final File getTunnelsVisualizationFile() {
        return new File(dataDir_ + File.separator + "tunnels.pdb");
    }

    public final File getClusterPdbDir() {
        File cdir = new File(dataDir_ + File.separator + "clusters");
        if (!cdir.exists()) {
            cdir.mkdir();
        }
        return cdir;
    }

    public final File getClusterRadiiDir() {
        File rdir = new File(dataDir_ + File.separator + "cluster_radii");
        if (!rdir.exists()) {
            rdir.mkdir();
        }
        return rdir;
    }

    public final File getClusterTimelessDir() {
        File cdir = new File(dataDir_ + File.separator + "clusters_timeless");
        if (!cdir.exists()) {
            cdir.mkdir();
        }
        return cdir;
    }

    public final File getClusterPdbFile(int clusterPriority) {
        String clusterId = fill(clusterPriority, 3);

        return new File(getClusterPdbDir() + File.separator + "tun_cl_"
                + clusterId + ".pdb");
    }

    public final File getClusterRadiiFile(int clusterPriority) {
        String clusterId = fill(clusterPriority, 3);

        return new File(getClusterRadiiDir() + File.separator + "tun_cl_"
                + clusterId + ".r");
    }

//    public final File getClusterPqrDir(int clusterPriority) {
//        String clusterId = fill(clusterPriority, 6);
//
//        return new File(getClusterPqrDir() + File.separator + "cluster_"
//                + clusterId);
//    }
//    public final File getClusterFramePqrFile(int clusterPriority, SnapId frame) {
//        String clusterId = fill(clusterPriority, 6);
//        String frameId = fill(frame.getNumber(), 9);
//
//        return new File(getClusterPqrDir(clusterPriority) + File.separator
//                + "cluster_" + clusterId + "_frame_" + frameId + ".pqr");
//    }
    public final File getTimelessClusterFile(String clusterId, int i) {
        return new File(getClusterTimelessDir() + File.separator
                + "tun_cl_" + clusterId + "_" + i + ".pdb");
    }

    public final File getClusteringTemproraryFile() {
        return new File(getClusterPdbDir() + File.separator + "cluster_temp.dat");
    }

    public final File getClusteringTemproraryFrameFile() {
        return new File(getClusterPdbDir() + File.separator + "cluster_temp_frame.dat");
    }

    public final File getDetailsTemplateFile() {
        return detailsTemplateFile;
    }

    public File getSnapshotTemplateFile() {
        return new File(getInstalationDirectory() + File.separator
                + "snapshot_template.py");
    }

    public File getVoidScriptTemplate() {
        return new File(getInstalationDirectory() + File.separator
                + "void_template.py");
    }

    public File getVoidScript() {
        return new File(dataDir_ + File.separator
                + "void.py");
    }

    public File getPointsScriptTemplate() {
        return new File(getInstalationDirectory() + File.separator
                + "points.py");
    }

    public File getPointsScript() {
        return new File(pymolDir_ + File.separator
                + "points.py");
    }

    public File getZonesScriptTemplate() {
        return new File(getInstalationDirectory() + File.separator
                + "zones.py");
    }

    public File getZonesScript() {
        return new File(pymolDir_ + File.separator
                + "zones.py");
    }

    public final double getClusteringThreshold() {
        return clusteringThreshold;
    }

    public final int getTimeSparsity() {
        return timeSparsity;
    }

    public final int getFirstFrame() {
        return firstFrame;
    }

    public final int getLastFrame() {
        return lastFrame;
    }

    public int getMaxNumberOfTunnels() {
        return maxNumberOfTunnels;
    }

    public File getTunnelComputationInfo() {
        return new File(dataDir_ + File.separator + "tunnel_computation.info");
    }

    public final boolean loadTunnels() {
        return loadTunnels;
    }

    public final boolean loadClusteringTree() {
        return loadClusterTree;
    }

    public final boolean stopAfterTunnels() {
        return stopAfterTunnels;
    }

    public final boolean stopAfterClusterTree() {
        return stopAfterClusterTree;
    }

    public double getProbeRadius() {
        return probeRadius;
    }

    public double getShellRadius() {
        return shellRadius;
    }

    public void setShellRadius(double d) {
        shellRadius = d;
    }

    public boolean automaticShellRadius() {
        return automaticShellRadius;
    }

    public double getShellDepth() {
        return shellDepth;
    }

    public int getMaxVisualizableTunnelsPerCluster() {
        return maxVisualizableTunnelsPerCluster;
    }

    public int getMaxOutputClusters() {
        return maxOutputClusters;
    }

    public File getAtomSelectionsScript() {
        return new File(pymolDir_ + File.separator + "atoms.py");
    }

    public SortedMap<SnapId, File> getTrajectoryFiles(
            int sparsity, int firstFrame, int lastFrame) {
        return PdbUtil.getTrajectoryFiles(
                getPdbDir(), sparsity, firstFrame, lastFrame);
    }

//    public boolean allClustersPdb() {
//        return allClustersPdb;
//    }
    public boolean isRandomTunnelInSnapshot() {
        return randomTunnelInSnapshot;
    }

    public boolean isCheapestTunnelInSnapshot() {
        return cheapestTunnelInSnapshot;
    }

//    public int getSeed() {
//        return seed_;
//    }
    public Sampling getVisualizationSubsampling() {
        return visualizationSubsampling;
    }

    public boolean computeTunnelResidues() {
        return computeTunnelResidues;
    }

    public int getOptimizationSampleSize() {
        return optimizationSampleSize;
    }

    public static double getDistanceFromStartingPointToIgnore() {
        return 2;
    }

    /*
     * Loads tunnel dynamics, but only protein representative snapshot.
     */
    public File getPymolScript() {
        return new File(pymolDir_ + File.separator + "view.py");
    }

    /*
     * Loads whole protein trajectory.
     */
    public File getPymolTrajectoryScript() {
        return new File(pymolDir_ + File.separator + "view_trajectory.py");
    }

    public File getVmdScript() {
        return new File(vmdScriptsDir_ + File.separator + "view.tcl");
    }

    public File getVmdRadiiScriptTemplate() {
        return new File(getInstallationDirectory() + File.separator + "radii.tcl");
    }

    public File getVmdRadiiScript() {
        return new File(vmdScriptsDir_ + File.separator + "radii.tcl");
    }

    public File getVmdTimelessScript() {
        return new File(vmdScriptsDir_ + File.separator + "view_timeless.tcl");
    }

    /*
     * VMD bat
     */
    public File getVmdBatTemplate() {
        return new File(getInstalationDirectory() + File.separator + "vmd.bat");
    }

    public File getVmdBat() {
        return new File(vmdDir_ + File.separator + "vmd.bat");
    }

    public File getVmdTimelessBatTemplate() {
        return new File(getInstalationDirectory() + File.separator
                + "vmd_timeless.bat");
    }

    public File getVmdTimelessBat() {
        return new File(vmdDir_ + File.separator + "vmd_timeless.bat");
    }

    /*
     * VMD sh
     */
    public File getVmdShTemplate() {
        return new File(getInstalationDirectory() + File.separator + "vmd.sh");
    }

    public File getVmdSh() {
        return new File(vmdDir_ + File.separator + "vmd.sh");
    }

    public File getVmdTimelessShTemplate() {
        return new File(getInstalationDirectory() + File.separator
                + "vmd_timeless.sh");
    }

    public File getVmdTimelessSh() {
        return new File(vmdDir_ + File.separator + "vmd_timeless.sh");
    }

    public File getPymolScriptTemplate() {
        return new File(getInstalationDirectory() + File.separator + "view.py");
    }

    public File getVmdScriptTemplate() {
        return new File(getInstalationDirectory() + File.separator + "view.tcl");
    }

    public File getVmdTimelessScriptTemplate() {
        return new File(getInstalationDirectory() + File.separator + "view_timeless.tcl");
    }

    public File getVmdLoadStructuresTemplate() {
        return new File(getInstalationDirectory() + File.separator + "vmd_load_structures.tcl");
    }

    public File getVmdLoadStructures() {
        return new File(vmdScriptsDir_ + File.separator + "vmd_load_structures.tcl");
    }

    public File getVmdLoadStructureTemplate() {
        return new File(getInstalationDirectory() + File.separator + "vmd_load_structure.tcl");
    }

    public File getVmdLoadStructure() {
        return new File(vmdScriptsDir_ + File.separator + "vmd_load_structure.tcl");
    }

    public File getPymolTimelessScript() {
        return new File(pymolDir_ + File.separator + "view_timeless.py");
    }

    public File getVisualizationTimelessScriptTemplate() {
        return new File(getInstalationDirectory() + File.separator + "view_timeless.py");
    }

    public File getPymolPluginScriptTemplate() {
        return new File(getInstalationDirectory() + File.separator + "view_plugin.py");
    }

    public File getPymolPluginScript() {
        return new File(pymolDir_ + File.separator + "view_plugin.py");
    }

    public File getPymolRgbTemplate() {
        return new File(getInstalationDirectory() + File.separator + "rgb.py");
    }

    public File getPymolRgb() {
        return new File(pymolDir_ + File.separator + "rgb.py");
    }

    public File getVmdRgbTemplate() {
        return new File(getInstalationDirectory() + File.separator + "rgb.tcl");
    }

    public File getVmdRgb() {
        return new File(vmdScriptsDir_ + File.separator + "rgb.tcl");
    }

    public double getContactDistance() {
        return contactDistance;
    }

    public double getBottleneckContactDistance() {
        return bottleneckContactDistance;
    }

    public ClusteringMethod getClusteringMethod() {
        return clusteringMethod;
    }

    public File getMatrixFile() {
        return new File(dataDir_ + File.separator + "matrix.edges");
    }

    public File getMatrixFrameFile() {
        return new File(dataDir_ + File.separator + "matrix_frame.edges");
    }

    public File getTreeFile() {
        return new File(dataDir_ + File.separator + "tree.txt");
    }

    public File getCorridorFile() {
        return new File(dataDir_ + File.separator + "dijkstra.pdb");
    }

    public File getVoidFilePrecursor() {
        return new File(dataDir_ + File.separator + "void_precursor.pdb");
    }

    public File getVoidFile() {
        return new File(dataDir_ + File.separator + "void.pdb");
    }

    public boolean inflatingActivated() {
        return this.inflateActivated;

    }

    public double getInflatingRadius() {
        return inflatingRadius;
    }

    public AtomRadii getAtomRadii() throws IOException {
        return new AtomRadii(
                new File(getInstalationDirectory() + File.separator + "radii.txt"));
    }

    public PdbFileProcessor getPdbFileProcessor(File pdbFile) {
        try {
            PdbFileFactory pff = new PdbFileFactory(getPeriodicTable());
            PdbFile pf = pff.create(pdbFile);
            PdbFileProcessor pfp = new PdbFileProcessor(pf);
            return pfp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Point createStartingPoint(PdbFileProcessor pfp) {

        if (null == startingPoint_
                && startingResidues_.isEmpty()
                && startingAtoms_.isEmpty()) {
            return null;
        }

        Point sum = new Point(0, 0, 0);
        int count = 0;

        for (String sri : startingResidues_) {
            try {
                Residue r = pfp.guessResidue(sri);
                Point s = r.getCenter();
                sum = sum.plus(s);
                count++;
            } catch (ResidueNotFoundException e) {
                Logger.getLogger("caver").log(Level.WARNING,
                        "Starting residue " + e.getResidueId() + " not found.",
                        e);
            }
        }

        for (int ai : startingAtoms_) {
            try {
                Atom a = pfp.getAtom(ai);
                sum = sum.plus(a.getCenter());
                count++;
            } catch (AtomNotFoundException e) {
                Logger.getLogger("caver").log(Level.WARNING,
                        "Starting atom not found (" + ai + ").", e);
            }
        }
        if (null != startingPoint_) {
            sum = sum.plus(startingPoint_);
            count++;
        }
        sum = sum.divide(count);
        return sum;
    }


    /*
     * Returns null if internal triangulation should be used.
     */
    public File getExternalTriangulationPath() {
        return externalTriangulationPath;
    }

    public PeriodicTable getPeriodicTable() {
        String name = getInstalationDirectory() + File.separator
                + "atom_radii.csv";
        try {
            PeriodicTable periodicTable = new PeriodicTable(
                    new File(name));
            return periodicTable;
        } catch (IOException e) {
            Logger.getLogger("caver").log(Level.WARNING, "Missing file {0}",
                    name);
            throw new RuntimeException(e);
        }
    }

    public File getApproximationDir() {
        File d = new File(dataDir_ + File.separator + "approximation");
        if (!d.exists()) {
            d.mkdir();
        }
        return d;
    }

    public File getOuterPdbFile() {
        File f = new File(dataDir_ + File.separator + "outer.pdb");
        return f;
    }

    public File getInnerPdbFile() {
        File f = new File(dataDir_ + File.separator + "inner.pdb");
        return f;
    }

    

    public void cleanDirectory(File dir) {
        FileOperations.deleteDirectory(dir);
        dir.mkdir();
    }

    public File getApproximationFile(File pdbFile) {

        String pdbFileName = pdbFile.getName();
        File f;
        if (pdbFileName.contains(".pdb")) {
            f = new File(getApproximationDir() + File.separator
                    + pdbFileName.replace(
                    ".pdb", "_aprox.pdb"));
        } else {
            f = new File(getApproximationDir() + File.separator
                    + pdbFileName + "_aprox.pdb");
        }

        return f;
    }

    public boolean saveApproximation() {
        return saveApproximation;
    }

    public boolean oneRadiusApproximation() {
        return oneRadiusApproximation;
    }

    public CostFunction getPassingFunction() {
        return passingFunction;
    }

    public double getMaxShatter() {
        return maxShatter;
    }

    public double getDissimilarRadiusTolerance() {
        return dissimilarRadiusTolerance;
    }

    public double getProfileTunnelSamplingStep() {
        return profileTunnelSamplingStep;
    }

    public double getVisualizationTunnelSamplingStep() {
        return visualizationTunnelSamplingStep;
    }

    public boolean saveDynamicsVisualization() {
        return saveDynamicsVisualization;
    }

    public void copyPdbRepresentant(File file) {
        pdbRepresentant = new File(dataDir_ + File.separator + file.getName());
        HotSpotWizardMyUtil.copyFile(file,
                pdbRepresentant);
    }

    public void setLoadTrajectory(boolean b) {
        loadTrajectory = b;
    }

    public void prepareVisualizationScript(File in, File out) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(in));
        BufferedWriter bw = new BufferedWriter(new FileWriter(out));
        String line;
        while (null != (line = br.readLine())) {

            if (line.contains("$load_trajectory")) {
                line = line.replace("$load_trajectory",
                        loadTrajectory ? "True" : "False");
                bw.write(line);
                bw.newLine();
            } else if (line.contains("$pymol_scripts")) {
                line = line.replace("$pymol_scripts",
                        pymolDir_.getAbsolutePath().replace("\\", "/"));
                bw.write(line);
                bw.newLine();
            } else if (line.contains("$pdb_representant")) {
                line = line.replace("$pdb_representant",
                        pdbRepresentant.getName());
                bw.write(line);
                bw.newLine();
            } else if (line.contains("$pdb_dir")) {
                line = line.replace("$pdb_dir",
                        this.getPdbDir().getAbsolutePath().replace("\\", "/"));
                bw.write(line);
                bw.newLine();
            } else if (line.contains("$load_pdb_frames_into_vmd")) {

                SortedMap<SnapId, File> files = getTrajectoryFiles(
                        getTimeSparsity(), getFirstFrame(), getLastFrame());

                boolean first = true;
                for (SnapId snap : files.keySet()) {
                    File f = files.get(snap);
                    String path = f.getAbsolutePath().toString().
                            replace("\\", "/");
                    if (first) {
                        bw.write("mol load pdb ${dir}/" + f.getName());
                        first = false;
                    } else {
                        bw.write("animate read pdb ${dir}/" + f.getName());
                    }
                    bw.newLine();
                }
            } else if (line.contains("$computation_id")) {
                String id = pdbRepresentant.getName();
                if (id.endsWith(".pdb") || id.endsWith(".ent")) {
                    id = id.substring(0, id.length() - 4);
                }

                line = line.replace("$computation_id",
                        "'" + id + "'");
                bw.write(line);
                bw.newLine();
            } else {
                bw.write(line);
                bw.newLine();
            }

        }
        bw.close();
        br.close();
    }

    public void prepareBatch(File in, File out) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(in));
        BufferedWriter bw = new BufferedWriter(new FileWriter(out));
        String line;
        while (null != (line = br.readLine())) {

            if (line.contains("$vmd")) {
                line = line.replace("$vmd",
                        getVmdPath().replace("\\", "/"));
                bw.write(line);
                bw.newLine();
            } else if (line.contains("$linux_vmd")) {
                line = line.replace("$linux_vmd",
                        getVmdLinuxPath().replace("\\", "/"));
                bw.write(line);
                bw.newLine();
            } else {
                bw.write(line);
                bw.newLine();
            }

        }
        bw.close();
        br.close();
    }

    public File getOriginsPdb() {
        return new File(dataDir_ + File.separator + "origins.pdb");
    }

    public File getVoronoiOriginsPdb() {
        return new File(dataDir_ + File.separator + "v_origins.pdb");
    }

    public double getDesiredRadius() {
        return desiredRadius;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public double getDefaultMaxDistance() {
        return defaultMaxDistance;
    }

    public boolean generateVoronoi() {
        return generateVoronoi;
    }

    public boolean doFrameClustering() {
        return frameClustering;
    }

    public double getBottleneckMultiplier() {
        return bottleneckMultiplier;
    }

    public int getNumberOfSpheres() {
        return balls;
    }

    public boolean addCentralSphere() {
        return addCentralSphere;
    }

    public File getWarningLog() {
        return new File(getOutputDirectory().getPath() + File.separator
                + "warnings.txt");

    }

    public File getAdvicesFile() {
        return new File(getOutputDirectory().getPath() + File.separator
                + "advices.txt");

    }

    public File getTrainingFile() {
        return new File(dataDir_ + File.separator
                + "training.arff");

    }

    public int getExactClusteringLimit() {
        return hierarchicalClusteringLimit;
    }

    public Integer getExactClusteringPool() {
        return exactClusteringPool;
    }

    public int getMinTrainingTunnels() {
        return minTrainingTunnels;
    }

    public int getLayersCount() {
        return layersCount;
    }

    public boolean doApproximateClustering() {
        return doApproximateClustering;
    }

    public LayeredTunnel.SpaceTransformation getSpaceTransformation() {
        return spaceTransformation;
    }

    public LayersSettings getGlobalLayersSettings() {
        return gls;
    }

    public LayersSettings getFrameLayersSettings() {
        return fls;
    }

    public LayeredTunnel.SpaceTransformation getFrameSpaceTransformation() {
        return frameSpaceTransformation;
    }

    public double getFrameClusteringThreshold() {
        return frameClusteringThreshold;
    }

    public String getClassifier() {
        return classifier;
    }

    public int getMaxTrainingClusters() {
        return maxTrainingClusters;
    }

    public boolean generateUnclassifiedCluster() {
        return generateUnclassifiedCluster;
    }

    public double getOriginProtectionRadius() {
        return originProtectionRadius;
    }

    public double getLengthImportance() {
        return lengthImportance;
    }

    public int getWaypointCount() {
        return waypointCount;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean computeBottleneckResidues() {
        return computeBottleneckResidues;
    }

    public double getCorrectionShift() {
        return correctionShift;
    }

    public int getCorrectionGridSize() {
        return correctionGridSize;
    }

    public boolean correctApproximation() {
        return correctApproximation;
    }

    public MolecularSystem createMolecularSystem(PdbFileProcessor pfp) {
        MolecularSystem ms = MolecularSystem.create(pfp);


        Set<ResidueRange> eri = new HashSet<ResidueRange>();
        Set<ResidueRange> iri = new HashSet<ResidueRange>();

        try {
            for (ResidueStringRange r : excludeResiduesIded) {

                ResidueId a = pfp.guessResidue(r.getA()).getId();
                ResidueId b = pfp.guessResidue(r.getB()).getId();

                if (a.getChain() != b.getChain()) {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "Residue range spans more chains. {0}-{1}",
                            new Object[]{a.toString(), b.toString()});
                }

                ResidueRange rr = new ResidueRange(a, b);
                eri.add(rr);
            }
        } catch (ResidueNotFoundException e) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Starting residue " + e.getResidueId() + " not found.",
                    e);
        }
        try {
            for (ResidueStringRange r : includeResiduesIded) {
                ResidueId a = pfp.guessResidue(r.getA()).getId();
                ResidueId b = pfp.guessResidue(r.getB()).getId();

                if (a.getChain() != b.getChain()) {
                    Logger.getLogger("caver").log(Level.WARNING,
                            "Residue range spans more chains. {0}-{1}",
                            new Object[]{a.toString(), b.toString()});
                }

                ResidueRange rr = new ResidueRange(a, b);
                iri.add(rr);
            }
        } catch (ResidueNotFoundException e) {
            Logger.getLogger("caver").log(Level.WARNING,
                    "Starting residue " + e.getResidueId() + " not found.",
                    e);
        }

        if (saveBeforeFilter) {
            ms.save(new File(dataDir_ + File.separator + "before.pdb"));
        }


        ms.remove(includeResiduesNamed,
                iri,
                includeAtomsNumbered,
                excludeResiduesNamed,
                eri,
                excludeAtomsNumbered, includeAll || (!otherIncludePresent));

        if (saveAfterFilter) {
            ms.save(new File(dataDir_ + File.separator + "after.pdb"));
        }
        return ms;
    }

    public String getVmdPath() {
        return vmdPath;
    }

    public String getVmdLinuxPath() {
        return vmdLinuxPath;
    }

    public boolean findBoundingBox() {
        return findBoundingBox;
    }

    public int getBoundingBoxN() {
        return boundingBoxN;
    }

    public File getBoundingBoxFile() {
        return boundingBoxFile;

    }

    public File getThroughtputStatistics() {
        return new File(analysisDir_ + File.separator
                + "throughputs.csv");
    }

    public File getThroughtputHistograms() {
        return new File(histogramsDir_ + File.separator
                + "throughput_histograms.csv");
    }

    public File getBottleneckHistograms() {
        return new File(histogramsDir_ + File.separator
                + "bottleneck_histograms.csv");
    }

    public Histogram getBottleneckHistogram() {
        return bottleneckHistogram;
    }

    public Histogram getThroughputHistogram() {
        return throughputHistogram;
    }

    public double getThroughputBestFraction() {
        return throughputBestPercent;
    }

    public File getTrajectoryFile() {
        return new File(dataDir_ + File.separator
                + "trajectory.pdb");

    }

    public boolean generateTrajectory() {
        return generateTrajectory;
    }

    public double getProfileHeatMapResolution() {
        return profileHeatMapResolution;
    }

    public int getMaxHeatMapWidth() {
        return maxHeatMapWidth;
    }

    public File getPallette() {
        return new File(getInstallationDirectory().getPath() + File.separator
                + "pallette.png");
    }

    public File getUnknownColor() {
        return new File(getInstallationDirectory().getPath() + File.separator
                + "unknown.png");
    }

    public File getProfileHeatMapsDirectory() {
        return profileHeatMapsDir_;
    }

    public File getBottleneckHeatMapsDirectory() {
        return bottleneckHeatMapsDir_;
    }

    public File getProfileAverageImagesDirectory() {
        return profileAverageImagesDir_;
    }

    public File getProfileHeatMapImage(int priority) {
        return new File(getProfileHeatMapsDirectory() + File.separator
                + "cl_" + fill(priority, 6) + "_profile_heat_map.png");
    }

    public File getProfileAverageImage(int priority) {
        return new File(getProfileAverageImagesDirectory() + File.separator
                + "cl_" + fill(priority, 6) + "_average_heat_map.png");
    }

    public File getProfileHeatMapCsv(int priority) {
        return new File(profileCsvDir_ + File.separator
                + "cl_" + fill(priority, 6) + "_heat_map.csv");
    }

    public File getBottleneckHeatMapCsv(int priority) {
        return new File(bottleneckCsvDir_ + File.separator
                + "cl_" + fill(priority, 6) + "_heat_map.csv");
    }

    public File getProfileAverageCsv(int priority) {
        return new File(profileAverageCsvDir_ + File.separator
                + "cl_" + fill(priority, 6) + "_average_heat_map.csv");
    }

    public double getHeatMapLow() {
        return heatMapLow;
    }

    public double getHeatMapHigh() {
        return heatMapHigh;
    }

    public int getZoomX() {
        return heatMapElementX;
    }

    public int getZoomY() {
        return heatMapElementY;
    }

    public double getBottleneckHeatMapResolution() {
        return bottleneckHeatMapResolution;
    }

    public File getBottleneckPallette() {
        return new File(getInstallationDirectory().getPath() + File.separator
                + "bottleneck_pallette.png");
    }

    public File getBottleneckUnknownColor() {
        return new File(getInstallationDirectory().getPath() + File.separator
                + "bottleneck_unknown.png");
    }

    public double getBottleneckHeatMapLow() {
        return bottleneckHeatMapLow;
    }

    public double getBottleneckHeatMapHigh() {
        return bottleneckHeatMapHigh;
    }

    public int getBottleneckZoomX() {
        return bottleneckHeatMapElementX;
    }

    public int getBottleneckZoomY() {
        return bottleneckHeatMapElementY;
    }

    public void setAverageVoronoiOrigin(Point p) {
        averageVoronoiOrigin_ = p;
    }

    public void setAverageOrigin(Point p) {
        averageOrigin_ = p;
    }

    public Point getAverageVoronoiOrigin() {
        return averageVoronoiOrigin_;
    }

    public Point getAverageOrigin() {
        return averageOrigin_;
    }

    public void setTunnelsByFrame(SortedMap<SnapId, Tunnels> tunnelsByFrame) {
        this.tunnelsByFrame_ = tunnelsByFrame;

    }

    public SortedMap<SnapId, Tunnels> getTunnelsByFrame() {
        return tunnelsByFrame_;
    }

    public void setFrameToStart(SortedMap<SnapId, Point> frameToStart) {
        frameToStart_ = frameToStart;
    }

    public SortedMap<SnapId, Point> getFrameToStart() {
        return frameToStart_;
    }

    public List<String> getSummaryComments() {
        try {
            File file = new File(installationDir_ + File.separator
                    + "summary_comments.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            List<String> list = new ArrayList<String>();
            while (null != (line = br.readLine())) {
                list.add(line);
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean experimental() {
        return experimental;
    }

    public File getLogFile() {
        return new File(outDir_ + File.separator + "log.txt");
    }

    public File getTimesFile() {
        return new File(dataDir_ + File.separator + "times.txt");
    }

    public boolean generateProfileHeatMaps() {
        return generateProfileHeatMap;
    }

    public boolean generateBottleneckHeatMap() {
        return generateBottleneckHeatMap;
    }

    public double getCostFunctionExponent() {
        return costFunctionExponent;
    }

    public boolean checkVisibility() {
        return checkVisibility;
    }

    public double getVisibilityProbeRadius() {
        return visibilityProbeRadius;
    }

    public boolean saveErrorProfiles() {
        return saveErrorProfiles;
    }

    public boolean computeErrors() {
        return computeErrors;
    }

    public boolean generateHistrograms() {
        return generateHistograms;
    }

    public boolean generateTunnelProfiles() {
        return generateTunnelProfiles;
    }

    public boolean generateSummary() {
        return generateSummary;
    }

    public boolean generateTunnelCharacteristics() {
        return generateTunnelCharacteristics;
    }

    public boolean doAverageSurfaceGlobal() {
        return averageSurfaceGlobal;
    }

    public boolean doAverageSurfaceFrame() {
        return averageSurfaceFrame;
    }

    public File getSurfaceFile() {
        return new File(dataDir_ + File.separator + "surface.pdb");
    }

    public File getSurfaceDefinitionFile() {
        return new File(dataDir_ + File.separator + "surface_definition.pdb");
    }

    public double getSurfaceSmoothness() {
        return averageSurfaceSmoothness;
    }

    public double getSurfaceSmoothnessAngle() {
        return averageSurfaceSmoothnessAngle;
    }

    public boolean saveSurfaceVisualization() {
        return saveSurfaceVisualization;
    }

    public void saveDeadEnd(Point p) {
        if (producePoints()) {
            if (saveZones) {
                File deadEndFile = new File(dataDir_ + File.separator + "end_zone.pdb");
                if (null == deadEndWriter) {
                    deadEndWriter = new PdbFileWriter(deadEndFile);
                }
                deadEndWriter.savePoint(p);
            }
        }
    }

    public void saveDeadStart(Point p) {
        if (producePoints()) {
            if (saveZones) {
                File deadStartFile = new File(dataDir_ + File.separator + "start_zone.pdb");
                if (null == deadStartWriter) {
                    deadStartWriter = new PdbFileWriter(deadStartFile);
                }
                deadStartWriter.savePoint(p);
            }
        }
    }

    public void saveLayerPoint(Point p) {
        if (saveLayerPoints && producePoints()) {
            File layerFile = new File(dataDir_ + File.separator + "layers.pdb");
            if (null == layerWriter) {
                layerWriter = new PdbFileWriter(layerFile);
            }
            layerWriter.savePoint(p);
        }

    }

    public void increaseLayerResidue() {
        if (null != layerWriter) {
            layerWriter.increaseResidue();
        }
    }

    public void setLayerResidue(int i) {
        if (null != layerWriter) {
            layerWriter.setResidue(i);
        }
    }

    public boolean lucky() {
        double r = getRandom().nextDouble();
        if (r < averageSurfaceDensity) {
            return true;
        } else {
            return false;
        }

    }

    public void startProducingPoints(boolean b) {
        producePoints = b;
    }

    public boolean producePoints() {
        return producePoints;
    }

    public double getAverageSurfaceTunnelSamplingStep() {
        return averageSurfaceTunnelSamplingStep;
    }

    public double getSurfacePointMaxDist() {
        return averageSurfacePointMinDist;
    }

    public AdvancedCalculationSettings getAdvanced() {
        return acs_;
    }

    private AtomRange getAtomRange(String s) {

        char a = '-';
        char b = '–';

        if ((s.contains(a + "") && a != s.charAt(0))
                || s.contains(b + "") && b != s.charAt(0)) {
            StringTokenizer rst = new StringTokenizer(s, "" + a + b);
            int start = Integer.parseInt(rst.nextToken());
            int end = Integer.parseInt(rst.nextToken());
            return new AtomRange(start, end);
        } else {
            return new AtomRange(Integer.parseInt(s));
        }

    }

    private ResidueStringRange getResidueRange(String s) {

        char a = '-';
        char b = '–';

        if ((s.contains(a + "") && a != s.charAt(0))
                || s.contains(b + "") && b != s.charAt(0)) {
            StringTokenizer rst = new StringTokenizer(s, "" + a + b);
            String start = rst.nextToken();
            String end = rst.nextToken();
            return new ResidueStringRange(start, end);
        } else {
            return new ResidueStringRange(s);
        }

    }

    public File getEdgesDir() {
        return edgesDir_;
    }

    public boolean swap() {
        return swap;
    }

    public boolean saveSwap() {
        return saveSwap;
    }

    public boolean evaluateClassification() {
        return evaluateClassification;
    }

    public boolean loadTrainingSet() {
        return loadTrainingSet;
    }

    public boolean saveAllTunnels() {
        return visualizeUnprocessedTunnels;
    }

    public int visualizeMaxTunnels() {
        return visualizeMaxTunnels;
    }

    public boolean checkDistance() {
        return checkDistance;
    }

    public Double getExactMinBottleneck() {
        return exactMinBottleneck;
    }

    public double getStandardDeviationMultiplier() {
        return standardDeviationMultiplier;
    }

    public int getClusterCharacterizationSample() {
        return clusterCharacterizationSample;
    }

    public int getOutlierCharacterizationSample() {
        return outlierCharacterizationSample;
    }

    public double getMinCostFunctionRadius() {
        return minCostFunctionRadius;
    }

    /*
     * Converts angle to distance on surface of unit sphere.
     */
    private double angleToDistance(double a) {
        double radians = (a * 2 * Math.PI / 360);

        double tg = Math.tan(radians / 2);
        double tg2 = tg * tg;
        double d2 = 4 * tg2 / (tg2 + 1);
        double d = Math.sqrt(d2);
        return d;

    }

    private static double distanceToAngle(double d) {
        double radians = 2 * Math.atan(d / 2 / Math.sqrt(1 - d * d / 4));
        return 360 * radians / 2 / Math.PI;
    }

    public static void main(String[] args) {
    }

    public boolean protect() {
        return protect;
    }

    public File getBottleneckHeatMapImage() {
        return new File(bottleneckHeatMapsDir_ + File.separator
                + "bottleneck_heat_map.png");
    }

    public File getBottleneckHeatMapCsv() {
        return new File(bottleneckHeatMapsDir_ + File.separator
                + "bottleneck_heat_map.csv");
    }

    public boolean checkClustering() {
        return checkClustering;
    }

    public int protectTunnelsIfMoreSnapshotsThan() {
        return protectTunnelsIfMoreSnapshotsThan;
    }

    public boolean doVoids() {
        return doVoids;
    }

    public final File getDeepVoidsFile() {
        return new File(dataDir_ + File.separator + "deep.pdb");
    }

    public final File getDeepPointsFile() {
        return new File(dataDir_ + File.separator + "deep_points.pdb");
    }

    public final File getVoidsFile(int i) {
        return new File(dataDir_ + File.separator + "void_" + i + ".pdb");
    }

    public double getMinTunnelLength() {
        return minTunnelLength;
    }

    public void logNoLayers() {
        noLayers = true;
    }

    public void initNoLayers() {
        noLayers = false;
    }

    public boolean hadNoLayers() {
        return noLayers;
    }

    public Point getStartingPoint() {
        return startingPoint_;
    }

    public Set<Integer> getStartingAtoms() {
        return startingAtoms_;

    }

    public Set<String> getStartingResidues() {
        return startingResidues_;
    }

    public String iterableToString(Iterable it, String sep) {
        Iterator i = it.iterator();
        StringBuilder sb = new StringBuilder();
        while (i.hasNext()) {
            sb.append(i.next().toString());
            if (i.hasNext()) {
                sb.append(sep);
            }
        }
        return sb.toString();
    }
}
