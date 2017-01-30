package sandbox.sfwatergit.peerinfluence.run.modules;

import org.matsim.core.controler.AbstractModule;
import sandbox.sfwatergit.analysis.gis.PressureShapeWriter;
import sandbox.sfwatergit.analysis.stats.IterationModeShiftControlerListener;
import sandbox.sfwatergit.analysis.stats.PressureAnalysisUtils;

/**
 * Created by sidneyfeygin on 1/7/16.
 */
public class PressureAnalysisModule extends AbstractModule {

    @Override
    public void install() {

        // Analytics writer
        addControlerListenerBinding().toProvider(IterationModeShiftControlerListener.class);
        addControlerListenerBinding().to(PressureShapeWriter.class);
        bind(PressureAnalysisUtils.class);


    }

}
