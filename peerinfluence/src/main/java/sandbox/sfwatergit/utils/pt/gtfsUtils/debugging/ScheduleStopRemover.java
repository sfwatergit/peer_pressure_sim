package sandbox.sfwatergit.utils.pt.gtfsUtils.debugging;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.accessibility.CSVWriter;
import org.matsim.contrib.util.CSVReaders;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleFactoryImpl;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.pt2matsim.tools.ScheduleCleaner;
import org.matsim.pt2matsim.tools.ScheduleTools;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by sfeygin on 11/13/16.
 */
public class ScheduleStopRemover {
    private static final Logger log = Logger.getLogger(ScheduleStopRemover.class);

    final Scenario sc;
    private final String csvPath;

    public ScheduleStopRemover(String scFile,String csvPath) {
        sc = ScenarioUtils.loadScenario(ConfigUtils.loadConfig(scFile));
        this.csvPath = csvPath;
    }

    public static void main(String[] args) {

        final String confFile = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/sf_bay/config_gtfs.xml";
//        final String tsPathIn = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/sf_bay/schedule/sf_bay_schedule_all.xml";
//        final String tsPathOut = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/sf_bay/schedule/sf_bay_schedule_all_clean.xml";
//        String csvPath="/Users/sfeygin/current_code/java/research/ucb_smartcities_all/sandbox/sfwatergit/src/main/java/sandbox/sfwatergit/pt/gtfsUtils/stops.csv";
//
//        final ScheduleStopRemover ssR = new ScheduleStopRemover(confFile,csvPath);
//        ssR.umLaufeTester();
//        ssR.removeStopsFromTransitSchedule(tsPathIn,tsPathOut,csvPath);
        final Scenario sc = ScenarioUtils.loadScenario(ConfigUtils.loadConfig(confFile));
        final TransitSchedule ts = sc.getTransitSchedule();
//        final Vehicles tv = sc.getTransitVehicles();
//        ScheduleCleaner.cleanVehicles(ts,tv);
//        VehicleWriterV1 vW = new VehicleWriterV1(tv);
//        vW.writeFile("/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/sf_bay/vehicles/sf_bay_vehicles_all_1.xml");
        ScheduleCleaner.combineIdenticalTransitRoutes(ts);
        System.out.println("Done");
    }

    public void umLaufeTester(){
        final MyUmlaufInterpolator myUmlaufInterpolator = new MyUmlaufInterpolator(sc.getNetwork(), sc.getConfig().planCalcScore());
        ReconstructingUmlaufBuilder reconstructingUmlaufBuilder = new ReconstructingUmlaufBuilder(this.sc.getTransitSchedule().getTransitLines().values(),this.sc.getTransitVehicles(), myUmlaufInterpolator);
        reconstructingUmlaufBuilder.build();
        final Set<String> badNodes = myUmlaufInterpolator.getBadNodeIds();

        final CSVWriter csvWriter = new CSVWriter(this.csvPath);
        Pattern p = Pattern.compile("(\\d+)");
        final Set<String> badStopIds = Sets.newHashSet();
        for (String badNode : badNodes) {
            Matcher m = p.matcher(badNode);
            String badStopNum = "";
            if(m.find()){
                badStopNum=m.group(0);
            }
            String agencyId = badNode.substring(0,2);
            badStopIds.add(String.format("%s.link:%s_%s_link_%s", badStopNum, agencyId, badStopNum, agencyId));
        }

        badStopIds.forEach(csvWriter::writeField);
        csvWriter.close();
    }

//    public void umlaufeTester(){
//        Collection<TransitLine> transitLines = this.scenario.getTransitSchedule().getTransitLines().values();
//        GreedyUmlaufBuilderImpl greedyUmlaufBuilder = new GreedyUmlaufBuilderImpl(new UmlaufInterpolator(this.scenario.getNetwork(), this.scenario.getConfig().planCalcScore()), transitLines);
//        Collection<Umlauf> umlaeufe = greedyUmlaufBuilder.build();
//
//    }

    public void removeStopsFromTransitSchedule(String tsPathIn, String tsPathOut, String csvPath){
        final TransitSchedule tsIn = ScheduleTools.readTransitSchedule(tsPathIn);
        final List<String[]> stopLinkIdRefs = CSVReaders.readCSV(csvPath);
        final Set<Id<TransitStopFacility>> tStops = stopLinkIdRefs.stream().flatMap(Stream::of).map(s -> Id.create(s, TransitStopFacility.class)).collect(Collectors.toSet());
        final TransitSchedule tsOut = ScheduleStopRemover.removeTransitLinesFromTransitSchedule(tsIn, ScheduleStopRemover.getLinesServingTheseStops(tsIn, tStops));
        ScheduleTools.writeTransitSchedule(tsOut, tsPathOut);
    }


    public static Set<Id<TransitLine>> getLinesServingTheseStops(TransitSchedule transitSchedule, Set<Id<TransitStopFacility>> stopIds){
        log.info("Searching for lines serving one of the following stops:" + stopIds);
        Set<Id<TransitLine>> linesServingOneOfThoseStops = new TreeSet<>();

        for (TransitLine line : transitSchedule.getTransitLines().values()) {
            for (TransitRoute route : line.getRoutes().values()) {
                linesServingOneOfThoseStops.addAll(route.getStops().stream().filter(stop -> stopIds.contains(stop.getStopFacility().getId())).map(stop -> line.getId()).collect(Collectors.toList()));
            }
        }

        log.info("Found the following " + linesServingOneOfThoseStops.size() + " lines: " + linesServingOneOfThoseStops);
        return linesServingOneOfThoseStops;
    }

    public static TransitSchedule removeTransitLinesFromTransitSchedule(TransitSchedule transitSchedule, Set<Id<TransitLine>> linesToRemove){
        log.info("Removing " + linesToRemove + " lines from transit schedule...");

        TransitSchedule tS = new TransitScheduleFactoryImpl().createTransitSchedule();

        transitSchedule.getFacilities().values().forEach(tS::addStopFacility);

        transitSchedule.getTransitLines().values().stream().filter(line -> !linesToRemove.contains(line.getId())).forEach(tS::addTransitLine);

        log.info("Old schedule contained " + transitSchedule.getTransitLines().values().size() + " lines.");
        log.info("New schedule contains " + tS.getTransitLines().values().size() + " lines.");
        return tS;
    }
}
