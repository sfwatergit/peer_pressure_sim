package sandbox.sfwatergit.peerinfluence.internalization.pressure.routing;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.population.algorithms.PlanAlgorithm;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.FlaggedPlanModifier;

import java.util.List;

/**
 * Switch to public transit if flagged
 * Created by sidneyfeygin on 8/16/15.
 */
public class SwitchFlaggedModeAlgorithm implements PlanAlgorithm {


    public SwitchFlaggedModeAlgorithm() {
    }


    @Override
    public void run(Plan plan) {
        if (plan.getType() != null && plan.getType().equals(FlaggedPlanModifier.FLAGGED)) {
            final List<PlanElement> tour = plan.getPlanElements();
            changeToFixedLegMode(tour);

        }
    }

    private void changeToFixedLegMode(List<PlanElement> tour) {
        for (PlanElement planElement : tour) {
            if (planElement instanceof Leg) {
                Leg leg = (Leg) planElement;
                leg.setMode(TransportMode.pt);
            }
        }
    }


}
