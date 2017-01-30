package sandbox.sfwatergit.peerinfluence.io;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import org.matsim.api.core.v01.Scenario;

/**
 *
 * Created by sidneyfeygin on 2/18/16.
 */
public class SNKMLWriter {
    private final Scenario scenario;
    private final Kml kml;

    public SNKMLWriter(Scenario scenario) {
        this.scenario = scenario;
        kml = new Kml();

    }
}
