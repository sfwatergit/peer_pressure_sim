package sandbox.sfwatergit.peerinfluence.internalization.pressure.routing;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.selectors.ExpBetaPlanChanger;
import org.matsim.core.router.TripRouter;
import org.matsim.facilities.ActivityFacilities;


public class SwitchFlaggedModeStrategyFactory implements Provider<PlanStrategy> {

    public static final String STRATEGY_NAME = "SelectFlaggedExpBeta";
    private Scenario scenario;

    @Inject private GlobalConfigGroup globalConfigGroup;
    @Inject private ActivityFacilities facilities;
    @Inject private Provider<TripRouter> tripRouterProvider;

    @Inject
    protected SwitchFlaggedModeStrategyFactory(Scenario scenario) {
        this.scenario = scenario;
    }


    @Override
    public PlanStrategy get() {

        final Config config = scenario.getConfig();

        final PlanStrategyImpl.Builder builder = new PlanStrategyImpl.Builder(new ExpBetaPlanChanger<>(config.planCalcScore().getBrainExpBeta()));


        builder.addStrategyModule(
                new SwitchFlaggedModeModule(config.global().getNumberOfThreads()
                ));

        builder.addStrategyModule(new FlaggedReroute(facilities,tripRouterProvider,globalConfigGroup));

        return builder.build();
    }

}
