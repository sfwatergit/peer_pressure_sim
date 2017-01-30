package sandbox.sfwatergit.utils;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.replanning.selectors.BestPlanSelector;
import org.matsim.core.router.MainModeIdentifier;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Static utilities for plans
 *
 * @author sidneyfeygin (7/26/15)
 */
public class PlanUtils {

    public static final MainModeIdentifier MAIN_MODE_IDENTIFIER = tripElements -> {
        for (PlanElement tripElement : tripElements) {
            if (tripElement instanceof Leg) {
                Leg element = (Leg) tripElement;
                if (element.getMode().contains(TransportMode.pt) || element.getMode().contains(TransportMode.transit_walk)) {
                    return TransportMode.pt;
                } else {
                    return TransportMode.car;
                }
            }
        }
        return TransportMode.other;
    };
    public static Function<Person, String> getModeOfPlanWithBestScore = PlanUtils::getModeOfPlanWithBestScore;

    public static double getBestPtScore(List<? extends Plan> plans) {
        return getBestModeScore(plans, TransportMode.pt);
    }

    public static boolean isDriving(Scenario scenario, Id<Person> personId){
        return getModeOfSelectedPlan(getPerson(scenario,personId)).equals(TransportMode.car);
    }

    public static boolean isWalking(Scenario scenario, Id<Person> personId){
        return getModeOfSelectedPlan(getPerson(scenario,personId)).equals(TransportMode.walk);
    }

    public static boolean isUsingPt(Scenario scenario, Id<Person> personId){
        return getModeOfSelectedPlan(getPerson(scenario,personId)).equals(TransportMode.pt);
    }

    public static boolean isTransitWalking(Scenario scenario, Id<Person> personId){
        return getModeOfSelectedPlan(getPerson(scenario,personId)).equals(TransportMode.transit_walk);
    }

    public static double getBestCarScore(List<? extends Plan> plans) {
        return getBestModeScore(plans, TransportMode.car);
    }

    public static double getBestModeScore(List<? extends Plan> plans, String transportMode) {

        double bestPlanScore = Double.NEGATIVE_INFINITY;
        for (Plan plan : plans) {
            final String mode = MAIN_MODE_IDENTIFIER.identifyMainMode(plan.getPlanElements());
            if (mode.equals(transportMode)) {
                final Double score = Optional.ofNullable(plan.getScore()).orElse(Double.NEGATIVE_INFINITY);
                if (score > bestPlanScore) {
                    bestPlanScore = score;
                }
            }
        }

        return bestPlanScore;
    }

    public static Map<String, List<Plan>> groupPlansByMode(Person person) {
        return person.getPlans().stream().collect(Collectors.groupingBy(p -> MAIN_MODE_IDENTIFIER.identifyMainMode(p.getPlanElements())));
    }

    public static String getModeOfPlanWithBestScore(Person person) {
        getPlanWithBestScore(person);
        return MAIN_MODE_IDENTIFIER.identifyMainMode(getPlanWithBestScore(person).getPlanElements());
    }

    public static String getModeOfSelectedPlan(Person person) {
        return Optional.ofNullable(MAIN_MODE_IDENTIFIER.identifyMainMode(person.getSelectedPlan().getPlanElements())).orElse(TransportMode.other);
    }

    public static Plan getPlanWithBestScore(Person person) {
        BestPlanSelector<Plan, Person> bestPlanSelector = new BestPlanSelector<>();
        return bestPlanSelector.selectPlan(person);
    }

    /**
     * Indicates whether person has public transit mode in planset.
     *
     * @param plans @link Person's planset
     * @return true if person has PT in planset
     */
    public static boolean hasPtinPlans(List<? extends Plan> plans) {
        boolean hasPt = false;
        for (Plan plan : plans) {
            final String s = MAIN_MODE_IDENTIFIER.identifyMainMode(plan.getPlanElements());
            if (s.equals(TransportMode.pt)) {
                hasPt = true;
            }
        }
        return hasPt;
    }

    public static Person getPerson(Scenario scenario, Id<Person> personId){
        return scenario.getPopulation().getPersons().get(personId);
    }

    public static double getSelectedPlanScore(Person person){
        return person.getSelectedPlan().getScore();
    }

    public static SortedMap<Id<Person>, Coord> getPersonId2Coordinates(Population population, String activity) {
        SortedMap<Id<Person>, Coord> personId2coord = new TreeMap<>();

        for (Person person : population.getPersons().values()) {

            for (PlanElement pE : person.getSelectedPlan().getPlanElements()) {

                if (pE instanceof Activity) {
                    Activity act = (Activity) pE;

                    if (act.getType().equals(activity) || activity == null) {

                        Coord coord = act.getCoord();
                        personId2coord.put(person.getId(), coord);

                    } else {
                        //  other activity type
                    }
                }
            }
        }
        return personId2coord;
    }


}
