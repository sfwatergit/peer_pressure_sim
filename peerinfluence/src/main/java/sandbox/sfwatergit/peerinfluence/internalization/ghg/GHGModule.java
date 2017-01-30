package sandbox.sfwatergit.peerinfluence.internalization.ghg;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;

/**
 * Created by sidneyfeygin on 7/13/15.
 */
public class GHGModule {
    private static final Logger logger = Logger.getLogger(GHGModule.class);
    private final Scenario scenario;
    private GHGEmissionsHandler ghgEmissionsHandler;
    private EventsManager ghgEventsManager;


    @Inject
    public GHGModule(Scenario scenario) {
        this.scenario = scenario;
    }

    public void createGHGEmissionsHandler() {
        ghgEventsManager = EventsUtils.createEventsManager();

        logger.info("entering createGHGEmissionHandler");

        ghgEmissionsHandler = new GHGEmissionsHandler(scenario.getNetwork(), scenario, ghgEventsManager);
        logger.info("leaving createGHGEmissionHandler");
    }


    public GHGEmissionsHandler getGHGEmissionsHandler() {
        return this.ghgEmissionsHandler;
    }

    public EventsManager getGhgEventsManager() {
        return ghgEventsManager;
    }

    public void writeEmissionInformation(String ghgEventOutputFile) {
        logger.info("Warm emissions were not calculated for " + this.ghgEmissionsHandler.getLinkLeaveWarnCnt() + " of " + this.ghgEmissionsHandler.getLinkLeaveCnt() + " link leave events (no corresponding link enter event).");
    }
}
