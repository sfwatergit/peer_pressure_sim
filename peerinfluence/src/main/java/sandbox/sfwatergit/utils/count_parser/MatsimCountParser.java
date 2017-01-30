package sandbox.sfwatergit.utils.count_parser;

import com.google.common.collect.Lists;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.util.CSVReaders;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.counts.CountSimComparison;
import org.matsim.counts.CountSimComparisonImpl;
import org.matsim.counts.algorithms.CountSimComparisonKMLWriter;

import java.util.List;

/**
 * Parses counts file. Writes to KML.
 *
 * TODO: Permit writing of multiple counts files.
 * Created by sidneyfeygin on 5/23/16.
 */
public class MatsimCountParser {

    private static void writeKML(List<CountSimComparison> comparisons, Network network, String outfile){
        CountSimComparisonKMLWriter countSimComparisonKMLWriter = new CountSimComparisonKMLWriter(comparisons,network, TransformationFactory.getCoordinateTransformation("epsg:26910","epsg:4326"));
        countSimComparisonKMLWriter.writeFile(outfile);
        System.out.println("Done writing KML!");
    }

    private static List<CountSimComparison> parseFile(String filename){
        final List<String[]> strings = readFile(filename);
        if (strings != null) {
            strings.remove(0);  // remove header
        }
        List<CountSimComparison> comparisons = Lists.newArrayList();
        assert strings != null;
        for (String[] row : strings) {
            final Id<Link> linkId = Id.createLinkId(row[0]);
            int hour = Integer.parseInt(row[1]);
            double countsValSim = Double.parseDouble(row[2]);
            double countsVal2 = Double.parseDouble(row[3]);
            comparisons.add(new CountSimComparisonImpl(linkId,hour,countsVal2,countsValSim));
        }
        return comparisons;
    }

    private static List<String[]> readFile(String filename){
        return CSVReaders.readTSV(filename);

    }


    /**
     * Sample main call. Will generate the kml to output file according to provided arguments.
     *
     * @param args 0: location of counts file; 1: location of network; 2: output filename
     */
    public static void main(String[] args) {
        final Network network = ScenarioUtils.loadScenario(ConfigUtils.loadConfig(args[1])).getNetwork();
        MatsimCountParser.writeKML(MatsimCountParser.parseFile(args[0]),network,args[2]);
    }
}
