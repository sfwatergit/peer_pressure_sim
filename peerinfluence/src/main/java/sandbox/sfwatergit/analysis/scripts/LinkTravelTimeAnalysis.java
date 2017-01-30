package sandbox.sfwatergit.analysis.scripts;

import com.google.common.base.Joiner;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import sandbox.sfwatergit.analysis.modules.travelTime.LinkTravelTimeCalculator;
import sandbox.sfwatergit.analysis.stats.congestion.ExperiencedDelayAnalyzer;
import sandbox.sfwatergit.utils.obj.MapUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static sandbox.sfwatergit.PeerPressureConstants.getInDir;

/**
 * Link-specific travel delays differences and person costs btwn before and after pressure case
 * <p>
 * Created by sidneyfeygin on 3/21/16.
 */
public class LinkTravelTimeAnalysis {

    private static final double MARGINAL_UTIL_MONEY=0.083;
    private static final double MARGINAL_UTIL_PERF_SEC=6.0/3600;
    private static final double MARGINAL_UTIL_TRAVEL_CAR_SEC=-0.134/3600;
    private static final double MARGINAL_UTIL_TRAVEL_TIME = MARGINAL_UTIL_TRAVEL_CAR_SEC+MARGINAL_UTIL_PERF_SEC;
    private static final double VTTS_CAR = MARGINAL_UTIL_TRAVEL_TIME/MARGINAL_UTIL_MONEY;
    private static final String OUTPUT_DIR = "/Volumes/barnacle/pp_out_1617/";
//    private static final String OUTPUT_DIR = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/output/toy/";
    private static final String EVENTS_ROOT = OUTPUT_DIR;
//    private static final String EVENTS_ROOT = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/output/toy/ITERS/";
    private static final String EVENTS_FILE = EVENTS_ROOT+"it.%s/01.%s.events.xml";
//    private static final String EVENTS_FILE = EVENTS_ROOT+"it.%s/%s.events.xml";
    private static final String CONFIG_FILE = getInDir.apply("/pp_out_1617.xml");
//    private static final String CONFIG_FILE = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/toy/config.xml";
    private static final String RUN_ID = "01";
    public static final String[] ITERS = new String[]{"5", "80"};


    public static void main(String[] args) {
        final LinkTravelTimeAnalysis linkTravelTimeAnalysis = new LinkTravelTimeAnalysis();
//        linkTravelTimeAnalysis.runPersonDelays();
        linkTravelTimeAnalysis.runLinkDelays();
//        for (String iter : ITERS) {
//            try {
//                linkTravelTimeAnalysis.writeLinkTravelTimes(iter);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

    }

    private void runLinkDelays(){
        Map<Id<Link>, Double> linkDelays1 = getLinkDelays(ITERS[0]);
        Map<Id<Link>, Double> linkDelays2 = getLinkDelays(ITERS[1]);
        final Map<Id<Link>, Double> linkDelayDiffMap = MapUtils.subtractMaps(linkDelays1, linkDelays2);
        try {
            writeId2DelayMap(linkDelayDiffMap, String.format("%s_link_delay_diff_%s_%s.csv", RUN_ID,ITERS[0],ITERS[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void runPersonDelays(){
        Map<String,Map<Id<Person>,Double>> iter2DelaysPerPerson = new HashMap<>();

        for (String iter : ITERS) {
            final Map<Id<Person>, Double> delaysPerPerson = getDelaysPerPerson(String.format(EVENTS_FILE, iter,iter), ScenarioUtils.loadScenario(ConfigUtils.loadConfig(CONFIG_FILE)));
            iter2DelaysPerPerson.put(iter,delaysPerPerson);
        }

        final Map<Id<Person>, Double> delayDiff = MapUtils.subtractMaps(iter2DelaysPerPerson.get(ITERS[0]), iter2DelaysPerPerson.get(ITERS[1]));
        try {
            writeId2DelayMap(delayDiff, String.format("%s_person_delay_costs_diff_%s_%s.csv", RUN_ID, ITERS[0],ITERS[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T> void writeId2DelayMap(final Map<Id<T>, Double> map, final String fileName) throws IOException {
        BufferedWriter writer = IOUtils.getBufferedWriter(OUTPUT_DIR+fileName);
        writer.write("id, delay(sec)\n");
        for (Map.Entry<Id<T>, Double> id2Delay : map.entrySet()) {
            writer.write(Joiner.on(',').join(id2Delay.getKey(),id2Delay.getValue()+"\n"));
        }
    }



    private void writeLinkTravelTimes(String iter) throws IOException {
        final String eventsFile = String.format(EVENTS_FILE, iter,iter);
        LinkTravelTimeCalculator lttc = new LinkTravelTimeCalculator(eventsFile);
        lttc.preProcessData();
        Map<Id<Link>, Map<Id<Person>, List<Double>>> link2Person2TravelTime = lttc.getLink2Person2TravelTime();
        String outfile = String.format(OUTPUT_DIR + "01.%s.link_travel_times.csv", iter);
        BufferedWriter writer = IOUtils.getBufferedWriter(outfile);
        writer.write("linkId \t personCount \t totalTravelTimeSec \t avgTravelTimeSec \n");

        for (Id<Link> linkId : link2Person2TravelTime.keySet()) {
            double sum = 0;
            int count = 0;
            for (Id<Person> personId : link2Person2TravelTime.get(linkId).keySet()) {
                for (double d : link2Person2TravelTime.get(linkId).get(personId)) {
                    sum += d;
                    count++;
                }
            }
            writer.write(linkId + ", " + count + ", " + sum + ", " + sum / count + "\n");
        }
        writer.close();
    }

    private Map<Id<Link>,Double> getLinkDelays(String iter) {
        String eventFile = String.format(EVENTS_FILE, iter,iter);
        ExperiencedDelayAnalyzer linkAnalyzer = new ExperiencedDelayAnalyzer(eventFile, ScenarioUtils.loadScenario(ConfigUtils.loadConfig(CONFIG_FILE)), 1);
        linkAnalyzer.run();
        final Map<Double, Map<Id<Link>, Double>> timeBin2LinkId2Delay = linkAnalyzer.getTimeBin2LinkId2Delay();
        final Iterator<Map<Id<Link>, Double>> iterator = timeBin2LinkId2Delay.values().iterator();
        return iterator.next();
    }


    private Map<Id<Person>, Double> getDelaysPerPerson(final String eventsFile, final Scenario sc){
        ExperiencedDelayAnalyzer personAnalyzer = new ExperiencedDelayAnalyzer(eventsFile,sc,1);
        personAnalyzer.run();
        Map<Id<Person>, Double> personId2DelaysCosts= new HashMap<>();
        final Iterator<Map<Id<Person>, Double>> iterator = personAnalyzer.getTimeBin2AffectedPersonId2Delay().values().iterator();
        final Map<Id<Person>, Double> next = iterator.next();

        for(Id<Person> id :next.keySet() ){
            personId2DelaysCosts.put(id, VTTS_CAR*next.get(id));
        }
        return personId2DelaysCosts;
    }

}
