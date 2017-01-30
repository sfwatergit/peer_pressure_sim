package sandbox.sfwatergit.peerinfluence.internalization.ghg;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.emissions.events.WarmEmissionEvent;
import org.matsim.contrib.emissions.events.WarmEmissionEventHandler;
import sandbox.sfwatergit.peerinfluence.internalization.externalitytracking.AbstractMarginalExternalityHandler;

/**
 * Converts emissions events to utils and assigns to agents.
 * <p>
 * Created by sidneyfeygin after benjamin on 7/13/15.
 */
public class GHGInternalizationHandler extends AbstractMarginalExternalityHandler implements WarmEmissionEventHandler {

    private GHGCostModule emissionCostModule;
    private final double marginalUtilityOfMoney;


    public GHGInternalizationHandler(GHGCostModule emissionCostModule, double costRatio,double marginalUtilityOfMoney) {
        super(costRatio);
        this.emissionCostModule = emissionCostModule;
        this.marginalUtilityOfMoney = marginalUtilityOfMoney;
    }

    @Override
    public void handleEvent(WarmEmissionEvent event) {
        Id<Person> personId = Id.create(event.getVehicleId(), Person.class);

        double warmEmissionCosts =  - emissionCostModule.calculateGHGEmissionsEvent(event.getWarmEmissions())* marginalUtilityOfMoney;

      incrementPersonExternality(personId,warmEmissionCosts);

    }



}
