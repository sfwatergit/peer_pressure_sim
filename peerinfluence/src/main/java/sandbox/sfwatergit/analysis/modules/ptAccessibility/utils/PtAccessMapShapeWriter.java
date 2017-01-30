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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.opengis.feature.simple.SimpleFeature;
import sandbox.sfwatergit.analysis.modules.ptAccessibility.stops.PtStopMap;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Calculates and writes buffers to shapes
 *
 * @author aneumann
 */
public class PtAccessMapShapeWriter {

    private PtAccessMapShapeWriter() {

    }

    public static void writeAccessMap(Map<String, Map<String, MultiPolygon>> cluster2mode2area, int quadrantSegments, String outputFolder, String targetCoordinateSystem) {
        // Sort distance clusters
        ArrayList<Integer> distancesSmallestFirst = cluster2mode2area.keySet().stream().map(Integer::parseInt).sorted(Comparator.naturalOrder()).collect(Collectors.toCollection(ArrayList::new));

        HashMap<Integer, HashMap<String, Geometry>> distance2mode2buffer = new HashMap<>();

        // Calculate buffer for all Multipolygons
        HashMap<String, Geometry> mode2buffer = null;
        int lastDistance = 0;

        for (Integer distance : distancesSmallestFirst) {
            if (mode2buffer == null) {
                // it's the frist and smallest one
                mode2buffer = new HashMap<>();
                for (Entry<String, MultiPolygon> multipolygonEntry : cluster2mode2area.get(distancesSmallestFirst.get(0).toString()).entrySet()) {
                    mode2buffer.put(multipolygonEntry.getKey(), multipolygonEntry.getValue().buffer(0.0, quadrantSegments));
                    lastDistance = distance;
                }
            } else {
                HashMap<String, Geometry> tempBuffers = new HashMap<>();
                for (Entry<String, Geometry> bufferEntry : mode2buffer.entrySet()) {
                    tempBuffers.put(bufferEntry.getKey(), bufferEntry.getValue().buffer(distance - lastDistance));
                }
                mode2buffer = tempBuffers;
                lastDistance = distance;
            }

            distance2mode2buffer.put(distance, mode2buffer);
        }

        writeGeometries(outputFolder + PtStopMap.FILESUFFIX + "_buffer", distance2mode2buffer, targetCoordinateSystem);


        // resort distances - largest first
        ArrayList<Integer> distancesLargestFirst = new ArrayList<>();
        for (Integer distance : distancesSmallestFirst) {
            distancesLargestFirst.add(0, distance);
        }


        HashMap<Integer, HashMap<String, Geometry>> distance2mode2diffBuffer = new HashMap<>();
        HashMap<String, Geometry> lastMode2Buffer = null;
        Integer lastDist = null;

        // calculate Diff for all buffers
        for (Integer distance : distancesLargestFirst) {
            distance2mode2diffBuffer.put(distance, new HashMap<>());

            if (lastMode2Buffer == null) {
                lastMode2Buffer = distance2mode2buffer.get(distance);
                lastDist = distance;
            } else {
                // diff
                for (String mode : distance2mode2buffer.get(distance).keySet()) {
                    Geometry diffBuffer = lastMode2Buffer.get(mode).difference(distance2mode2buffer.get(distance).get(mode));
                    distance2mode2diffBuffer.get(lastDist).put(mode, diffBuffer);
                }
                lastMode2Buffer = distance2mode2buffer.get(distance);
                lastDist = distance;
            }
        }

        // add last (smallest) one as well
        if (lastMode2Buffer != null) {
            for (Entry<String, Geometry> mode2BufferEntry : lastMode2Buffer.entrySet()) {
                distance2mode2diffBuffer.get(lastDist).put(mode2BufferEntry.getKey(), mode2BufferEntry.getValue());
            }
        }

        writeGeometries(outputFolder + PtStopMap.FILESUFFIX + "_diffBuffer", distance2mode2diffBuffer, targetCoordinateSystem);
    }

    private static void writeGeometries(String outputFolderAndFileName, HashMap<Integer, HashMap<String, Geometry>> distance2mode2buffer, String targetCoordinateSystem) {
        // toFile all to file
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setCRS(MGC.getCRS(targetCoordinateSystem));
        b.setName("name");
        b.add("location", MultiPolygon.class);
        b.add("mode", String.class);
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(b.buildFeatureType());

        Collection<SimpleFeature> bufferFeatures;
        Object[] bufferFeatureAttribs;

        for (Entry<Integer, HashMap<String, Geometry>> distance2mode2bufferEntry : distance2mode2buffer.entrySet()) {
            bufferFeatures = new ArrayList<>();
            HashMap<String, Geometry> mode2buffer = distance2mode2bufferEntry.getValue();
            for (Entry<String, Geometry> mode2BufferEntry : mode2buffer.entrySet()) {
                bufferFeatureAttribs = new Object[2];
                bufferFeatureAttribs[0] = mode2BufferEntry.getValue();
                bufferFeatureAttribs[1] = mode2BufferEntry.getKey();
                try {
                    bufferFeatures.add(builder.buildFeature(null, bufferFeatureAttribs));
                } catch (IllegalArgumentException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            try {
                ShapeFileWriter.writeGeometries(bufferFeatures, outputFolderAndFileName + "_" + distance2mode2bufferEntry.getKey() + ".shp");
            } catch (ServiceConfigurationError e) {
                e.printStackTrace();
            }
        }

    }
}
