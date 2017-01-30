package sandbox.sfwatergit.utils.pt.gtfsUtils.ptMatrix;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.contrib.matrixbasedptrouter.utils.BoundingBox;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.facilities.Facility;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import sandbox.sfwatergit.peerinfluence.run.PeerPressureScenarioUtils;
import sandbox.sfwatergit.utils.pt.gtfsUtils.GtfsPropertyManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates a PTMatrix from a transit schedule by routing it w/ a TransitRouter instance.
 *
 * Created by sfeygin on 11/13/16.
 */
public class PTMatrixCreator {

    private static final Logger log = Logger.getLogger(PTMatrixCreator.class);
    private final Scenario sc;
    private final double depTime;
    private final String[] actTypes;
    private Map<Id<TransitStopFacility>, TransitStopFacility> ptMatrixLocationsMap;
    private final QuadTree<TransitStopFacility> quadTree;
    final Collection<TransitStopFacility> transitFacilities;

    public static void main(String[] args) {
        String confFile = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/sf_bay/config_gtfs.xml";
        String[] actTypes = new String[]{"h1", "w1"};
        final PTMatrixCreator ptMatrixCreator = new PTMatrixCreator(confFile, 8. * 60 * 60, actTypes);

        ptMatrixCreator.run(1);
    }

    PTMatrixCreator(String configPath, double depTime, String[] actTypes) {

        sc = ScenarioUtils.loadScenario(PeerPressureScenarioUtils.loadConfig(configPath));

        transitFacilities = sc.getTransitSchedule().getFacilities().values();
        final BoundingBox boundingBox = BoundingBox.createBoundingBox(sc.getNetwork());
        quadTree = new QuadTree<>(boundingBox.getXMin(), boundingBox.getYMin(), boundingBox.getXMax(), boundingBox.getYMax());
        transitFacilities.forEach(tf->quadTree.put(tf.getCoord().getX(),tf.getCoord().getY(),tf));
        this.depTime = depTime;
        this.actTypes = actTypes;
    }

    public void run(int numberOfThreads){
        GtfsPropertyManager props = new GtfsPropertyManager();
        final String outputRoot = props.getOutputDir();



        Set<TransitStopFacility> tfSet = Sets.newHashSet();

        for (String actType : actTypes) {
           tfSet.addAll(getNearestStopsForActivity(actType));
        }

        ptMatrixLocationsMap=tfSet.stream().collect(Collectors.toMap(TransitStopFacility::getId,v->v));
        createStopsFile(outputRoot + "ptStops.csv", ",");
            /* Split up map of locations into (approximately) equal parts */
        int numberOfMeasuringPoints = ptMatrixLocationsMap.size();
        log.info("numberOfMeasuringPoints = " + numberOfMeasuringPoints);
        int arrayNumber = 0;
        int locationsAdded = 0;

        Map<Integer, Map> mapOfLocationFacilitiesMaps = new HashMap<>();
        for (int i = 0; i < numberOfThreads; i++) {
            Map locationFacilitiesPartialMap = new LinkedHashMap<>();
            mapOfLocationFacilitiesMaps.put(i, locationFacilitiesPartialMap);
        }

        for ( Facility fac : ptMatrixLocationsMap.values() ) {
            mapOfLocationFacilitiesMaps.get(arrayNumber).put( fac.getId(), fac ) ;
            locationsAdded++;
            if (locationsAdded == (numberOfMeasuringPoints / numberOfThreads) + 1) {
                arrayNumber++;
                locationsAdded = 0;
            }
        }

        for (int currentThreadNumber = 0; currentThreadNumber < numberOfThreads; currentThreadNumber++) {
            new ThreadedMatrixCreator(sc, mapOfLocationFacilitiesMaps.get(currentThreadNumber),
                    ptMatrixLocationsMap, depTime, outputRoot, " ", currentThreadNumber);
        }
    }

    private Set<TransitStopFacility> getNearestStopsForActivity(String actType) {
        return sc.getPopulation().getPersons().values().stream().flatMap(p->p.getSelectedPlan().getPlanElements().stream())
                .filter(Activity.class::isInstance).map(Activity.class::cast).filter(a->a.getType().startsWith(actType))
                .map(Activity::getCoord)
                .map(c->quadTree.getClosest(c.getX(),c.getY())).collect(Collectors.toSet());
    }



    /**
     * Creates a csv file containing the public transport stops or measure points
     */
    public void createStopsFile(String outputFileStops, String separator) {
        final CSVFileWriter stopsWriter = new CSVFileWriter(outputFileStops, separator);

        stopsWriter.writeField("id");
        stopsWriter.writeField("x");
        stopsWriter.writeField("y");
        stopsWriter.writeNewLine();

        for (Facility fac : ptMatrixLocationsMap.values() ) {
            stopsWriter.writeField(fac.getId());
            stopsWriter.writeField(fac.getCoord().getX());
            stopsWriter.writeField(fac.getCoord().getY());
            stopsWriter.writeNewLine();
        }

        stopsWriter.close();
        log.info("Stops file based on schedule written.");
    }




}
