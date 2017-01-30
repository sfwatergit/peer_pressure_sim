package sandbox.sfwatergit.peerinfluence.internalization.pressure;

import com.google.common.collect.Lists;
import com.google.inject.name.Names;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import org.apache.commons.collections15.Factory;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetworkImpl;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;
import sandbox.sfwatergit.peerinfluence.run.PeerPressureScenarioUtils;
import sandbox.sfwatergit.peerinfluence.run.config.PeerPressureAnalysisConfigGroup;
import sandbox.sfwatergit.peerinfluence.run.config.SocialNetworkConfigGroup;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.SocialNetworkUtils.createSocialVertex;

/**
 * Knows pressure scoring functionality testing. Highly mutable.
 * <p>
 * <p>
 * Created by sidneyfeygin on 7/22/15.
 */
public class PressureScorerTest {


    private SocialNetwork socialNetwork = new SocialNetworkImpl(new SparseGraph<>());
    private Controler controler;
    private Config config;
    private Scenario scenario;

    public PressureScorerTest() {
    }

    public static void main(String[] args) throws Exception {
        new PressureScorerTest().run();
    }

    public void run() {
        init();
        controler.run();
    }

    public void init() {
        String configFile = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/toy/config.xml";
        config = PeerPressureScenarioUtils.loadConfig(configFile);
        scenario = ScenarioUtils.loadScenario(config);
        socialNetwork = new SocialNetworkImpl(createErdosRenyiErgm(scenario));
        scenario.addScenarioElement(SocialNetwork.ELEMENT_NAME, this.socialNetwork);
        controler = new Controler(scenario);

        createPopAndSocNet();

//
//        controler.addOverridingModule(
//                new ExternalitiesModule());


//        controler.addOverridingModule(new AbstractModule() {
//            @Override
//            public void install() {
//                addPlanStrategyBinding(SwitchFlaggedModeStrategyFactory.STRATEGY_NAME).toProvider(SwitchFlaggedModeStrategyFactory.class);
//            }
//        });

    }

    /**
     * Generates a random graph
     */
    private void createPopAndSocNet() {


        PeerPressureScenarioUtils.addMainPPModules(controler);
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {

                final String socialNetFilename = config.getModule(SocialNetworkConfigGroup.GROUP_NAME).getValue(SocialNetworkConfigGroup.SN_FILE_ELEMENT_NAME);
                binder().bindConstant().annotatedWith(Names.named(SocialNetworkConfigGroup.SN_FILE_ELEMENT_NAME)).to(socialNetFilename);


                // Social cost Value
                binder().bindConstant().annotatedWith(Names.named(PeerPressureAnalysisConfigGroup.GROUP_NAME)).to(config.getModule(PeerPressureAnalysisConfigGroup.GROUP_NAME).getValue(PeerPressureAnalysisConfigGroup.PRESSURE_COST));

                // Start iteration
                binder().bindConstant().annotatedWith(Names.named(PeerPressureAnalysisConfigGroup.START_PRESSURE_ITERATION)).to(config.getModule(PeerPressureAnalysisConfigGroup.GROUP_NAME).getValue(PeerPressureAnalysisConfigGroup.START_PRESSURE_ITERATION));


                bind(SocialNetwork.class).toInstance(socialNetwork);


            }
        });

    }

    public UndirectedGraph<SocialVertex, SocialEdge> createErdosRenyiErgm(Scenario scenario) {
        final Factory<UndirectedGraph<SocialVertex, SocialEdge>> factory = UndirectedSparseGraph.getFactory();
        final UndirectedGraph<SocialVertex, SocialEdge> g = factory.create();
        final Map<Id<Person>, ? extends Person> persons = scenario.getPopulation().getPersons();
        final CopyOnWriteArrayList<Id<Person>> ids = Lists.newCopyOnWriteArrayList(persons.keySet());
        Random random = new Random(123);
        final int N = scenario.getPopulation().getPersons().size();
        for (int i = 0; i < N; i++) {
            g.addVertex(createSocialVertex(persons.get(ids.get(i))));
        }

        List<SocialVertex> list = Lists.newArrayList(g.getVertices());

        for (int i = 0; i < N-1; i++) {
            SocialVertex ego = list.get(i);
            for (int j = i + 1; j < N; j++) {
                SocialVertex alter = list.get(j);
                if (random.nextDouble() < 0.09d) {
                    g.addEdge(new SocialEdge(ego, alter), ego, alter);
                }

            }
        }
        return g;
    }

}