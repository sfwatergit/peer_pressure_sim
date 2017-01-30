/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package sandbox.sfwatergit.analysis.modules.legMode;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.*;
import sandbox.sfwatergit.analysis.math.MathUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate Modal Share from last iteration plans file and events file.
 *
 * @author amit
 */
public final class ModalShareUtils {


    private ModalShareUtils() {
    }

    private static final Logger logger = Logger.getLogger(ModalShareUtils.class);


    private static Function<Map<String, Long>, Long> numLegsTotaler = mode2NoOfLegs ->
            mode2NoOfLegs.keySet().stream()
                    .map(mode2NoOfLegs::get)
                    .reduce(0L, (BinaryOperator<Long>) (aLong, aLong2) -> aLong + aLong2);


    // Spagett! (Spooked ya!)
    private static Function<Collection<? extends Person>, SortedMap<String, Long>> numLegsPerMode = pop -> getUsedModes(pop).stream().collect(toTreeMap(
            k -> k,
            v -> Stream.of(pop).flatMap(c -> c.stream().flatMap(p ->
                    p.getSelectedPlan()
                    .getPlanElements().stream()
                            .filter(pe -> pe instanceof Leg)
                            .map(Leg.class::cast)
                            .filter(leg -> leg.getMode().equals(v))))
                    .count()
    ));

    private static <T, K, U>
    Collector<T, ?, TreeMap<K, U>> toTreeMap(Function<? super T, ? extends K> keyMapper,
                                             Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), TreeMap::new);
    }

    /**
     * Returns a merge function, suitable for use in
     * {@link Map#merge(Object, Object, BiFunction) Map.merge()} or
     * , which always
     * throws {@code IllegalStateException}.  This can be used to enforce the
     * assumption that the elements being collected are distinct.
     *
     * @param <T> the type of input arguments to the merge function
     * @return a merge function which always throw {@code IllegalStateException}
     */
    private static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

    public static SortedMap<String, Double> getMode2PctShareFromPlans(Population population) {

        logger.info("=====Modal split is calculated using input plans file.=====");

        Map<String, Long> legsPerMode = numLegsPerMode.apply(population.getPersons().values());
        long totalNoOfLegs = numLegsTotaler.apply(legsPerMode);

        return legsPerMode.keySet().stream().collect(toTreeMap(
                k -> k,
                v -> MathUtils.roundDouble(100 * legsPerMode.get(v) / (double) totalNoOfLegs, 3)
        ));
    }

    private static SortedSet<String> getUsedModes(Collection<? extends Person> pop) {
        SortedSet<String> usedModes = new TreeSet<>();
        for (Person person : pop) {
            Plan plan = person.getSelectedPlan();
            List<PlanElement> planElements = plan.getPlanElements();
            planElements.stream().filter(planElement -> planElement instanceof Leg).forEach(planElement -> {
                Leg element = (Leg) planElement;
                final String legMode = element.getMode();
                if (!usedModes.contains(legMode)) {
                    usedModes.add(legMode);
                }
            });
        }
        return usedModes;
    }
}

