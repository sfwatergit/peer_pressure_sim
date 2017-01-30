package sandbox.sfwatergit.utils.pt.gtfsUtils.efficiency.psim;

import org.apache.commons.cli.ParseException;
import org.matsim.contrib.pseudosimulation.RunPSim;

/**
 * Run class for {@link PSimControler}
 *
 * Created by sfeygin on 11/16/16.
 */
public class RunPSimSmartBay {

    public static void main(String[] args) {
        try {
            RunPSim.main(args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
