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
package sandbox.sfwatergit.analysis.stats.congestion;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.events.algorithms.Vehicle2DriverEventHandler;
import org.matsim.core.gbl.Gbl;
import org.matsim.vehicles.VehicleType;

import java.util.*;

/**
 * @author amit
 */
public class ExperiencedDelayHandler implements LinkEnterEventHandler, LinkLeaveEventHandler,
        PersonDepartureEventHandler, PersonArrivalEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler, TransitDriverStartsEventHandler {

    public final static Logger LOG = Logger.getLogger(ExperiencedDelayHandler.class);

    private final Vehicle2DriverEventHandler delegate = new Vehicle2DriverEventHandler();

    private final List<Id<Person>> transitDriverPersons = new ArrayList<>();

    private final SortedMap<Double, Map<Id<Person>, Double>> timebin2PersonId2Delay = new TreeMap<>();
    private final Map<Double, Map<Id<Link>, Double>> timebin2LinkId2Delay = new HashMap<>();
    private final Map<Id<Link>, Map<Id<Person>, Double>> linkId2PersonIdLinkEnterTime = new HashMap<>();
    private final Map<Id<Link>, Map<String, Double>> linkId2FreeSpeedLinkTravelTime = new HashMap<>();
    private final Map<Double, Map<Id<Link>, Integer>> timebin2LinkIdLeaveCount = new HashMap<>();
    private double totalDelay;
    private double warnCount = 0;


    private final Map<Id<Person>, String> personId2Mode = new HashMap<>();
    private final SortedMap<String, Double> mode2Speed = new TreeMap<>();
    Map<Id<Person>, Integer> nullPersonCountMap = new HashMap<>();
    private double timeBinSize;

    public ExperiencedDelayHandler(final Scenario scenario, final int noOfTimeBins) {
        initialize(scenario, noOfTimeBins);
    }

    private void initialize(final Scenario scenario, final int noOfTimeBins) {
        double simulationEndTime = scenario.getConfig().qsim().getEndTime();
        this.timeBinSize = simulationEndTime / noOfTimeBins;
        Network network = scenario.getNetwork();

        // Map each travel mode to its maximum velocity for a vehicle type
        for (VehicleType vt : scenario.getVehicles().getVehicleTypes().values()) {
            mode2Speed.put(vt.getId().toString(), vt.getMaximumVelocity());
        }

        // Add car mode with max speed of infinity if scenario uses default vehicles
        if (scenario.getConfig().qsim().getVehiclesSource().equals(QSimConfigGroup.VehiclesSource.defaultVehicle)){
            mode2Speed.put(TransportMode.car, Double.MAX_VALUE);
        }

        // If no vehicle types are available, only car travel is assumed on links
        // Set the maximum speed for car to infinity
        if (mode2Speed.isEmpty()) {
            mode2Speed.put(TransportMode.car, Double.MAX_VALUE);
        }

        // Operations on each link in the network
        for (Link link : network.getLinks().values()) {
            // Initialize the mapping for person ids as they enter the network
            this.linkId2PersonIdLinkEnterTime.put(link.getId(), new HashMap<>());

            Map<String, Double> mode2freeSpeedTime = new HashMap<>();
            for (String mode : mode2Speed.keySet()) { // for each available mode, find the freespeed travel time and add it to the map
                Double freeSpeedLinkTravelTime = Math.floor(link.getLength() / Math.min(link.getFreespeed(), mode2Speed.get(mode))) + 1; // Add 1 to ensure that we have nonzero travel times
                mode2freeSpeedTime.put(mode, freeSpeedLinkTravelTime);
            }
            this.linkId2FreeSpeedLinkTravelTime.put(link.getId(), mode2freeSpeedTime);
        }

        for (int i = 0; i < noOfTimeBins; i++) {
            this.timebin2PersonId2Delay.put(this.timeBinSize * (i + 1), new HashMap<>());
            this.timebin2LinkId2Delay.put(this.timeBinSize * (i + 1), new HashMap<>());
            this.timebin2LinkIdLeaveCount.put(this.timeBinSize * (i + 1), new HashMap<>());

            for (Person person : scenario.getPopulation().getPersons().values()) {
                Map<Id<Person>, Double> delayForPerson = this.timebin2PersonId2Delay.get(this.timeBinSize * (i + 1));
                delayForPerson.put(person.getId(), 0.);
            }

            for (Link link : network.getLinks().values()) {
                Map<Id<Link>, Double> delayOnLink = this.timebin2LinkId2Delay.get(this.timeBinSize * (i + 1));
                delayOnLink.put(link.getId(), 0.);
                Map<Id<Link>, Integer> countOnLink = this.timebin2LinkIdLeaveCount.get(this.timeBinSize * (i + 1));
                countOnLink.put(link.getId(), 0);
            }
        }
    }

    @Override
    public void reset(int iteration) {
        this.timebin2LinkId2Delay.clear();
        this.timebin2PersonId2Delay.clear();
        LOG.info("Resetting person delays to   " + this.timebin2PersonId2Delay);
        this.linkId2PersonIdLinkEnterTime.clear();
        this.linkId2FreeSpeedLinkTravelTime.clear();
        this.timebin2LinkIdLeaveCount.clear();
        this.transitDriverPersons.clear();
        LOG.info("Resetting linkLeave counter to " + this.timebin2LinkIdLeaveCount);
        this.personId2Mode.clear();
        LOG.warn("Total number of null persons" + nullPersonCountMap.size());
        nullPersonCountMap.forEach((personId, count) -> LOG.warn(String.format("ID: %s; Number of times: %s",personId.toString(),count.toString())));
        LOG.info("Resetting null person counter ");
        this.nullPersonCountMap.clear();
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        Id<Link> linkId = event.getLinkId();
        Id<Person> personId = event.getPersonId();

        if (this.transitDriverPersons.contains(personId)) return;

        personId2Mode.put(event.getPersonId(), event.getLegMode());

        if (this.linkId2PersonIdLinkEnterTime.get(linkId).containsKey(personId)) {
            // Person is already on the link. Cannot happen.
            throw new RuntimeException("Person is already on the link. Cannot happen.");
        }

        if (!mode2Speed.containsKey(event.getLegMode())) return;

        Map<Id<Person>, Double> personId2LinkEnterTime = this.linkId2PersonIdLinkEnterTime.get(linkId);
        double derivedLinkEnterTime = event.getTime() + 1 - this.linkId2FreeSpeedLinkTravelTime.get(linkId).get(event.getLegMode());
        personId2LinkEnterTime.put(personId, derivedLinkEnterTime);
        this.linkId2PersonIdLinkEnterTime.put(linkId, personId2LinkEnterTime);
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        Double time = event.getTime();
        if (time == 0.0) time = this.timeBinSize;
        double endOfTimeInterval = 0.0;
        endOfTimeInterval = Math.ceil(time / this.timeBinSize) * this.timeBinSize;
        if (endOfTimeInterval <= 0.0) endOfTimeInterval = this.timeBinSize;

        Id<Link> linkId = event.getLinkId();
        Id<Person> personId = delegate.getDriverOfVehicle(event.getVehicleId());

        if (this.transitDriverPersons.contains(personId)) return;

        double actualTravelTime = 0.0;
        final Map<Id<Person>, Double> personId2LinkEnterTime = this.linkId2PersonIdLinkEnterTime.get(linkId);
        final Double linkTravelTime = Optional.ofNullable(personId2LinkEnterTime.get(personId)).orElse(0.0);
        actualTravelTime = event.getTime() - linkTravelTime;

        personId2LinkEnterTime.remove(personId);
        final Map<String, Double> modeOnLink2TravelTime = this.linkId2FreeSpeedLinkTravelTime.get(linkId);
        double freeSpeedTime = Optional.ofNullable(modeOnLink2TravelTime.get(personId2Mode.get(personId))).orElse(0.0);

        double currentDelay = actualTravelTime - freeSpeedTime;
        if (currentDelay < 1.) currentDelay = 0.;
        this.totalDelay += currentDelay;

        Map<Id<Person>, Double> delayForPerson = this.timebin2PersonId2Delay.get(endOfTimeInterval);
        Map<Id<Link>, Double> delayOnLink = this.timebin2LinkId2Delay.get(endOfTimeInterval);
        Map<Id<Link>, Integer> countTotal = this.timebin2LinkIdLeaveCount.get(endOfTimeInterval);

        delayForPerson.put(personId, currentDelay + delayForPerson.get(personId));

        delayOnLink.put(linkId, currentDelay + delayOnLink.get(linkId));

        double countsSoFar = countTotal.get(linkId);
        double newValue = countsSoFar + 1.;
        countTotal.put(linkId, (int) newValue);
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        double time = event.getTime();
        Id<Link> linkId = event.getLinkId();
        Id<Person> personId = delegate.getDriverOfVehicle(event.getVehicleId());

        if (this.linkId2PersonIdLinkEnterTime.get(linkId).containsKey(personId) && warnCount == 0) {
            warnCount++;
            LOG.warn("Person " + personId + " is entering on link " + linkId + " two times without leaving from the same. "
                    + "Link enter times are " + this.linkId2PersonIdLinkEnterTime.get(linkId).get(personId) + " and " + time);
            LOG.warn("Reason might be : There is at least one teleport activity departing on the link (and thus derived link "
                    + "enter time) and later person is entering the link with main congested mode. In such cases, the old time will be replaced.");
            LOG.warn(Gbl.ONLYONCE);
        }

        Map<Id<Person>, Double> personId2LinkEnterTime = this.linkId2PersonIdLinkEnterTime.get(linkId);
        personId2LinkEnterTime.put(personId, time);
        this.linkId2PersonIdLinkEnterTime.put(linkId, personId2LinkEnterTime);
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if (this.transitDriverPersons.remove(event.getPersonId())) return;
        this.linkId2PersonIdLinkEnterTime.get(event.getLinkId()).remove(event.getPersonId());
    }

    public SortedMap<Double, Map<Id<Person>, Double>> getDelayPerPersonAndTimeInterval() {
        return this.timebin2PersonId2Delay;
    }

    public Map<Double, Map<Id<Link>, Double>> getDelayPerLinkAndTimeInterval() {
        return this.timebin2LinkId2Delay;
    }

    public double getTotalDelayInHours() {
        return this.totalDelay / 3600;
    }

    public Map<Double, Map<Id<Link>, Integer>> getTime2linkIdLeaveCount() {
        return this.timebin2LinkIdLeaveCount;
    }

    @Override
    public void handleEvent(VehicleLeavesTrafficEvent event) {
        this.delegate.handleEvent(event);
    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent event) {
        this.delegate.handleEvent(event);
    }

    @Override
    public void handleEvent(TransitDriverStartsEvent event) {
        transitDriverPersons.add(event.getDriverId());
    }
}
