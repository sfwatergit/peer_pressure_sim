package sandbox.sfwatergit.peerinfluence.run;

import org.apache.log4j.Logger;
import org.matsim.contrib.eventsBasedPTRouter.TransitRouterEventsWSModule;
import org.matsim.contrib.eventsBasedPTRouter.stopStopTimes.StopStopTimeCalculator;
import org.matsim.contrib.eventsBasedPTRouter.waitTimes.WaitTimeStuckCalculator;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigReader;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import sandbox.sfwatergit.peerinfluence.run.config.ExternalitiesConfigGroup;
import sandbox.sfwatergit.peerinfluence.run.config.PeerPressureAnalysisConfigGroup;
import sandbox.sfwatergit.peerinfluence.run.config.SocialNetworkConfigGroup;
import sandbox.sfwatergit.peerinfluence.run.modules.*;

/**
 * Convenience methods to set up trial runs for peer pressure simulation.
 * <p>
 * (inspired by thibaut in Socnetsim contrib).
 * <p>
 * Created by sidneyfeygin on 11/29/15.
 */
public class PeerPressureScenarioUtils {

    private static final Logger log = Logger.getLogger(PeerPressureScenarioUtils.class);

    // non-instantiable
    private PeerPressureScenarioUtils() {
    }

    private static void addConfigGroups(final Config config) {
        config.addModule(new SocialNetworkConfigGroup());
        config.addModule(new ExternalitiesConfigGroup());
        config.addModule(new PeerPressureAnalysisConfigGroup());
    }

    public static Config loadConfig(final String configFile) {
        Config config = createConfig();
        new ConfigReader(config).readFile(configFile);
        return config;
    }

    private static Config parameterizeConfig(Config config){
        //------ CONTROLLER
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
        //------ COUNTS
//        config.counts().setCountsFileName(PeerPressureConstants.getInDir.apply("counts/weekday_list.xml"));
//        config.counts().setWriteCountsInterval(10);
//        config.counts().setOutputFormat("html");
        //------ QSIM
//        config.qsim().setFlowCapFactor(0.007);
//        config.qsim().setStorageCapFactor(0.007);

        return config;
    }

    private static Config createConfig() {
        final Config config = ConfigUtils.createConfig();
        addConfigGroups(config);
        parameterizeConfig(config);
        return config;
    }

    public static void addEventBasedPTModule(Controler controler){
        //Routing PT
        WaitTimeStuckCalculator waitTimeCalculator = new WaitTimeStuckCalculator(controler.getScenario().getPopulation(), controler.getScenario().getTransitSchedule(), controler.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int) (controler.getConfig().qsim().getEndTime()-controler.getConfig().qsim().getStartTime()));
        controler.getEvents().addHandler(waitTimeCalculator);
        log.warn("About to init StopStopTimeCalculator...");
        StopStopTimeCalculator stopStopTimeCalculator = new StopStopTimeCalculator(controler.getScenario().getTransitSchedule(), controler.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int) (controler.getConfig().qsim().getEndTime()-controler.getConfig().qsim().getStartTime()));
        controler.getEvents().addHandler(stopStopTimeCalculator);
        controler.addOverridingModule(new TransitRouterEventsWSModule(waitTimeCalculator.getWaitTimes(), stopStopTimeCalculator.getStopStopTimes()));
    }


    public static void addMainPPModules(Controler controler){

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                install(new SocialNetworkModule());
                install(new PeerPressureModule());
                install(new PressureAnalysisModule());
                install(new ExternalitiesModule());
                install(new PressureScoringFunctionModule());
            }
        });

    }




}
