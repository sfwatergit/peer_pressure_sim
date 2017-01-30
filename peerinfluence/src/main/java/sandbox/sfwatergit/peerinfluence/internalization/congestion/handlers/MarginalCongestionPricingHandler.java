/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
 *
 */
package sandbox.sfwatergit.peerinfluence.internalization.congestion.handlers;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import sandbox.sfwatergit.peerinfluence.internalization.congestion.events.CongestionEvent;
import sandbox.sfwatergit.peerinfluence.internalization.externalitytracking.AbstractMarginalExternalityHandler;


/**
* Not monetiz
 *
 * @author ikaddoura
 */
public class MarginalCongestionPricingHandler extends AbstractMarginalExternalityHandler implements CongestionEventHandler {

    private final static Logger log = Logger.getLogger(MarginalCongestionPricingHandler.class);
    private final double vot_car;


    public MarginalCongestionPricingHandler(Scenario scenario, double congestionCostRatio) {
        super(congestionCostRatio);
        this.vot_car = (scenario.getConfig().planCalcScore().getModes().get(TransportMode.car).getMarginalUtilityOfTraveling() - scenario.getConfig().planCalcScore().getPerforming_utils_hr());
        log.info("Using the same VTTS for each agent to translate delays into monetary units.");
    }


    @Override
    public void handleEvent(CongestionEvent event) {
        double amount = event.getDelay() / 3600 * this.vot_car;
        final Id<Person> causingAgentId = event.getCausingAgentId();
        incrementPersonExternality(causingAgentId,amount);
    }

}