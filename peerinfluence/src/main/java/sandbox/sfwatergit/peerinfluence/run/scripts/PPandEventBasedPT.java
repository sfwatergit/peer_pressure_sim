package sandbox.sfwatergit.peerinfluence.run.scripts;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import sandbox.sfwatergit.peerinfluence.run.PeerPressureScenarioUtils;

/**
 * Entry-point for standard Peer Pressure simulation.
 *
 */
public class PPandEventBasedPT {

    private final String configFile;
    private static final Logger log = Logger.getLogger(PPandEventBasedPT.class);

    private PPandEventBasedPT(String configFile){
        this.configFile = configFile;
    }

    public void run() {
        final Scenario sc = ScenarioUtils.loadScenario(PeerPressureScenarioUtils.loadConfig(configFile));
        Controler controler = new Controler(sc);
        PeerPressureScenarioUtils.addMainPPModules(controler);
        PeerPressureScenarioUtils.addEventBasedPTModule(controler);

        controler.run();

    }

    public static void main(String[] args) {
        final PPandEventBasedPT peerPressureSimulation = new PPandEventBasedPT(args[0]);
        peerPressureSimulation.run();
    }


}
