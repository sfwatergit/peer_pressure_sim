package sandbox.sfwatergit.peerinfluence.internalization.externalitytracking;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * Created by sidneyfeygin on 1/9/16.
 */
public abstract class AbstractMarginalExternalityHandler implements MarginalExternalityHandler {

    private final double costRatio;
    protected double totalExternality = 0;
    protected Map<Id<Person>, Double> personExternalityData = new HashMap<>();

    public AbstractMarginalExternalityHandler(double costRatio) {
        this.costRatio = costRatio;
    }

    @Override
    public double getTotalExternalityInfo() {
        return totalExternality;
    }

    @Override
    public void incrementTotalExternality(double personExternality) {
        totalExternality+= personExternality;
    }

    @Override
    public void incrementPersonExternality(Id<Person>personId,double externalityToAdd){
        final Double currentExternality=personExternalityData.get(personId);
        Double newExternalityAmount;
        // Increment person externality
        if (currentExternality==null) {
            // no existing externality, so create a new one
            newExternalityAmount=externalityToAdd;
        } else {
            newExternalityAmount = currentExternality + externalityToAdd;
        }
        this.personExternalityData.put(personId,newExternalityAmount);
        incrementTotalExternality(externalityToAdd);
    }

    @Override
    public Optional<Double> getPersonExternalityAmount(Id<Person> personId) {
        return Optional.ofNullable(personExternalityData.get(personId));
    }

    @Override
    public Double getPersonExternalityPenalty(Id<Person> personId) {
        return costRatio*(totalExternality - getPersonExternalityAmount(personId).orElse(0.0));
    }


    @Override
    public void reset(int iteration) {
        totalExternality = 0.0;
        personExternalityData.clear();
    }

}
