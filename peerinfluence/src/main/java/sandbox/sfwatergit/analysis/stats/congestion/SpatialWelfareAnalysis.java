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
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import sandbox.sfwatergit.analysis.modules.MoneyEventHandler;
import sandbox.sfwatergit.analysis.modules.userbenefits.UserBenefitsCalculator;
import sandbox.sfwatergit.analysis.modules.userbenefits.WelfareMeasure;

import java.util.HashMap;
import java.util.Map;

/**
 * Compares a base case with a policy case.
 * Computes the increase/decrease of user benefits and plots these changes.
 *
 * @author ikaddoura
 */
public class SpatialWelfareAnalysis {
    private static final Logger log = Logger.getLogger(SpatialWelfareAnalysis.class);

    static String runDirectory1; // base case
    static String runDirectory2; // policy case

    static String shapeFileZones;
    static String homeActivity;
    static String workActivity;

    // the number of persons a single agent represents
    static int scalingFactor;

    public static void main(String[] args) {

        if (args.length > 0) {

            runDirectory1 = args[0];
            log.info("runDirectoryBaseCase: " + runDirectory1);

            runDirectory2 = args[1];
            log.info("runDirectoryPolicyCase: " + runDirectory2);

            shapeFileZones = args[2];
            log.info("shapeFileZones: " + shapeFileZones);

            homeActivity = args[3];
            log.info("homeActivity: " + homeActivity);

            workActivity = args[4];
            log.info("workActivity: " + workActivity);

            scalingFactor = Integer.valueOf(args[5]);
            log.info("scalingFactor: " + scalingFactor);

        } else {

            runDirectory1 = "/Users/sfeygin/remote_files/pp_out_1617/it.5/";
            runDirectory2 = "/Users/sfeygin/remote_files/pp_out_1617/it.80/";
            shapeFileZones = "/Volumes/barnacle/pp_out_1617/shp/grid_for_welfare.shp";
            homeActivity = "h1";
            workActivity = "w1";
            scalingFactor = 750;
        }

        SpatialWelfareAnalysis congestionEventsWriter = new SpatialWelfareAnalysis();
        congestionEventsWriter.run();
    }

    private void run() {

        IKGISAnalyzer gisAnalysis = new IKGISAnalyzer(shapeFileZones, scalingFactor, homeActivity, workActivity);

        // Base Case

        Config config1 = ConfigUtils.loadConfig("/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/sf_bay/pp_out_1617.xml");
        config1.network().setInputFile(runDirectory1 + "01.output_network.xml.gz");
        config1.plans().setInputFile(runDirectory1 + "01.5.plans.xml.gz");
        MutableScenario scenario1 = (MutableScenario) ScenarioUtils.loadScenario(config1);

        String eventsFile1="/Users/sfeygin/remote_files/pp_out_1617/it.5/01.5.events.xml.gz";
        Map<Id<Person>, Double> personId2userBenefit_baseCase = getPersonId2UserBenefit(scenario1);
        Map<Id<Person>, Double> personId2tollPayments_baseCase = getPersonId2TollPayments(eventsFile1);
        Map<Id<Person>, Double> personId2welfareContribution_baseCase = calculateSum(personId2userBenefit_baseCase, personId2tollPayments_baseCase);

        gisAnalysis.analyzeZones_welfare("baseCase", scenario1, runDirectory1, personId2userBenefit_baseCase, personId2tollPayments_baseCase, personId2welfareContribution_baseCase);

        // Policy Case

        Config config2 = ConfigUtils.loadConfig("/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/sf_bay/pp_out_1617.xml");
        config2.network().setInputFile(runDirectory2 + "01.output_network.xml.gz");
        config2.plans().setInputFile(runDirectory2 + "01.80.plans.xml.gz");
        MutableScenario scenario2 = (MutableScenario) ScenarioUtils.loadScenario(config2);


        String eventsFile2="/Users/sfeygin/remote_files/pp_out_1617/it.80/01.80.events.xml.gz";
        Map<Id<Person>, Double> personId2userBenefit_policyCase = getPersonId2UserBenefit(scenario2);
        Map<Id<Person>, Double> personId2tollPayments_policyCase = getPersonId2TollPayments(eventsFile2);
        Map<Id<Person>, Double> personId2welfareContribution_policyCase = calculateSum(personId2userBenefit_policyCase, personId2tollPayments_policyCase);

        gisAnalysis.analyzeZones_welfare("afterPressure", scenario2, runDirectory2, personId2userBenefit_policyCase, personId2tollPayments_policyCase, personId2welfareContribution_policyCase);

        // Comparison

        Map<Id<Person>, Double> personId2userBenefit_difference = calculateDifference(scenario1, personId2userBenefit_policyCase, personId2userBenefit_baseCase);
        Map<Id<Person>, Double> personId2tollPayments_difference = calculateDifference(scenario1, personId2tollPayments_policyCase, personId2tollPayments_baseCase);
        Map<Id<Person>, Double> personId2welfareContribution_difference = calculateDifference(scenario1, personId2welfareContribution_policyCase, personId2welfareContribution_baseCase);

        gisAnalysis.analyzeZones_welfare("pressure_baseCase_comparison", scenario2, runDirectory2, personId2userBenefit_difference, personId2tollPayments_difference, personId2welfareContribution_difference);

    }

    private Map<Id<Person>, Double> calculateSum(Map<Id<Person>, Double> personId2userBenefit, Map<Id<Person>, Double> personId2tollPayments) {
        Map<Id<Person>, Double> personId2Sum = new HashMap<Id<Person>, Double>();

        for (Id<Person> id : personId2userBenefit.keySet()) {
            if (personId2tollPayments.containsKey(id)) {
                personId2Sum.put(id, personId2userBenefit.get(id) + Math.abs(personId2tollPayments.get(id)));
            } else {
                personId2Sum.put(id, personId2userBenefit.get(id));
            }
        }
        return personId2Sum;
    }

    private Map<Id<Person>, Double> calculateDifference(MutableScenario scenario, Map<Id<Person>, Double> personId2value1, Map<Id<Person>, Double> personId2value2) {
        Map<Id<Person>, Double> personId2difference = new HashMap<Id<Person>, Double>();

        for (Id<Person> id : scenario.getPopulation().getPersons().keySet()) {
            double value1 = 0.;
            double value2 = 0.;
            if (personId2value1.containsKey(id)) {
                value1 = personId2value1.get(id);
            }
            if (personId2value2.containsKey(id)) {
                value2 = personId2value2.get(id);
            }
            personId2difference.put(id, value1 - value2);
        }
        return personId2difference;
    }

    private Map<Id<Person>, Double> getPersonId2TollPayments(String eventsFile) {

        EventsManager events = EventsUtils.createEventsManager();

        MoneyEventHandler moneyHandler = new MoneyEventHandler();
        events.addHandler(moneyHandler);

        log.info("Reading events file...");
        MatsimEventsReader reader = new MatsimEventsReader(events);
        reader.readFile(eventsFile);
        log.info("Reading events file... Done.");

        return moneyHandler.getPersonId2amount();
    }

    private Map<Id<Person>, Double> getPersonId2UserBenefit(Scenario scenario) {

        UserBenefitsCalculator userBenefitsCalculator_selected = new UserBenefitsCalculator(scenario.getConfig(), WelfareMeasure.SELECTED, true);
        userBenefitsCalculator_selected.calculateUtility_money(scenario.getPopulation());
        return userBenefitsCalculator_selected.getPersonId2MonetizedUtility();
    }

}


