package sandbox.sfwatergit.analysis.modules.legMode;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.SortedMap;

/**
 * Compute the mode share from a set of plans
 *
 * Created by sfeygin on 1/19/17.
 */
public class ModeShareAnalysis {

    public final String planFileName;


    public ModeShareAnalysis(String planFileName) {
        this.planFileName = planFileName;
    }

    public void printModeShare(){
        final Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(this.planFileName);


        final SortedMap<String, Double> mode2PctShareFromPlans = ModalShareUtils.getMode2PctShareFromPlans(scenario.getPopulation());
        System.out.println("Mode,Share");
        mode2PctShareFromPlans.forEach((s, d) -> {
            System.out.println(s+","+d);

        });
    }

    public static void main(String[] args) {
        final ModeShareAnalysis modeShareAnalysis = new ModeShareAnalysis(args[0]);
        modeShareAnalysis.printModeShare();
    }
}
