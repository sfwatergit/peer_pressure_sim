/* *********************************************************************** *
 * project: org.matsim.*
 * Population2Coordinates.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import sandbox.sfwatergit.PeerPressureConstants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author sfeygin
 */
public class Population2Coordinates {


    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        PopulationReader reader = new PopulationReader(scenario);
        reader.readFile("/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/input/sf_bay/population/matsim_plans_spark_UTM.xml");
        Set<PersonData> points = getPoints(scenario.getPopulation());
        writePoints(points, PeerPressureConstants.getOutDir.apply("sf_bay/plans.csv"));

    }

    private static Set<PersonData> getPoints(Population population) {
        Set<PersonData> points = new HashSet<>();

        for (Person person : population.getPersons().values()) {
            Plan plan = person.getPlans().get(0);
            Activity act = (Activity) plan.getPlanElements().get(0);
            Coord c = act.getCoord();
            String id = plan.getPerson().getId().toString();
            String type = act.getType();
            String startTime = String.valueOf(act.getStartTime());
            String endTime = String.valueOf(act.getEndTime());
            Coordinate coordinate = new Coordinate(c.getX(), c.getY());
            PersonData personData = new PersonData(id, type, startTime, endTime, String.valueOf(coordinate.x), String.valueOf(coordinate.y));
            points.add(personData);
        }

        return points;
    }


    private static void writePoints(Set<PersonData> points, String file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("id\tx\ty\ttype\tstartTime\tendTime");
        writer.newLine();
        for (PersonData p : points) {
            writer.write(String.valueOf(p.getId()));
            writer.write("\t");
            writer.write(p.getActivityType());
            writer.write("\t");
            writer.write(String.valueOf(p.getxCoord()));
            writer.write("\t");
            writer.write(String.valueOf(p.getyCoord()));
            writer.write("\t");
            writer.write(String.valueOf(p.getStartTime()));
            writer.write("\t");
            writer.write(p.getEndTime());
            writer.newLine();
        }
        writer.close();
    }

    public static class PersonData {
        private String id;
        private String activityType;
        private String startTime;
        private String endTime;
        private String xCoord;
        private String yCoord;

        PersonData(String id, String activityType, String startTime, String endTime, String xCoord, String yCoord) {
            this.id = id;
            this.activityType = activityType;
            this.startTime = startTime;
            this.endTime = endTime;
            this.xCoord = xCoord;
            this.yCoord = yCoord;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        String getxCoord() {
            return xCoord;
        }


        String getyCoord() {
            return yCoord;
        }

        String getActivityType() {
            return activityType;
        }

    }
}
