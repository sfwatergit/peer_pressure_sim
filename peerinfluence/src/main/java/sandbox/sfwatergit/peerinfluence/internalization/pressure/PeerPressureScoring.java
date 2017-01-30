package sandbox.sfwatergit.peerinfluence.internalization.pressure;

import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.SumScoringFunction;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm.PressurePersonData;

import java.util.Optional;

/**
 * Custom scoring function for cost of peer influence
 * Created by sidneyfeygin on 1/18/16.
 */
public class PeerPressureScoring implements SumScoringFunction.BasicScoring {

    private final PressureDataManager pressureDataManager;
    private final Person person;

    private Double pressureScorePenalty = 0.0D;

    PeerPressureScoring(PressureDataManager pressureDataManager, Person person) {
        this.pressureDataManager = pressureDataManager;
        this.person = person;
    }

    @Override
    public void finish() {
        final Optional<PressurePersonData> pressurePersonData = Optional.of(pressureDataManager.getPressurePersonData().get(person.getId()));
        pressurePersonData.ifPresent(ppd -> pressureScorePenalty = ppd.getPressurePenalty());
    }

    @Override
    public double getScore() {
        double pressureScore = pressureScorePenalty;
        if(Double.isInfinite(pressureScore)){
            return 0.0;
        }else {
            return -pressureScore;
        }
    }
}
