package sandbox.sfwatergit.peerinfluence.run.modules;

import org.matsim.core.controler.AbstractModule;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.pt.PtConstants;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.PressureScoringFunctionFactory;
import sandbox.sfwatergit.analysis.stats.scoretracking.TravelTimesRecord;

/**
 * All components of pressure score and tracking provided here.
 *
 * Created by sidneyfeygin on 1/7/16.
 */
public class PressureScoringFunctionModule extends AbstractModule{

    @Override
    public void install() {
        install(new ScoreTrackingModule());
        binder().bind(TravelTimesRecord.class).toInstance(new TravelTimesRecord(new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE)));
        bindScoringFunctionFactory().to(PressureScoringFunctionFactory.class);
    }
}
