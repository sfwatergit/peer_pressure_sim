package sandbox.sfwatergit.peerinfluence.internalization.ghg;

import com.google.inject.Inject;
import org.matsim.contrib.emissions.types.WarmPollutant;

import java.util.Map;

/**
 * Modified version of EmissionsCostModule
 * <p>
 * Created by sidneyfeygin on 7/13/15.
 *
 */
public class GHGCostModule {
    //    private final double emissionCostFactor;
    EmissionCostFactors DOLLARS_PER_GRAMM_CO2 = EmissionCostFactors.CO2_TOTAL;

    @Inject
    public GHGCostModule() {
    }

    public double calculateGHGEmissionsEvent(Map<WarmPollutant, Double> warmEmissions) {
        double warmEmissionCosts = 0.0;

        for (WarmPollutant wp : warmEmissions.keySet()) {
            if (wp.equals(WarmPollutant.CO2_TOTAL)) {
                double co2Costs = warmEmissions.get(wp) * DOLLARS_PER_GRAMM_CO2.getCostFactor();
                warmEmissionCosts += co2Costs;

            }// else  //do nothing
        }
        return warmEmissionCosts;
    }
}
