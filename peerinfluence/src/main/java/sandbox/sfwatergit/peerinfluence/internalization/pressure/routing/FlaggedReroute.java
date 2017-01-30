/* *********************************************************************** *
 * project: org.matsim.*
 * ReRoute.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

import com.google.inject.Provider;
import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.algorithms.PlanAlgorithm;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;
import org.matsim.core.router.TripRouter;
import org.matsim.facilities.ActivityFacilities;

/**
 * Note... pointing to slightly modified plan router now. Should only
 * handle flagged plans(sidfeygin).
 * <p>
 * Uses the routing algorithm provided by the {@linkplain Controler} for
 * calculating the routes of plans during Replanning.
 *
 * @author mrieser
 */
public class FlaggedReroute extends AbstractMultithreadedModule {

    private final Provider<TripRouter> tripRouterProvider;
    private final ActivityFacilities facilities;

    public FlaggedReroute(ActivityFacilities facilities, Provider<TripRouter> tripRouterProvider, GlobalConfigGroup globalConfigGroup) {
        super(globalConfigGroup);
        this.facilities = facilities;
        this.tripRouterProvider = tripRouterProvider;
    }


    @Override
    public PlanAlgorithm getPlanAlgoInstance() {
            return new FlaggedPlanReRouter(
                    tripRouterProvider.get(),
                    facilities);

    }
}
