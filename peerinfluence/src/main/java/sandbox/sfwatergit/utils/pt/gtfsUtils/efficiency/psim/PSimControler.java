package sandbox.sfwatergit.utils.pt.gtfsUtils.efficiency.psim;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.HasPlansAndId;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.contrib.eventsBasedPTRouter.TransitRouterEventsWSFactory;
import org.matsim.contrib.eventsBasedPTRouter.stopStopTimes.StopStopTimeCalculator;
import org.matsim.contrib.eventsBasedPTRouter.waitTimes.WaitTimeStuckCalculator;
import org.matsim.contrib.pseudosimulation.mobsim.PSimFactory;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.pt.router.TransitRouter;
import sandbox.sfwatergit.peerinfluence.run.PeerPressureScenarioUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Test of PSim functionality for SmartCities
 * Created by sfeygin on 11/16/16.
 */
public class PSimControler implements BeforeMobsimListener {
    final WaitTimeStuckCalculator waitTimeCalculator;
    final StopStopTimeCalculator stopStopTimeCalculator;
    final TravelTimeCalculator travelTimeCalculator;
    private final PSimFactory pSimFactory;
    Config config;
    Controler controler;
    Scenario scenario;

    public PSimControler(String configFile, String eventsFile) {
        config = PeerPressureScenarioUtils.loadConfig(configFile);
        scenario = ScenarioUtils.loadScenario(config);
        controler = new Controler(scenario);


        waitTimeCalculator = new WaitTimeStuckCalculator(
                controler.getScenario().getPopulation(),
                controler.getScenario().getTransitSchedule(),
                controler.getConfig().travelTimeCalculator().getTraveltimeBinSize(),
                (int) (controler.getConfig().qsim().getEndTime() - controler.getConfig().qsim().getStartTime()));


        stopStopTimeCalculator = new StopStopTimeCalculator(
                controler.getScenario().getTransitSchedule(),
                controler.getConfig().travelTimeCalculator().getTraveltimeBinSize(),
                (int) (controler.getConfig().qsim().getEndTime() - controler.getConfig().qsim().getStartTime()));


        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                bind(TransitRouter.class).toProvider(new TransitRouterEventsWSFactory(scenario, waitTimeCalculator.getWaitTimes(), stopStopTimeCalculator.getStopStopTimes()));
            }
        });

        travelTimeCalculator = TravelTimeCalculator.create(scenario.getNetwork(), config.travelTimeCalculator());

        EventsManager eventsManager = EventsUtils.createEventsManager();
        EventsReaderXMLv1 reader = new EventsReaderXMLv1(eventsManager);
        eventsManager.addHandler(waitTimeCalculator);
        eventsManager.addHandler(stopStopTimeCalculator);
        eventsManager.addHandler(travelTimeCalculator);
        reader.readFile(eventsFile);

        pSimFactory = new PSimFactory(scenario, eventsManager);

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                bindMobsim().toProvider(pSimFactory);
            }
        });

        controler.addControlerListener(this);
    }

    public void run(){
        controler.run();
    }

    @Override
    public void notifyBeforeMobsim(BeforeMobsimEvent event) {
        Collection<Plan> plans = controler.getScenario().getPopulation().getPersons().values().stream().map((Function<Person, Plan>) HasPlansAndId::getSelectedPlan).collect(Collectors.toCollection(ArrayList::new));
        pSimFactory.setWaitTime(waitTimeCalculator.getWaitTimes());
        pSimFactory.setTravelTime(travelTimeCalculator.getLinkTravelTimes());
        pSimFactory.setStopStopTime(stopStopTimeCalculator.getStopStopTimes());
        pSimFactory.setPlans(plans);
    }
}
