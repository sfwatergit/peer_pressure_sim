package sandbox.sfwatergit.analysis.modules.vtts;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.events.handler.EventHandler;
import sandbox.sfwatergit.analysis.modules.AbstractAnalysisModule;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sidneyfeygin on 8/15/15.
 */
public class VTTSAnalysisModule extends AbstractAnalysisModule {

    private String runDirectory;
    private int iteration;
    private VTTSHandler vttsHandler;

    /**
     * @param runDirectory directory containing runs
     */
    public VTTSAnalysisModule(String runDirectory) {
        super("VTTS Module");
        this.runDirectory = runDirectory;
    }

    public void init(Scenario scenario, Integer iteration) {
        vttsHandler = new VTTSHandler(scenario);
        this.iteration = iteration;
    }


    @Override
    public List<EventHandler> getEventHandler() {
        List<EventHandler> handler = new LinkedList<>();
        handler.add(vttsHandler);
        return handler;
    }

    @Override
    public void preProcessData() {

    }

    @Override
    public void postProcessData() {
        vttsHandler.computeFinalVTTS();
    }

    @Override
    public void writeResults(String outputFolder) {
        vttsHandler.printVTTS(runDirectory + "ITERS/it." + iteration + "/" + iteration + ".VTTS.csv");
        vttsHandler.printAvgVTTSperPerson(runDirectory + "ITERS/it." + iteration + "/" + iteration + ".avgVTTS.csv");
        vttsHandler.printCarVTTS(runDirectory + "ITERS/it." + iteration + "/" + iteration + ".VTTS_car.csv");
    }
}
