package sandbox.sfwatergit.peerinfluence.run.scripts;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import sandbox.sfwatergit.peerinfluence.run.PeerPressureScenarioUtils;
import sandbox.sfwatergit.peerinfluence.run.config.PeerPressureAnalysisConfigGroup;
import sandbox.sfwatergit.utils.pt.gtfsUtils.efficiency.routeCaching.PTQSimFactory;

import java.util.Optional;

/**
 * Entry-point for standard Peer Pressure simulation.
 *
 */
public class VanillaPeerPressureSimulation {

    private final String configFile;

    private VanillaPeerPressureSimulation(String configFile){
        this.configFile = configFile;
    }

    public void run(Double pressureCost) {
        final Config config = PeerPressureScenarioUtils.loadConfig(configFile);

        final PeerPressureAnalysisConfigGroup ppConfigGroup = ConfigUtils.addOrGetModule(config, PeerPressureAnalysisConfigGroup.class);
        ppConfigGroup.setPressureCost(pressureCost);

        final Scenario sc = ScenarioUtils.loadScenario(config);
        Controler controler = new Controler(sc);
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                bindMobsim().toProvider(() -> new PTQSimFactory().createMobsim(sc, controler.getEvents()));
            }
        });
        PeerPressureScenarioUtils.addMainPPModules(controler);
        controler.run();
    }

    public static void main(String[] args) {
        final VanillaPeerPressureSimulation peerPressureSimulation = new VanillaPeerPressureSimulation(args[0]);
        final String pressureCost = Optional.ofNullable(args[1]).orElse("1.0");
        peerPressureSimulation.run(Double.parseDouble(pressureCost));
    }


}
