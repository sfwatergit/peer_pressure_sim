package sandbox.sfwatergit.analysis.filters;

import com.google.common.collect.Lists;
import edu.uci.ics.jung.graph.util.Pair;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sandbox.sfwatergit.PeerPressureConstants.getInDir;
import static sandbox.sfwatergit.PeerPressureConstants.getOutDir;

/**
 * Filters population to only include people whose home locations are near transit stops
 * Created by sidneyfeygin on 7/2/15.
 */
public class PersonNearTransitStopFilter {

    public static final String configFile = getInDir.apply("sf_bay/calibration_test.xml");
    public static final String outFile = getOutDir.apply("/sf_bay/popNearTransitStop.xml");
    private static final Logger log = Logger.getLogger(PersonNearTransitStopFilter.class);
    private static final double RADIUS = 2000;
    private static ArrayList<Id<Person>> toAdd = Lists.newArrayList();

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.loadConfig(configFile));
        final Population population = scenario.getPopulation();
        final Map<Id<Person>, ? extends Person> personMap = population.getPersons();
        final TransitSchedule transitSchedule = scenario.getTransitSchedule();
        Map<Id<TransitStopFacility>, TransitStopFacility> facilities = transitSchedule.getFacilities();
        List<Coord> stopCoords = Lists.newArrayList();

        stopCoords.addAll(facilities.values().stream().map(TransitStopFacility::getCoord).collect(Collectors.toList()));

        for (Map.Entry<Id<Person>, ? extends Person> entry : personMap.entrySet()) {
            final Pair<Coord> homeWorkCoords = getHomeWorkCoords(entry.getValue());
            if (isInCircle(homeWorkCoords, stopCoords)) {
                toAdd.add(entry.getKey());
            }
        }
        Population nearTransit = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getPopulation();
        for (Id<Person> personId : toAdd) {
            nearTransit.addPerson(personMap.get(personId));
        }
        new PopulationWriter(nearTransit, scenario.getNetwork()).write(outFile);
    }

    public static Pair<Coord> getHomeWorkCoords(Person person) {

        Coord homeCoord = null;
        Coord workCoord = null;
        for (PlanElement planElement : person.getSelectedPlan().getPlanElements()) {
            if (planElement instanceof Activity) {
                Activity activity = (Activity) planElement;
                final String type = activity.getType();
                if (type != null) {
                    if (type.equals("h1")) {
                        homeCoord = activity.getCoord();
                    }
                    if (type.equals("w1")) {
                        workCoord = activity.getCoord();
                    }
                }

            }
        }
        assert homeCoord != null;
        assert workCoord != null;
        return new Pair<>(homeCoord, workCoord);
    }

    public static boolean isInCircle(Pair<Coord> hwCoords, List<Coord> stopCoords) {
        boolean homeInCircle = false;
        boolean workInCircle = false;
        for (Coord stopCoord : stopCoords) {
            Coord homeCoord = hwCoords.getFirst();
            if (euclideanDistance(new double[]{stopCoord.getX(), stopCoord.getY()},
                    new double[]{homeCoord.getX(), homeCoord.getY()}) < RADIUS) {
                homeInCircle = true;
            }
        }
        Coord workCoord = hwCoords.getSecond();
        for (Coord stopCoord : stopCoords) {
            if (euclideanDistance(new double[]{stopCoord.getX(), stopCoord.getY()},
                    new double[]{workCoord.getX(), workCoord.getY()}) < RADIUS) {
                workInCircle = true;
            }
        }
        return (homeInCircle && workInCircle);
    }

    public static double euclideanDistance(double[] a, double[] b) {
        return Math.sqrt(Math.pow((a[1] - b[1]), 2.0) + Math.pow((a[1] - b[1]), 2.0));
    }
}
