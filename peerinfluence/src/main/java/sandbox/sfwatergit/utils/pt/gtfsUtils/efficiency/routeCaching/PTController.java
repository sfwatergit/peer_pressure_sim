package sandbox.sfwatergit.utils.pt.gtfsUtils.efficiency.routeCaching;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import sandbox.sfwatergit.peerinfluence.run.PeerPressureScenarioUtils;

/**
 * Caching Umlauf controller
 *
 * Created by sfeygin on 11/16/16.
 */
public class PTController {

    public static void main(String[] args) {
        Scenario sc = ScenarioUtils.loadScenario(PeerPressureScenarioUtils.loadConfig(args[0]));
        final Controler controler = new Controler(sc);
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                bindMobsim().toProvider(() -> new PTQSimFactory().createMobsim(sc, controler.getEvents()));
            }
        });
        controler.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
        controler.run();
    }
}
