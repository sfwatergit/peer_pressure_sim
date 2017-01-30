/* *********************************************************************** *
 * project: org.matsim.*
 * Plans2ESRIShape.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.PointFeatureFactory;
import org.matsim.core.utils.gis.PolylineFeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.matsim.core.utils.gis.ShapeFileWriter.writeGeometries;

/**
 * Simple class to convert MATSim plans to ESRI shape files. Activities will be converted into points and
 * legs will be converted into line strings. Parameters as defined in the population xml file will be added
 * as attributes to the shape files. There are also some parameters to configure this converter, please
 * consider the corresponding setters in this class.
 *
 * @author laemmel
 * @author dgrether removed many "features" of original class  by laemmel
 */
public class DgSelectedPlans2ESRIShape {
    private final CoordinateReferenceSystem crs;
    private final String outputDir;
    private final GeometryFactory geofac;
    private final Network network;

    private PolylineFeatureFactory.Builder legBuilder;
    private Population population;
    private PolylineFeatureFactory pff;
    private PointFeatureFactory.Builder actBuilder;
    private PointFeatureFactory poff;

    public DgSelectedPlans2ESRIShape(final Population population, final Network network, final CoordinateReferenceSystem crs, final String outputDir) {
        this.population = population;
        this.network = network;
        this.crs = crs;
        this.outputDir = outputDir;
        this.geofac = new GeometryFactory();
        initFeatureType();
    }


    public void writeActs(String prefix) {
        String outputFile = this.outputDir + "/" + prefix + "acts.shp";
        ArrayList<SimpleFeature> fts = new ArrayList<>();
        for (Person p : this.population.getPersons().values()) {
            Plan plan = p.getSelectedPlan();
            String id = plan.getPerson().getId().toString();
            plan.getPlanElements().stream().filter(pe -> pe instanceof Activity).forEach(pe -> {
                Activity act = (Activity) pe;
                fts.add(getActFeature(id, act));
            });
        }

        writeGeometries(fts, outputFile);
    }

    public void writeLegs(String prefix) throws IOException {
        String outputFile = this.outputDir + "/" + prefix + "legs.shp";

        List<SimpleFeature> fts = Lists.newArrayList();
        for (Person p : this.population.getPersons().values()) {
            Plan plan = p.getSelectedPlan();
            String id = plan.getPerson().getId().toString();
            plan.getPlanElements().stream().filter(pe -> pe instanceof Leg).forEach(pe -> {
                Leg leg = (Leg) pe;
                if (leg.getRoute() instanceof NetworkRoute) {
                    if (RouteUtils.calcDistance((NetworkRoute) leg.getRoute(), 0.0,0.0, this.network) > 0) {
                        final SimpleFeature legFeature = getLegFeature(leg, id);
                        if (legFeature != null) {
                            fts.add(legFeature);
                        }
                    }
                } else if (leg.getRoute().getDistance() > 0) {
                    final SimpleFeature legFeature = getLegFeature(leg, id);
                    if (legFeature != null) {
                        fts.add(legFeature);
                    }
                }
            });
        }


        writeGeometries(fts, outputFile);
    }

    private SimpleFeature getActFeature(final String id, final Activity act) {
        String type = act.getType();
//        String linkId = act.getLinkId().toString();
        final Coord coord = act.getCoord();
        Double startTime = act.getStartTime();
        Double endTime = act.getEndTime();
        try {
            return this.poff.createPoint(MGC.coord2Point(coord), new Object[]{type, startTime, endTime}, id);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }

    private SimpleFeature getLegFeature(final Leg leg, final String id) {
        if (!(leg.getRoute() instanceof NetworkRoute)) {
            return null;
        }
        String mode = leg.getMode();
        Double depTime = leg.getDepartureTime();
        Double travTime = leg.getTravelTime();
        Double dist = RouteUtils.calcDistance((NetworkRoute) leg.getRoute(),0.0,0.0, this.network);

        List<Id<Link>> linkIds = ((NetworkRoute) leg.getRoute()).getLinkIds();
        Coordinate[] coords = new Coordinate[linkIds.size() + 1];
        for (int i = 0; i < linkIds.size(); i++) {
            Link link = this.network.getLinks().get(linkIds.get(i));
            Coord c = link.getFromNode().getCoord();
            Coordinate cc = new Coordinate(c.getX(), c.getY());
            coords[i] = cc;
        }

        Link link = this.network.getLinks().get(linkIds.get(linkIds.size() - 1));
        Coord c = link.getToNode().getCoord();
        Coordinate cc = new Coordinate(c.getX(), c.getY());
        coords[linkIds.size()] = cc;

        LineString ls = this.geofac.createLineString(coords);

        try {
            return pff.createPolyline(ls, new Object[]{Integer.parseInt(id), mode, depTime, travTime, dist}, id);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void initFeatureType() {
        actBuilder = new PointFeatureFactory.Builder();
        actBuilder.setName("activity");

        actBuilder.setCrs(this.crs);
        actBuilder.addAttribute("PERS_ID", String.class);
        actBuilder.addAttribute("location", Point.class);
        actBuilder.addAttribute("TYPE", String.class);
        actBuilder.addAttribute("START_TIME", Double.class);
        actBuilder.addAttribute("END_TIME", Double.class);
        poff = actBuilder.create();

        legBuilder = new PolylineFeatureFactory.Builder();
        legBuilder.setName("leg");
        legBuilder.setCrs(this.crs);
        legBuilder.addAttribute("PERS_ID", Integer.class);
        legBuilder.addAttribute("MODE", String.class);
        legBuilder.addAttribute("DEP_TIME", Double.class);
        legBuilder.addAttribute("TRAV_TIME", Double.class);
        legBuilder.addAttribute("DIST", Double.class);


        pff = legBuilder.create();


    }


}

