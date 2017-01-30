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

import com.google.inject.Inject;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;
import sandbox.sfwatergit.peerinfluence.internalization.ghg.GHGCostModule;
import sandbox.sfwatergit.peerinfluence.internalization.ghg.GHGModule;

/**
 * @author sidneyfeygin after amit after Benjamin and Ihab
 */
public class GHGCongestionTravelDisutilityCalculatorFactory implements TravelDisutilityFactory {

    private final GHGModule emissionModule;
    private final GHGCostModule emissionCostModule;
    private final PlanCalcScoreConfigGroup cnScoringGroup;

    @Inject
    public GHGCongestionTravelDisutilityCalculatorFactory(GHGModule ghgModule, GHGCostModule ghgCostModule,	PlanCalcScoreConfigGroup cnScoringGroup) {
        this.emissionModule = ghgModule;
        this.emissionCostModule = ghgCostModule;
        this.cnScoringGroup = cnScoringGroup;
    }

    @Override
    public TravelDisutility createTravelDisutility(TravelTime timeCalculator) {
        final GHGCongestionTravelDisutilityCalculator ghgtdc = new GHGCongestionTravelDisutilityCalculator(timeCalculator, cnScoringGroup, this.emissionModule, this.emissionCostModule);

        return new TravelDisutility() {

            @Override
            public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
                return ghgtdc.getLinkTravelDisutility(link, time, person, vehicle);
            }

            @Override
            public double getLinkMinimumTravelDisutility(Link link) {
                return ghgtdc.getLinkMinimumTravelDisutility(link);
            }
        };
    }

}
