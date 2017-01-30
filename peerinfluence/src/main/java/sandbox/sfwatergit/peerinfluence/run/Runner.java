package sandbox.sfwatergit.peerinfluence.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import sandbox.sfwatergit.peerinfluence.run.modules.ScoreTrackingModule;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A very simple main.controller that implements the default MATSim traffic assignment modules.
 */

public class Runner {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        final Config config = ConfigUtils.loadConfig(args[0]);
//        PlansConverter.convertCoords(args[1],args[2]);

        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
        Scenario scenario = ScenarioUtils.loadScenario(config);

        Controler controler = new Controler(scenario);

        controler.addOverridingModule(new ScoreTrackingModule());
        clearMain(true, config.controler().getOutputDirectory());


        controler.run();



        long endTime = System.currentTimeMillis();
        long totTime = endTime - startTime;
        System.out.println("TOTAL RUNTIME: " + totTime);
    }

    private static void clearMain(boolean shouldClearMain, String outputDir) {

        if (shouldClearMain)
            try {

                final Path dir = Paths.get(outputDir);
                if (Files.isDirectory(dir)) {
                    IOUtils.deleteDirectory(new File(outputDir));
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
    }
}