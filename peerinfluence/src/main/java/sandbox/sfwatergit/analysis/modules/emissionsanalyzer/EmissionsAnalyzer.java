/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

/**
 * @author ikaddoura
 */
package sandbox.sfwatergit.analysis.modules.emissionsanalyzer;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.emissions.events.EmissionEventsReader;
import org.matsim.contrib.emissions.types.ColdPollutant;
import org.matsim.contrib.emissions.types.WarmPollutant;
import org.matsim.contrib.emissions.utils.EmissionUtils;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.handler.EventHandler;
import sandbox.sfwatergit.analysis.modules.AbstractAnalysisModule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This module requires an emissions events file.
 * <p>
 * It then provides:
 * - total emissions per emission type
 * - warm emissions per person and emission type
 * - cold emissions per person and emission type
 * - sum of warm and cold emissions per person and emission type
 *
 * @author ikaddoura, benjamin
 */
public class EmissionsAnalyzer extends AbstractAnalysisModule {
    private final static Logger log = Logger.getLogger(EmissionsAnalyzer.class);
    private final String emissionEventsFile;
    private Scenario scenario;
    private EmissionUtils emissionUtils;
    private EmissionsPerPersonWarmEventHandler warmHandler;
    private Map<Id<Person>, Map<WarmPollutant, Double>> person2warmEmissions;
    private Map<Id<Person>, SortedMap<String, Double>> person2totalEmissions;
    private SortedMap<String, Double> totalEmissions;

    public EmissionsAnalyzer(String emissionsEventsFile) {
        super(EmissionsAnalyzer.class.getSimpleName());
        this.emissionEventsFile = emissionsEventsFile;
    }


    public void init(Scenario scenario) {

        this.scenario = scenario;
        this.emissionUtils = new EmissionUtils();
        this.warmHandler = new EmissionsPerPersonWarmEventHandler();
    }

    @Override
    public List<EventHandler> getEventHandler() {
        // the standard eventsFile is not read in this module
        return new LinkedList<>();
    }

    @Override
    public void preProcessData() {
        EventsManager eventsManager = EventsUtils.createEventsManager();
        EmissionEventsReader emissionReader = new EmissionEventsReader(eventsManager);

        eventsManager.addHandler(this.warmHandler);

        emissionReader.readFile(this.emissionEventsFile);
    }

    @Override
    public void postProcessData() {
        this.person2warmEmissions = this.warmHandler.getWarmEmissionsPerPerson();
        this.person2totalEmissions = this.emissionUtils.sumUpEmissionsPerId(person2warmEmissions, getPerson2coldEmissions());
        this.totalEmissions = this.emissionUtils.getTotalEmissions(this.person2totalEmissions);
    }

    // TODO: should probably also toFile out person2totalEmissions...
    @Override
    public void writeResults(String outputFolder) {
        String fileName = outputFolder + "emissions.txt";
        File file = new File(fileName);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            for (String pollutant : emissionUtils.getListOfPollutants()) {
                bw.write(pollutant + "\t");
            }
            bw.newLine();

            for (String pollutant : this.totalEmissions.keySet()) {
                Double pollutantValue = this.totalEmissions.get(pollutant);
                bw.write(pollutantValue.toString() + "\t");
            }
            bw.newLine();

            bw.close();
            log.info("Finished writing output to " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SortedMap<String, Double> getTotalEmissions() {
        return totalEmissions;
    }

    public Map<Id<Person>, Map<WarmPollutant, Double>> getPerson2warmEmissions() {
        return person2warmEmissions;
    }

    public Map<Id<Person>, Map<ColdPollutant, Double>> getPerson2coldEmissions() {
        final HashMap<Id<Person>, Map<ColdPollutant, Double>> dummyColdEmissions = new HashMap<>();
        final HashMap<ColdPollutant, Double> dummy = new HashMap<>();
        dummy.put(ColdPollutant.CO, 0.0);
        dummyColdEmissions.put(person2warmEmissions.keySet().iterator().next(), dummy);
        return dummyColdEmissions;

    }

    public Map<Id<Person>, SortedMap<String, Double>> getPerson2totalEmissions() {
        return person2totalEmissions;
    }

}
