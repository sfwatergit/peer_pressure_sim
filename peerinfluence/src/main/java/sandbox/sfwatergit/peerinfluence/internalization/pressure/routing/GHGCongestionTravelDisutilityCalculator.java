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
package sandbox.sfwatergit.peerinfluence.internalization.pressure.routing;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.emissions.types.WarmPollutant;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;
import sandbox.sfwatergit.peerinfluence.internalization.ghg.GHGCostModule;
import sandbox.sfwatergit.peerinfluence.internalization.ghg.GHGEmissionsHandler;
import sandbox.sfwatergit.peerinfluence.internalization.ghg.GHGModule;

import java.util.Map;

/**
 * @author sidneyfeygin after amit after Benjamin and Ihab
 */
public class GHGCongestionTravelDisutilityCalculator implements TravelDisutility {

    private final Logger logger = Logger.getLogger(GHGCongestionTravelDisutilityCalculator.class);

    /*
     * Blur the Social Cost to speed up the relaxation process. Values between
     * 0.0 and 1.0 are valid. 0.0 means the old value will be kept, 1.0 means
     * the old value will be totally overwritten.
     */
    private TravelTime timeCalculator;
    private double marginalUtlOfMoney;
    private double distanceCostRateCar;
    private double marginalUtlOfTravelTime;
    private GHGModule ghgModule;
    private GHGCostModule emissionCostModule;


    public GHGCongestionTravelDisutilityCalculator(TravelTime timeCalculator, PlanCalcScoreConfigGroup cnScoringGroup, GHGModule ghgModule, GHGCostModule emissionCostModule) {
        this.timeCalculator = timeCalculator;
        this.marginalUtlOfMoney = cnScoringGroup.getMarginalUtilityOfMoney();
        this.distanceCostRateCar = cnScoringGroup.getModes().get(TransportMode.car).getMonetaryDistanceRate();
        this.marginalUtlOfTravelTime = (-cnScoringGroup.getModes().get(TransportMode.car).getMarginalUtilityOfTraveling() / 3600.0) + (cnScoringGroup.getPerforming_utils_hr() / 3600.0);
        this.emissionCostModule = emissionCostModule;
        this.ghgModule = ghgModule;


    }

    @Override
    public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle v) {
        double linkTravelDisutility;

        double linkTravelTime = this.timeCalculator.getLinkTravelTime(link, time, person, v);
        double linkTravelTimeDisutility = this.marginalUtlOfTravelTime * linkTravelTime;

        double distance = link.getLength();
        double distanceCost = -this.distanceCostRateCar * distance;
        double linkDistanceDisutility = this.marginalUtlOfMoney * distanceCost;

        double linkExpectedEmissionDisutility;
        linkExpectedEmissionDisutility = calculateExpectedEmissionDisutility(link, distance, linkTravelTime);

        /* // Test the routing:
			if(!link.getId().equals(new IdImpl("11"))) 
			generalizedTravelCost = generalizedTravelTimeCost + generalizedDistanceCost;
			else */
        linkTravelDisutility = linkTravelTimeDisutility + linkDistanceDisutility + linkExpectedEmissionDisutility;

        return linkTravelDisutility;
    }

    private double calculateExpectedEmissionDisutility(Link link, double distance, double linkTravelTime) {
        double linkExpectedEmissionDisutility;

		/* The following is an estimate of the warm emission costs that an agent (depending on her vehicle type and
		the average travel time on that link in the last iteration) would have to pay if chosing that link in the next
		iteration. Cold emission costs are assumed not to change routing; they might change mode choice or
		location choice (not implemented)! */


        GHGEmissionsHandler ghgEmissionsHandler = this.ghgModule.getGHGEmissionsHandler();
        Map<WarmPollutant, Double> expectedWarmEmissions = ghgEmissionsHandler.calculateCO2Emission(
                linkTravelTime,
                link.getFreespeed(),
                distance);

        double expectedEmissionCosts = this.emissionCostModule.calculateGHGEmissionsEvent(expectedWarmEmissions);
        linkExpectedEmissionDisutility = this.marginalUtlOfMoney * expectedEmissionCosts;
        // logger.info("expected emission costs for person " + person.getId() + " on link " + link.getId() + " at time " + time + " are calculated to " + expectedEmissionCosts);

        return linkExpectedEmissionDisutility;
    }



    @Override
    public double getLinkMinimumTravelDisutility(Link link) {
        return link.getLength() / link.getFreespeed();
    }
}
