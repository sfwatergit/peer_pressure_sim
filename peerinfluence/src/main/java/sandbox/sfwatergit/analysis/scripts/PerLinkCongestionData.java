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
package sandbox.sfwatergit.analysis.scripts;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import sandbox.sfwatergit.analysis.stats.congestion.ExperiencedDelayAnalyzer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;


/**
 * @author sid after amit
 */
public class PerLinkCongestionData {
    private final Logger logger = Logger.getLogger(PerLinkCongestionData.class);
    private String outputDir = "/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/sandbox/sfwatergit/src/main/resources/output_archives/sf_bay/bart_caltrain/";/*"./output/run2/";*/
    private String inputDir = "/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/sandbox/sfwatergit/src/main/resources/input/sf_bay";
    private String networkFile = outputDir + "/1-01.output_network.xml.gz";//"/network.xml";
    private String configFile = outputDir + "/1-01.output_config.xml";//"/config.xml";//


    private Network network;

    public static void main(String[] args) throws IOException {
        PerLinkCongestionData data = new PerLinkCongestionData();
        Integer[] iters = new Integer[]{140, 160, 200};
        for (Integer iter : iters) {
            data.run(String.valueOf(iter));
        }
    }

    private void run(String iter) throws IOException {

        BufferedWriter writer1 = IOUtils.getBufferedWriter(MessageFormat.format("{0}ITERS/it.{1}/1-01.{1}.timeLinkIdTotalCongestion.txt", this.outputDir, iter));//
        String eventFile = MessageFormat.format("{0}ITERS/it.{1}/1-01.{1}.events.xml.gz", outputDir, iter);//"/events.xml";//
        MutableScenario scenario = (MutableScenario) ScenarioUtils.loadScenario(ConfigUtils.loadConfig(configFile));
        this.network = scenario.getNetwork();
        ExperiencedDelayAnalyzer linkAnalyzer = new ExperiencedDelayAnalyzer(eventFile, scenario, 1);
//        linkAnalyzer.
//        linkAnalyzer.postProcessData();
        linkAnalyzer.checkTotalDelayUsingAlternativeMethod();
        Map<Double, Map<Id<Link>, Double>> time2linkIdDelays = linkAnalyzer.getTimeBin2LinkId2Delay();

        writer1.write("time \t linkId \t delay(in sec) \n");
        for (double time : time2linkIdDelays.keySet()) {
            for (Link link : this.network.getLinks().values()) {
                double delay;
                if (time2linkIdDelays.get(time).get(link.getId()) == null) delay = 0.0;
                else delay = time2linkIdDelays.get(time).get(link.getId());
                writer1.write(time + "\t" + link.getId().toString() + "\t" + delay);
                writer1.write("\n");
            }
        }
        writer1.close();
        this.logger.info("Finished Writing files.");
    }
}
