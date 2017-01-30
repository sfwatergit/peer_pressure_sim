package sandbox.sfwatergit.analysis.scripts;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import sandbox.sfwatergit.PeerPressureConstants;
import sandbox.sfwatergit.analysis.modules.VspAnalyzer;
import sandbox.sfwatergit.analysis.modules.activity.ActivityType2ActDurationsAnalyzer;
import sandbox.sfwatergit.analysis.modules.legModeDistanceDistribution.LegModeDistanceDistribution;
import sandbox.sfwatergit.analysis.modules.ptAccessibility.PtAccessibility;
import sandbox.sfwatergit.analysis.modules.transitSchedule2Shp.TransitSchedule2Shp;
import sandbox.sfwatergit.analysis.modules.userbenefits.UserBenefitsAnalyzer;
import sandbox.sfwatergit.analysis.modules.vtts.VTTSAnalysisModule;
import sandbox.sfwatergit.analysis.gis.DgPopulation2ShapeWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Uses VSP modules from general travelTime directory to perform discrete travelTime tasks
 * <p>
 * Created by sidneyfeygin on 8/11/15.
 */
public class PressureStudyAnalyzer {
    public static final String CRS = PeerPressureConstants.SC_CRS;
    private static final String CONFIG_FILE = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/sf_bay/pp_out_1617.xml";
    private int iteration;
    private String outputDir;
    private VspAnalyzer analysis;
    private Config config;
    private Scenario scenario;
    private CoordinateReferenceSystem crs;
    private ArrayList<String> whichAnalyses = Lists.newArrayList();
    private String eventsFile;

    public static void main(String[] args) {

        PressureStudyAnalyzer pressureStudyAnalyzer = new PressureStudyAnalyzer();
        pressureStudyAnalyzer.run();
    }

    private void run() {

        config = ConfigUtils.loadConfig(CONFIG_FILE);
        this.iteration = 5;
        this.outputDir = "/Volumes/barnacle/pp_out_1617/analysis/";

        eventsFile = String.format("/Volumes/barnacle/pp_out_1617/it.%s/01.%s.events.xml.gz", iteration,iteration);
        scenario = ScenarioUtils.loadScenario(config);
        analysis = new VspAnalyzer(outputDir, eventsFile);
        loadModules();

        this.analysis.run();

    }

    private void runExtras() {
        crs = MGC.getCRS(CRS);
        DgPopulation2ShapeWriter dgPopulation2ShapeWriter = new DgPopulation2ShapeWriter(scenario.getPopulation());
        dgPopulation2ShapeWriter.write("home", outputDir + "/shp/homePoints.shp");
        dgPopulation2ShapeWriter.write("work", outputDir + "/shp/workPoints.shp");
    }

    public void loadModules() {


        final LegModeDistanceDistribution legModeDistanceDistribution = new LegModeDistanceDistribution();
        legModeDistanceDistribution.init(scenario);

        VTTSAnalysisModule vttsAnalysisModule = new VTTSAnalysisModule(outputDir);
        vttsAnalysisModule.init(scenario, iteration);

        // Utility by parts travelTime
//        UtilityByPartsAnalyzer utilityByPartsAnalyzer = new UtilityByPartsAnalyzer(true, true, true, true);

        // User benefits travelTime
        UserBenefitsAnalyzer userBenefitsAnalyzer = new UserBenefitsAnalyzer();
        userBenefitsAnalyzer.init(scenario);

        // Transit schedule to shapefile
        TransitSchedule2Shp transitSchedule2Shp = new TransitSchedule2Shp(scenario, CRS);

        // Car distance travelTime

        ActivityType2ActDurationsAnalyzer activityType2ActDurationsAnalyzer = new ActivityType2ActDurationsAnalyzer(outputDir,eventsFile);

        List<Integer> dists = Lists.newArrayList(100, 500, 1000);
        SortedMap<String, List<String>> activityMap = Maps.newTreeMap();
        activityMap.put("h1", Lists.newArrayList("h1"));
        activityMap.put("w1", Lists.newArrayList("w1"));
//        activityMap.put("s1", Lists.newArrayList("s1"));

        PtAccessibility ptAccessibility = new PtAccessibility(scenario, dists, 100, activityMap, CRS, 10);
//
//        Links2ESRIShape links2ESRIShape = new Links2ESRIShape(scenario.getNetwork(), "/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/sandbox/sfwatergit/src/main/resources/output_archives/sf_bay/bart_caltrain/travelTime/shp/sh_links.shp", CRS);
//
//        Nodes2ESRIShape nodes2ESRIShape = new Nodes2ESRIShape(scenario.getNetwork(), "/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/sandbox/sfwatergit/src/main/resources/output_archives/sf_bay/bart_caltrain/travelTime/shp/sf_nodes.shp", CRS);

////// Add HERE ////

//        analysis.addAnalysisModule(legModeDistanceDistribution);
        analysis.addAnalysisModule(vttsAnalysisModule);
//        analysis.addAnalysisModule(userBenefitsAnalyzer);
//        analysis.addAnalysisModule(transitSchedule2Shp);
//        analysis.addAnalysisModule(carDistanceAnalyzer);
//        analysis.addAnalysisModule(ptAccessibility);
//        analysis.addAnalysisModule(activityType2ActDurationsAnalyzer);
//        analysis.addAnalysisModule(utilityByPartsAnalyzer);

        /// Run Standalone here ///
//        links2ESRIShape.toFile();
//        nodes2ESRIShape.toFile();
    }


}
