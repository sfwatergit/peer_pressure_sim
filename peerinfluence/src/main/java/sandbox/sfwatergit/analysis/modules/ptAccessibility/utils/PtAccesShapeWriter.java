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
package sandbox.sfwatergit.analysis.modules.ptAccessibility.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.PointFeatureFactory;
import org.matsim.core.utils.gis.PolygonFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.opengis.feature.simple.SimpleFeature;
import sandbox.sfwatergit.analysis.modules.ptAccessibility.activity.ActivityLocation;
import sandbox.sfwatergit.analysis.modules.ptAccessibility.activity.LocationMap;
import sandbox.sfwatergit.analysis.modules.utils.GridNode;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author droeder, aneumann
 *         <p>
 *         just a helper-class
 */
public class PtAccesShapeWriter {

    private static final Logger log = Logger.getLogger(PtAccesShapeWriter.class);

    private PtAccesShapeWriter() {
    }

    public static void writeMultiPolygons(Map<String, MultiPolygon> mps, String filename, String name, String targetCoordinateSystem) {
        PolygonFeatureFactory factory = new PolygonFeatureFactory.Builder().
                setCrs(MGC.getCRS(targetCoordinateSystem)).
                setName(name).
                addAttribute("name", String.class).
                create();
        Collection<SimpleFeature> features = new ArrayList<SimpleFeature>();

        Object[] featureAttribs;
        for (Entry<String, MultiPolygon> e : mps.entrySet()) {
            featureAttribs = new Object[1];
            featureAttribs[0] = e.getKey();
            features.add(factory.createPolygon(e.getValue(), featureAttribs, null));
        }
        ShapeFileWriter.writeGeometries(features, filename);
    }

    public static void writeActivityLocations(LocationMap locationMap, String outputFolder, String name, String targetCoordinateSystem, double gridSize) {
        PointFeatureFactory featureFactory = new PointFeatureFactory.Builder().
                setCrs(MGC.getCRS(targetCoordinateSystem)).
                setName(name).
                addAttribute("personId", String.class).
                addAttribute("name", String.class).
                addAttribute("type", String.class).
                create();

        PointFeatureFactory clusterFeatureFactory = new PointFeatureFactory.Builder().
                setCrs(MGC.getCRS(targetCoordinateSystem)).
                setName(name).
                addAttribute("count", Integer.class).
                create();

        for (Entry<String, List<ActivityLocation>> type2LocationEntry : locationMap.getType2Locations().entrySet()) {
            if (!type2LocationEntry.getValue().isEmpty()) {
                try {
                    Collection<SimpleFeature> features = new ArrayList<SimpleFeature>();
                    for (int i = 0; i < type2LocationEntry.getValue().size(); i++) {
                        features.add(featureFactory.createPoint(type2LocationEntry.getValue().get(i).getCoord(), new Object[]{
                                type2LocationEntry.getValue().get(i).getPersonId(),
                                type2LocationEntry.getKey() + "_" + String.valueOf(i),
                                type2LocationEntry.getKey()
                        }, null));
                    }

                    ShapeFileWriter.writeGeometries(features, outputFolder + "activityLocations_" + type2LocationEntry.getKey() + ".shp");
                } catch (IllegalArgumentException | ServiceConfigurationError e) {
                    e.printStackTrace();
                }

                // cluster first toFile then

                try {
                    Collection<SimpleFeature> features = new ArrayList<SimpleFeature>();

                    HashMap<String, GridNode> gridNodeId2GridNode = new HashMap<String, GridNode>();

                    // go through all acts of this type
                    for (int i = 0; i < type2LocationEntry.getValue().size(); i++) {
                        Coord coord = new Coord(type2LocationEntry.getValue().get(i).getCoord().x, type2LocationEntry.getValue().get(i).getCoord().y);
                        String gridNodeId = GridNode.getGridNodeIdForCoord(coord, gridSize);

                        if (gridNodeId2GridNode.get(gridNodeId) == null) {
                            gridNodeId2GridNode.put(gridNodeId, new GridNode(gridNodeId));
                        }

                        gridNodeId2GridNode.get(gridNodeId).addPoint(type2LocationEntry.getKey(), coord);
                    }

                    features.addAll(gridNodeId2GridNode.values().stream().map(gridNode -> clusterFeatureFactory.createPoint(new Coordinate(gridNode.getX(), gridNode.getY()), new Object[]{gridNode.getCountForType(type2LocationEntry.getKey())}, null)).collect(Collectors.toList()));

                    ShapeFileWriter.writeGeometries(features, outputFolder + "activityLocations_clustered_" + type2LocationEntry.getKey() + ".shp");
                } catch (IllegalArgumentException | ServiceConfigurationError e) {
                    e.printStackTrace();
                }

            } else {
                log.info("No activities found for cluster " + type2LocationEntry.getKey());
            }
        }
    }

}

