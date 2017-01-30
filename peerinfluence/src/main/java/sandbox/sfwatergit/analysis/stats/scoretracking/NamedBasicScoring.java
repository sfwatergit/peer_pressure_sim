package sandbox.sfwatergit.analysis.stats.scoretracking;

import org.matsim.core.scoring.SumScoringFunction;

/**
 * Wrapper class to add custom name to scoring function for tracking
 *
 * Created by sidneyfeygin on 2/2/16.
 */
public class NamedBasicScoring implements SumScoringFunction.BasicScoring {

    private final SumScoringFunction.BasicScoring delegate;
    private final String name;


    public NamedBasicScoring(SumScoringFunction.BasicScoring delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }

    @Override
    public void finish() {
        delegate.finish();
    }

    @Override
    public double getScore() {
        return delegate.getScore();
    }

    public String getName() {
        return name;
    }
}
