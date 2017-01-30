package sandbox.sfwatergit.peerinfluence.internalization.pressure;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import lombok.val;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.population.algorithms.PlanAlgorithm;
import org.matsim.core.router.PlanRouter;
import org.matsim.core.router.TripRouterFactoryBuilderWithDefaults;
import org.matsim.core.router.util.TravelDisutilityUtils;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm.PressurePersonData;
import sandbox.sfwatergit.utils.PlanUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

/**
 * Checks whether pressure was successful and reroutes.
 *
 * Created by sidneyfeygin on 1/18/16.
 */
public class FlaggedPlanModifier implements AfterMobsimListener {


    public static final String SHIFTED = "rerouted";
    public static final String FLAGGED = "flagged";
    private final PressureDataManager pressureDataManager;
    private final Scenario scenario;


    private Map<Id<Person>, Integer> iterationMap = Maps.newTreeMap();

    @Inject
    public FlaggedPlanModifier(PressureDataManager pressureDataManager, Scenario scenario) {
        this.pressureDataManager = pressureDataManager;
        this.scenario = scenario;
    }

    @Override
    public void notifyAfterMobsim(AfterMobsimEvent event) {

        for (Map.Entry<Id<Person>,PressurePersonData> ppde : pressureDataManager.getPressurePersonData().entrySet()) {
            val ppd = ppde.getValue();
            final int iteration = event.getIteration();
            Id<Person> egoId = ppde.getKey();
            final Person ego = PlanUtils.getPerson(scenario,egoId);
            final Plan egoSelectedPlan = Optional.ofNullable(ego.getSelectedPlan()).orElse(scenario.getPopulation().getFactory().createPlan());
            final String type = egoSelectedPlan.getType();
            if (type == null) {
                ppd.setShifted(iteration,false);
                continue;
            }
            switch (type) {
                case SHIFTED:

                    ppd.setShifted(iteration,true);

                    break;
                case FLAGGED:
                    ppd.setShifted(iteration,false);

                    if (iterationMap.get(egoId) == null) // not there; must mean just pressured previous iter and was not rerouted
                    {
                        iterationMap.put(egoId, iteration);
                    } else {
                        final int i = iterationMap.get(egoId) - iteration;
                        if (i < -4) {
                            // expired
                            egoSelectedPlan.setType(null);
                            ppd.setPressuredBy(new HashSet<>());
                            iterationMap.remove(egoId);
                            System.out.println(egoId.toString()+ " is no longer eligible for replanning after " + 5 + " missed iterations.");
                        }
                    }
                    break;
                default:
                    egoSelectedPlan.setType(null);
                    ppd.setShifted(iteration,false);
                    break;
            }
        }
    }



    private PlanAlgorithm getRouter(){
        val routerFactory = new TripRouterFactoryBuilderWithDefaults();
        val disutility = TravelDisutilityUtils.createFreespeedTravelTimeAndDisutility(scenario.getConfig().planCalcScore());
        val travelTime = new FreeSpeedTravelTime();
        routerFactory.setTravelDisutility(disutility);
        routerFactory.setTravelTime(travelTime);
        val tripRouter = routerFactory.build(scenario).get();
        return new PlanRouter(tripRouter);
    }
}
