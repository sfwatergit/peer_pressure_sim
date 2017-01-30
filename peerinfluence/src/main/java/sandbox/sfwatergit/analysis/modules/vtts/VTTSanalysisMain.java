/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package sandbox.sfwatergit.analysis.modules.vtts;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * Analyze the actual VTTS for each trip (applying a linearization for each activity)
 *
 * @author ikaddoura
 */
public class VTTSanalysisMain {
    private static final Logger log = Logger.getLogger(VTTSanalysisMain.class);

    private static String runDirectory;

    public static void main(String[] args) {

        if (args.length > 0) {
            runDirectory = args[0];
            log.info("run directory: " + runDirectory);

        } else {
            runDirectory = "/Volumes/barnacle/";
        }

        VTTSanalysisMain analysis = new VTTSanalysisMain();
        analysis.run();
    }

    private void run() {
        Integer iteration = 80;
        String runId = "c01";
        runDirectory+=runId+"/";
        String configFile = runDirectory + String.format("%s.output_config.xml", runId);
//		String configFile = runDirectory + "output_config_withoutUnknownParameters.xml";

        Config config = ConfigUtils.loadConfig(configFile);


        String populationFile = runDirectory+ String.format("%s.%s.plans.xml.gz", runId,iteration);
        String networkFile = "/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/input/sf_bay/network/combi/combi_bart_fix_clean.xml";
//		String networkFile = runDirectory + "output_network.xml.gz";

        config.plans().setInputFile(populationFile);
        config.network().setInputFile(networkFile);

        Scenario scenario = ScenarioUtils.loadScenario(config);
        EventsManager events = EventsUtils.createEventsManager();

        VTTSHandler vttsHandler = new VTTSHandler(scenario);
        events.addHandler(vttsHandler);

        String eventsFile = runDirectory + String.format("%s.%s.events.xml.gz",runId, iteration);

        log.info("Reading the events file...");
        MatsimEventsReader reader = new MatsimEventsReader(events);
        reader.readFile(eventsFile);
        log.info("Reading the events file... Done.");

        vttsHandler.computeFinalVTTS();

        vttsHandler.printVTTS(runDirectory + "travelTime/"+iteration + ".VTTS.csv");
        vttsHandler.printCarVTTS(runDirectory + "travelTime/it."+iteration+ ".VTTS_car.csv");
        vttsHandler.printAvgVTTSperPerson(runDirectory + "travelTime/it." +iteration + ".avgVTTS.csv");
    }

}


