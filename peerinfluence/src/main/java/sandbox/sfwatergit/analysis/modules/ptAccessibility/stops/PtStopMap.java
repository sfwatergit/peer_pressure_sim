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
package sandbox.sfwatergit.analysis.modules.ptAccessibility.stops;

import com.vividsolutions.jts.geom.*;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author droeder
 */
public class PtStopMap {

    public static final String FILESUFFIX = "AccessMap";

    private GeometryFactory f;

    private HashMap<String, Map<String, Polygon>> map;

    private Map<String, Circle> cluster;

    private String mode;

    public PtStopMap(String mode, Map<String, Circle> cluster) {
        this.f = new GeometryFactory();
        this.mode = mode;

        this.map = new HashMap<>();
        for (String s : cluster.keySet()) {
            this.map.put(s, new HashMap<>());
        }
        this.cluster = cluster;
    }

    /**
     * Takes one transit stop facility and adds it to a cluster; creating the cluster in the process and adding it to the ptToStopMap
     *
     * @param stop {@link TransitStopFacility} to add to mapping
     */
    public void addStop(TransitStopFacility stop) {
        Polygon g;

        for (Entry<String, Map<String, Polygon>> e : this.map.entrySet()) {
            g = this.cluster.get(e.getKey()).createPolygon(this.f, stop.getCoord());
            e.getValue().put(stop.getId().toString(), g);
        }
    }

    /**
     * @return the mapping of distances to clusters
     */
    public Map<String, MultiPolygon> getCluster() {
        Polygon[] p;
        MultiPolygon mp;
        Map<String, MultiPolygon> cluster = new HashMap<>();

        for (Entry<String, Map<String, Polygon>> e : this.map.entrySet()) {
            p = e.getValue().values().toArray(new Polygon[e.getValue().size()]);
            mp = this.f.createMultiPolygon(p);
            cluster.put(e.getKey(), mp);
        }
        return cluster;
    }

    public String getMode() {
        return this.mode;
    }

    public boolean contains(Coordinate c, String cluster) {
        Point p = MGC.coordinate2Point(c);
        for (Geometry g : this.map.get(cluster).values()) {
            if (g.contains(p)) return true;
        }
        return false;
    }

}

