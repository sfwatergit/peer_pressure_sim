package sandbox.sfwatergit.peerinfluence.internalization.externalitytracking;

import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.SumScoringFunction;
import sandbox.sfwatergit.peerinfluence.internalization.ExternalityType;

import java.util.Optional;

/**
 * General externality scoring... may break this up into separate externalities.
 *
 * Created by sidneyfeygin on 1/26/16.
 */
public class ExternalityScoring implements SumScoringFunction.BasicScoring {
    private final ExternalityManager externalityManager;
    private final Person person;
    private Optional<Double> externalityScorePenalty=Optional.empty();
    private final ExternalityType externalityType;

    public ExternalityScoring(ExternalityManager externalityManager, Person person, ExternalityType externalityType) {
        this.externalityManager = externalityManager;
        this.person = person;
        this.externalityType = externalityType;
    }

    @Override
    public void finish() {
        externalityScorePenalty= Optional.ofNullable(externalityManager.getPersonExternalityPenalty(person.getId(),externalityType));
    }

    @Override
    public double getScore() {
        return  externalityScorePenalty.orElse(0.0D);
    }
}
