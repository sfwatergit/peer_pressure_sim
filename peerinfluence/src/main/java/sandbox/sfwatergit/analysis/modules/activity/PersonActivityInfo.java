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
package sandbox.sfwatergit.analysis.modules.activity;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.utils.collections.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * @author amit
 */
public class PersonActivityInfo {

    private Id<Person> personId;
    private List<Tuple<String, Double>> actType2StartTimes;
    private List<Tuple<String, Double>> actType2EndTimes;


    public PersonActivityInfo(Id<Person> personId) {
        this.personId = personId;
        actType2EndTimes = new ArrayList<Tuple<String, Double>>();
        actType2StartTimes = new ArrayList<Tuple<String, Double>>();
    }

    public Id<Person> getPersonId() {
        return personId;
    }

    public void setPersonId(Id<Person> personId) {
        this.personId = personId;
    }

    public List<Tuple<String, Double>> getActType2StartTimes() {
        return actType2StartTimes;
    }

    public List<Tuple<String, Double>> getActType2EndTimes() {
        return actType2EndTimes;
    }


}
