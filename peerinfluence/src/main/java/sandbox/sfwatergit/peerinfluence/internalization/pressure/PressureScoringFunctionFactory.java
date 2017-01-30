package sandbox.sfwatergit.peerinfluence.internalization.pressure;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.SumScoringFunction.BasicScoring;
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelLegScoring;
import org.matsim.core.scoring.functions.SubpopulationCharyparNagelScoringParameters;
import sandbox.sfwatergit.analysis.stats.scoretracking.NamedBasicScoring;
import sandbox.sfwatergit.analysis.stats.scoretracking.ScoreTrackingListener;
import sandbox.sfwatergit.peerinfluence.internalization.ExternalityType;
import sandbox.sfwatergit.peerinfluence.internalization.externalitytracking.ExternalityManager;
import sandbox.sfwatergit.peerinfluence.internalization.externalitytracking.ExternalityScoring;

/**
 * Tracks all data for user for pressure scoring
 * Created by sidneyfeygin on 1/7/16.
 */
@Singleton
public class PressureScoringFunctionFactory implements ScoringFunctionFactory {

    private final SubpopulationCharyparNagelScoringParameters parameters;
    private final Scenario scenario;
    private final ScoreTrackingListener tracker;
    private final PressureDataManager pressureDataManager;
    private final ExternalityManager externalityManager;


    @Inject
    public PressureScoringFunctionFactory(Scenario scenario,
                                          ScoreTrackingListener tracker,
                                          PressureDataManager pressureDataManager,
                                          ExternalityManager externalityManager) {
        this.scenario = scenario;
        this.tracker = tracker;
        this.pressureDataManager = pressureDataManager;
        this.parameters = new SubpopulationCharyparNagelScoringParameters(scenario);
        this.externalityManager = externalityManager;
    }


    @Override
    public ScoringFunction createNewScoringFunction(Person person) {
        final SumScoringFunction scoringFunctionSum = new SumScoringFunction();

        final Id<Person> id = person.getId();

        addScoringFunction(id, scoringFunctionSum, new CharyparNagelActivityScoring(parameters.getScoringParameters(person)),"Activity");

        addScoringFunction(id, scoringFunctionSum, new CharyparNagelLegScoring(parameters.getScoringParameters(person), scenario.getNetwork()),"Leg");

        addScoringFunction(id, scoringFunctionSum, new PeerPressureScoring(pressureDataManager, person),"Peer Pressure");

        for (ExternalityType externalityType : ExternalityType.values()) {
            addScoringFunction(id, scoringFunctionSum, new ExternalityScoring(externalityManager, person, externalityType),externalityType.toString());
        }
        return scoringFunctionSum;
    }

    private SumScoringFunction addScoringFunction(
            final Id<Person> personId,
            final SumScoringFunction function,
            final BasicScoring element, String name) {
        tracker.addScoringFunction(personId, new NamedBasicScoring(element,name));
        function.addScoringFunction(element);
        return function;
    }


}
