package sandbox.sfwatergit.peerinfluence.internalization;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.events.handler.EventHandler;
import sandbox.sfwatergit.peerinfluence.internalization.congestion.handlers.CongestionHandlerImplV3;
import sandbox.sfwatergit.peerinfluence.internalization.congestion.handlers.MarginalCongestionPricingHandler;
import sandbox.sfwatergit.peerinfluence.internalization.externalitytracking.ExternalityManager;
import sandbox.sfwatergit.peerinfluence.internalization.ghg.GHGCostModule;
import sandbox.sfwatergit.peerinfluence.internalization.ghg.GHGInternalizationHandler;
import sandbox.sfwatergit.peerinfluence.internalization.ghg.GHGModule;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.PressureDataManager;
import sandbox.sfwatergit.peerinfluence.run.config.ExternalitiesConfigGroup;

/**
 *  Responsible for managing flow of externality and pressure data to pressure data manager
 *
 * Created by sidneyfeygin on 7/13/15.
 *
 */
@Singleton
public class InternalizationListener implements
        StartupListener,
        IterationStartsListener,
        IterationEndsListener,
        ShutdownListener,
        EventHandler {

    //============= CONSTANTS ============
    private static final Logger log = Logger.getLogger(InternalizationListener.class);
    private final Double congestionCostRatio;
    private final Double emissionsCostRatio;

    //============= MEMBERS ============
    private final Scenario scenario;
    private final PressureDataManager pressureDataManager;
    private final EventsManager events;
    private final Config config;
    private final GHGModule ghgModule;
    private final GHGCostModule ghgCostModule;
    private final ExternalityManager externalityManager;

    //============= FIELDS ============
    private String ghgEventOutputFile;
    private EventWriterXML ghgEventWriter;
    private int iteration;
    private int firstIt;
    private int lastIt;
    private CongestionHandlerImplV3 congestionHandler;
    private OutputDirectoryHierarchy controlerIO;


    @Inject
    public InternalizationListener(
            GHGModule ghgModule,
            GHGCostModule ghgCostModule,
            Scenario scenario,
            @Named(ExternalitiesConfigGroup.CONGESTION_COST_FACTOR) Double congestionCostRatio,
            @Named(ExternalitiesConfigGroup.EMISSIONS_COST_RATIO) Double emissionsCostRatio,
            EventsManager eventsManager,
            OutputDirectoryHierarchy controlerIO,
            Config config,
            PressureDataManager pressureDataManager,
            ExternalityManager externalityManager) {

        this.ghgModule = ghgModule;
        this.ghgCostModule = ghgCostModule;
        this.scenario = scenario;
        this.congestionCostRatio = congestionCostRatio;
        this.emissionsCostRatio = emissionsCostRatio;
        this.pressureDataManager = pressureDataManager;
        this.externalityManager = externalityManager;
        this.events = eventsManager;
        this.controlerIO = controlerIO;
        this.config = config;
    }

    @Override
    public void notifyStartup(StartupEvent event) {

        firstIt = config.controler().getFirstIteration();
        lastIt = config.controler().getLastIteration();

        // GHG handling
        ghgModule.createGHGEmissionsHandler();
        events.addHandler(ghgModule.getGHGEmissionsHandler());

        this.congestionHandler = new CongestionHandlerImplV3(events, this.scenario);
        MarginalCongestionPricingHandler marginalCongestionPricingHandler = new MarginalCongestionPricingHandler(scenario, congestionCostRatio);

        events.addHandler(this.congestionHandler);
        events.addHandler(marginalCongestionPricingHandler);

        externalityManager.addExternalityHandler(ExternalityType.CONGESTION, marginalCongestionPricingHandler);

    }

    ////////////////////////////////////////////////////////////
    // Begin Scoring methods
    ///////////////////////////////////////////////////////////

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        iteration = event.getIteration();
        // Listens for emissions events. Adds to SocialPerson and score

        GHGInternalizationHandler ghgInternalizationHandler = new GHGInternalizationHandler(ghgCostModule, emissionsCostRatio,event.getServices().getConfig().planCalcScore().getMarginalUtilityOfMoney());
        externalityManager.addExternalityHandler(ExternalityType.GHG, ghgInternalizationHandler);

        log.info("adding emission internalization module to emission events stream...");
        ghgModule.getGhgEventsManager().addHandler(ghgInternalizationHandler);

        if (iteration == firstIt || iteration == lastIt) {
            ghgEventOutputFile = controlerIO.getIterationFilename(iteration, "emission.events.xml.gz");
            log.info("creating new emission events writer...");
            ghgEventWriter = new EventWriterXML(ghgEventOutputFile);
            log.info("adding emission events writer to emission events stream...");
            ghgModule.getGhgEventsManager().addHandler(ghgEventWriter);
        }
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {

        scenario.getPopulation().getPersons().keySet().forEach(id -> pressureDataManager.addExternalityData(id, externalityManager.assignPersonExternalityData(id)));
        externalityManager.removeExternalityHandler(ExternalityType.GHG);

        log.info("removing ghg internalization module from emission events stream...");
        ghgModule.getGhgEventsManager().removeHandler(ghgModule.getGHGEmissionsHandler());


        if (iteration % 10 == 0) {
            this.congestionHandler.writeCongestionStats(controlerIO.getIterationFilename(iteration, "congestionStats.csv"));
        }

        if (iteration == firstIt || iteration == lastIt) {
            log.info("removing ghg events writer from emission events stream...");
            ghgModule.getGhgEventsManager().removeHandler(ghgEventWriter);
            log.info("closing emission events file...");
            ghgEventWriter.closeFile();
        }
    }



    ////////////////////////////////////////////////////////////
    // End scoring methods
    ///////////////////////////////////////////////////////////


    @Override
    public void notifyShutdown(ShutdownEvent event) {
        ghgModule.writeEmissionInformation(ghgEventOutputFile);

    }

    @Override
    public void reset(int iteration) {
        // Do nothing
    }

}
