package sandbox.sfwatergit.analysis.scripts;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import sandbox.sfwatergit.analysis.modules.VspAnalyzer;
import sandbox.sfwatergit.analysis.modules.travelTime.TravelTimeAnalyzer;

import static sandbox.sfwatergit.PeerPressureConstants.getInDir;

/**
 * Implements {@link TravelTimeAnalyzer}
 *
 * Created by sidneyfeygin on 3/30/16.
 */
public class ModeTravelTimeAnalysis {

    //////////////
    // CONSTANTS
    //////////////
    public static final String RUN_ID = "c01";

    public static final String[] ITERS = new String[]{"0", "80"};

    private static final String OUTPUT_DIR = "/Volumes/barnacle/final_pp_outputs/";

    private static final String EVENTS_FILE = "/Volumes/barnacle/c01/c01.%s.events.xml.gz";

    private static final String CONFIG_FILE = getInDir.apply("sf_bay/calibration_test.xml");

    /////////////
    // MEMBERS
    ////////////
    private final Scenario scenario;
    private final VspAnalyzer analysis;

    ///////////////
    // Constructor
    ///////////////

    public ModeTravelTimeAnalysis() {
        this.scenario = ScenarioUtils.loadScenario(ConfigUtils.loadConfig(CONFIG_FILE));
        TravelTimeAnalyzer travelTimeAnalyzer = new TravelTimeAnalyzer();
        travelTimeAnalyzer.init(scenario);
        analysis = new VspAnalyzer(OUTPUT_DIR, EVENTS_FILE);
    }

    public static void main(String[] args) {
            ModeTravelTimeAnalysis m = new ModeTravelTimeAnalysis();

    }
}
