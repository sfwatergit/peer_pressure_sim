/* *********************************************************************** *
 * project: org.matsim.*
 * DgPopulation2ShapeWriter
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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
package sandbox.sfwatergit.analysis.gis;

import com.vividsolutions.jts.geom.Coordinate;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.PointFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.opengis.feature.simple.SimpleFeature;

import java.util.ArrayList;
import java.util.List;


/**
 * @author dgrether
 */
public class DgPopulation2ShapeWriter {


    private Population pop;


    public DgPopulation2ShapeWriter(Population pop) {
        this.pop = pop;
    }

    public void write(String activityType, String filename) {
        try {

            PointFeatureFactory factory = new PointFeatureFactory.Builder().
                    setName("activity").
                    addAttribute("person_id", String.class).
                    addAttribute("type", String.class).
                    addAttribute("start_time", Double.class).
                    addAttribute("end_time", Double.class).
                    create();

            List<SimpleFeature> features = new ArrayList<>();
            SimpleFeature f = null;
            for (Person p : this.pop.getPersons().values()) {
                Plan plan = p.getSelectedPlan();
                for (PlanElement pe : plan.getPlanElements()) {
                    if (pe instanceof Activity) {
                        Activity activity = (Activity) pe;
                        if (activity.getType().compareTo(activityType) == 0) {

                            String id = p.getId().toString();
                            String type = activity.getType();
                            Double startTime = activity.getStartTime();
                            Double endTime = activity.getEndTime();

                            Coordinate actCoordinate = MGC.coord2Coordinate(activity.getCoord());


                            f = factory.createPoint(actCoordinate, new Object[]{id, type, startTime, endTime}, null);
                            features.add(f);
                        }
                    }
                }
            }

            ShapeFileWriter.writeGeometries(features, filename);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

}
