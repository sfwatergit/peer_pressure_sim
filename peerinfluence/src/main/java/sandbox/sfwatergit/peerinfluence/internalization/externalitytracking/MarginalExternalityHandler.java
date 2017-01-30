package sandbox.sfwatergit.peerinfluence.internalization.externalitytracking;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.events.handler.EventHandler;

import java.util.Optional;

/**
 * Created by sidneyfeygin on 1/9/16.
 */
public interface MarginalExternalityHandler extends EventHandler {

    /**
     * @return total externality for current iteration
     */
    double getTotalExternalityInfo();

    /**
     * Must increment total externality after handling individual  externality event. This should happen in {@link #incrementPersonExternality(Id, double)}
     * @param personExternality {@link Person}'s externality
     */
    void incrementTotalExternality(double personExternality);

    /**
     * Increment individual externality
     * @param personId the {@link Person}'s {@link Id}
     * @param personExternality {@link Person}'s externality
     */
    void incrementPersonExternality(Id<Person> personId, double personExternality);

    Optional<Double> getPersonExternalityAmount(Id<Person> personId);

    Double getPersonExternalityPenalty(Id<Person> personId);


}
