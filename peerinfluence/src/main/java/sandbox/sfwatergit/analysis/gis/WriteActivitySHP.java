package sandbox.sfwatergit.analysis.gis;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;

/**
 * Uses {@link DgPopulation2ShapeWriter} to toFile shapefile of activity locations from
 * plans xml.
 * <p>
 * Created by sidneyfeygin on 10/23/15.
 */
public class WriteActivitySHP {


    public static void main(String[] args) {

        String popFileName = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/sf_bay/population/mtc_plans_connected.xml";
        String outFileName = "/Volumes/barnacle/pp_shp";
        String netFileName = "/Volumes/barnacle/pp_out_1617/01.output_network.xml.gz";


        final Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        PopulationReader reader = new PopulationReader(scenario);
        final Network network = NetworkUtils.createNetwork();
        final NetworkReaderMatsimV2 netReader = new NetworkReaderMatsimV2(network);
        netReader.readFile(netFileName);
        reader.readFile(popFileName);

        Population pop = scenario.getPopulation();


        final SelectedPlans2ESRIShape selectedPlans2ESRIShape = new SelectedPlans2ESRIShape(pop, network, MGC.getCRS("epsg:4326"), outFileName);
        selectedPlans2ESRIShape.write();


    }

}
