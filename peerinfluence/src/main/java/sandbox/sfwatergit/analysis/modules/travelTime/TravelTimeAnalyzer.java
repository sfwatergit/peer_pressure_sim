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
package sandbox.sfwatergit.analysis.modules.travelTime;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.events.handler.EventHandler;
import sandbox.sfwatergit.analysis.modules.AbstractAnalysisModule;
import sandbox.sfwatergit.analysis.modules.ptDriverPrefix.PtDriverIdAnalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This module calculates for each mode the number of users, the number of departures, the total travel time,
 * the avg. travel time per user and the avg. travel time per departure.
 *
 * @author ikaddoura, benjamin
 *
 */
public class TravelTimeAnalyzer extends AbstractAnalysisModule {
    private final static Logger log = Logger.getLogger(TravelTimeAnalyzer.class);

    private List<AbstractAnalysisModule> anaModules = new LinkedList<>();

    private TravelTimePerModeEventHandler ttHandler;
    private Map<String, Map<Id, Double>> mode2personId2TravelTime;
    private Map<String, Double> mode2noOfTrips;
    private Map<String, Double> mode2avgTravelTimeOfModePerUser_mins;
    private Map<String, Double> mode2avgTravelTimeOfModePerTrip_mins;
    private Map<String, Integer> mode2numberOfPersons;
    private Map<String, Double> mode2sumOfTravelTimes_min;


    public TravelTimeAnalyzer() {
        super(TravelTimeAnalyzer.class.getSimpleName());
    }

    public void init(Scenario scenario) {

        // (sub-)module
        PtDriverIdAnalyzer ptDriverIdAnalyzer = new PtDriverIdAnalyzer();
        ptDriverIdAnalyzer.init(scenario);
        this.anaModules.add(ptDriverIdAnalyzer);

        this.ttHandler = new TravelTimePerModeEventHandler(ptDriverIdAnalyzer);
        this.mode2avgTravelTimeOfModePerTrip_mins = new HashMap<>();
        this.mode2avgTravelTimeOfModePerUser_mins = new HashMap<>();
        this.mode2numberOfPersons = new HashMap<>();
        this.mode2sumOfTravelTimes_min = new HashMap<>();
    }

    @Override
    public List<EventHandler> getEventHandler() {
        List<EventHandler> allEventHandler = new LinkedList<>();

        // from (sub-)modules
        for (AbstractAnalysisModule module : this.anaModules) {
            allEventHandler.addAll(module.getEventHandler());
        }

        // own handler
        allEventHandler.add(this.ttHandler);

        return allEventHandler;
    }

    @Override
    public void preProcessData() {
        log.info("Preprocessing all (sub-)modules...");
        this.anaModules.forEach(AbstractAnalysisModule::preProcessData);
        log.info("Preprocessing all (sub-)modules... done.");
    }

    @Override
    public void postProcessData() {

        log.info("Postprocessing all (sub-)modules...");
        this.anaModules.forEach(AbstractAnalysisModule::postProcessData);
        log.info("Postprocessing all (sub-)modules... done.");

        // own postProcessing

        this.mode2personId2TravelTime = this.ttHandler.getMode2personId2TravelTime();
        this.mode2noOfTrips = this.ttHandler.getUserGroup2mode2noOfTrips();

        for (String mode : mode2personId2TravelTime.keySet()) {
            if (mode2noOfTrips.get(mode) == null) {
                log.warn("Number of trips for mode " + mode + " is null.");
            } else {
                Map<Id, Double> personId2TravelTime = mode2personId2TravelTime.get(mode);
                double sumOfTravelTimes = 0.0;
                int numberOfPersons = 0;

                for (Id personId : personId2TravelTime.keySet()) {
                    sumOfTravelTimes += personId2TravelTime.get(personId);
                    numberOfPersons++;
                }
                double sumOfTravelTimes_min = sumOfTravelTimes / 60.;

                double avgTravelTimeOfModePerUser_mins = sumOfTravelTimes_min / numberOfPersons; // within this mode
                double avgTravelTimeOfModePerTrip_mins = sumOfTravelTimes_min / this.mode2noOfTrips.get(mode);

                this.mode2sumOfTravelTimes_min.put(mode, sumOfTravelTimes_min);
                this.mode2numberOfPersons.put(mode, numberOfPersons);
                this.mode2avgTravelTimeOfModePerUser_mins.put(mode, avgTravelTimeOfModePerUser_mins);
                this.mode2avgTravelTimeOfModePerTrip_mins.put(mode, avgTravelTimeOfModePerTrip_mins);

            }
        }
    }

    @Override
    public void writeResults(String outputFolder) {
        String fileName = outputFolder + "travelTimes.txt";
        File file = new File(fileName);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            bw.write("mode \t users \t departures \t total travelTime [min] \t avg travelTime per user [min] \t avg travelTime per departure [min]");
            bw.newLine();

            for (String mode : mode2personId2TravelTime.keySet()) {
                String modeInfo = mode + "\t" + this.mode2numberOfPersons.get(mode) + "\t" + this.mode2noOfTrips.get(mode) + "\t" + this.mode2sumOfTravelTimes_min.get(mode) + "\t" + this.mode2avgTravelTimeOfModePerUser_mins.get(mode) + "\t" + this.mode2avgTravelTimeOfModePerTrip_mins.get(mode);
                bw.write(modeInfo);
                bw.newLine();
            }

            bw.close();
            log.info("Finished writing output to " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
