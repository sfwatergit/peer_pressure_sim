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

package sandbox.sfwatergit.analysis.stats.congestion;

import org.apache.log4j.Logger;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import sandbox.sfwatergit.peerinfluence.internalization.congestion.handlers.CongestionHandlerImplV3;

import java.io.File;

/**
 * (1) Computes marginal congestion events based on a standard events file.
 * (2) Computes agent money events based on these marginal congestion events.
 *
 * @author ikaddoura
 */
public class CongestionEventsWriter {
    private static final Logger log = Logger.getLogger(CongestionEventsWriter.class);

    static String runDirectory;
    static String runId;
    static String iter;
    private static String prefix;

    public static void main(String[] args) {

        if (args.length > 0) {

            runDirectory = args[0];
            runId = args[1];
            iter = args[2];
            log.info("runDirectory: " + runDirectory);

        } else {
            runId = "c01";
            iter = "0";
            runDirectory = "/Volumes/barnacle/c01/";
        }
        prefix = runId + "." + iter + ".";
        CongestionEventsWriter congestionEventsWriter = new CongestionEventsWriter();
        congestionEventsWriter.run();
    }

    private void run() {

        log.info("Loading scenario...");
        Config config = ConfigUtils.loadConfig(runDirectory +  runId + ".output_config.xml");
        config.network().setInputFile(runDirectory + runId+ ".output_network.xml");
        config.plans().setInputFile(runDirectory + prefix + "plans.xml.gz");
        MutableScenario scenario = (MutableScenario) ScenarioUtils.loadScenario(config);
        log.info("Loading scenario... Done.");

        String outputDirectory = runDirectory + "/" + iter + "/travelTime/";

        File file = new File(outputDirectory);
        if (!file.isDirectory()) {
            file.mkdirs();
        }

        EventsManager events = EventsUtils.createEventsManager();

        EventWriterXML eventWriter = new EventWriterXML(outputDirectory + iter + ".events_ExternalCongestionCost_Offline.xml.gz");
        CongestionHandlerImplV3 congestionHandler = new CongestionHandlerImplV3(events, scenario);


        events.addHandler(eventWriter);
        events.addHandler(congestionHandler);


        log.info("Reading events file...");
        MatsimEventsReader reader = new MatsimEventsReader(events);
        reader.readFile(runDirectory + prefix + "events.xml.gz");
        log.info("Reading events file... Done.");

        eventWriter.closeFile();

        congestionHandler.writeCongestionStats(outputDirectory + config.controler().getLastIteration() + ".congestionStats_Offline.csv");
    }

}


