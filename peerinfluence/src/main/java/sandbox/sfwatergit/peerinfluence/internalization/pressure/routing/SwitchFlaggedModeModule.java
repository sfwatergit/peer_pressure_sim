package sandbox.sfwatergit.peerinfluence.internalization.pressure.routing;

import org.matsim.core.population.algorithms.PlanAlgorithm;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;

/**
 * Plan Switching Module
 * <p>
 * Created by sidneyfeygin on 8/16/15.
 */
public class SwitchFlaggedModeModule extends AbstractMultithreadedModule {

    public SwitchFlaggedModeModule(final int nOfThreads) {
        super(nOfThreads);
    }

    @Override
    public PlanAlgorithm getPlanAlgoInstance() {
        return new SwitchFlaggedModeAlgorithm();
    }
}
