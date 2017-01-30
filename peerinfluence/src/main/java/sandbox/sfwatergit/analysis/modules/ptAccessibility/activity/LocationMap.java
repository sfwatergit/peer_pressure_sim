/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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
package sandbox.sfwatergit.analysis.modules.ptAccessibility.activity;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;

import java.util.*;
import java.util.Map.Entry;


/**
 * @author droeder
 */
public class LocationMap {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(LocationMap.class);
    private Map<String, List<String>> activityCluser;

    private SortedMap<String, List<ActivityLocation>> type2locations;

    public LocationMap(Map<String, List<String>> activityCluster) {
        this.activityCluser = activityCluster;
        this.type2locations = new TreeMap<>();
        for (String s : activityCluster.keySet()) {
            this.type2locations.put(s, new ArrayList<>());
        }
        this.type2locations.put("unknown", new ArrayList<>());
    }

    public static void main(String[] args) {
        Map<String, List<String>> cluster = new HashMap<>();
        List<String> types = new ArrayList<>();

        types.add("1");
        types.add("2");
        cluster.put("a", types);

        types = new ArrayList<>();
        types.add("3");
        cluster.put("b", types);

        LocationMap map = new LocationMap(cluster);


        for (Entry<String, List<ActivityLocation>> e : map.getType2Locations().entrySet()) {
            System.out.println(e.getValue().size() + " ActivityLocations of Type " + e.getKey() + " at:");
            for (ActivityLocation l : e.getValue()) {
                System.out.println(l.getType() + " " + l.getCoord().toString());
            }
            System.out.println();
            System.out.println();
        }

    }

    public void addActivity(Activity activity, Id<Person> personId) {
        String type = "unknown";
        for (Entry<String, List<String>> e : this.activityCluser.entrySet()) {
            if (e.getValue().contains(activity.getType())) {
                type = e.getKey();
                break;
            }
        }
        ActivityLocation loc = new ActivityLocation(activity.getCoord(), type, personId);
        this.type2locations.get(type).add(loc);
    }

    public Map<String, List<ActivityLocation>> getType2Locations() {
        return this.type2locations;
    }

}

