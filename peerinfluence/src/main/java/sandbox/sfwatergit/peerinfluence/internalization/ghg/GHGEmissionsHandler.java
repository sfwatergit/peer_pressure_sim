package sandbox.sfwatergit.peerinfluence.internalization.ghg;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.emissions.events.WarmEmissionEvent;
import org.matsim.contrib.emissions.types.WarmPollutant;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.algorithms.Vehicle2DriverEventHandler;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.vehicles.Vehicle;
import sandbox.sfwatergit.utils.PlanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles mobility events and calculates emissions to pass to further event handlers.
 * <p>
 * Created by sidneyfeygin on 7/13/15.
 */
public class GHGEmissionsHandler implements LinkEnterEventHandler, LinkLeaveEventHandler,
        PersonArrivalEventHandler, PersonDepartureEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler {
    private static final Logger logger = Logger.getLogger(GHGEmissionsHandler.class);
    private final Network network;
    private final Scenario scenario;
    private final Map<Id<Person>, Tuple<Id<Link>, Double>> linkenter = new HashMap<>();
    private final Map<Id, Tuple<Id, Double>> agentarrival = new HashMap<>();
    private final Map<Id, Tuple<Id, Double>> agentdeparture = new HashMap<>();
    private EventsManager eventsManager;
    private int linkLeaveCnt = 0;
    private int linkLeaveFirstActWarnCnt = 0;
    private int linkLeaveSomeActWarnCnt = 0;
    private double freeFlowKmCounter = 0.0;
    private double stopGoKmCounter = 0.0;
    private double kmCounter = 0.0;

    private Vehicle2DriverEventHandler delegate = new Vehicle2DriverEventHandler() ;

    public GHGEmissionsHandler(
            final Network network,
            Scenario scenario, EventsManager eventsManager) {
        this.network = network;
        this.scenario = scenario;
        this.eventsManager = eventsManager;
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        Tuple<Id<Link>, Double> linkId2Time = new Tuple<Id<Link>, Double>(event.getLinkId(), event.getTime());
        this.linkenter.put(delegate.getDriverOfVehicle(event.getVehicleId()), linkId2Time);
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        linkLeaveCnt++;
        Id<Person> personId= delegate.getDriverOfVehicle(event.getVehicleId());
        Id<Link> linkId = event.getLinkId();
        Double leaveTime = event.getTime();
        Link link = this.network.getLinks().get(linkId);
        Double linkLength = link.getLength();
        Double freeVelocity = link.getFreespeed();
        if(PlanUtils.getPerson(scenario,personId)==null){
            return;
        }
        if (!this.linkenter.containsKey(personId)) {
            int maxLinkLeaveFirstActWarnCnt = 3;
            if (linkLeaveFirstActWarnCnt < maxLinkLeaveFirstActWarnCnt) {
                logger.info("Person " + personId + " is ending its first activity of the day and leaving link " + linkId + " without having entered.");
                logger.info("This is because of the MATSim logic that there is no link enter event for the link of the first activity");
                logger.info("Thus, no emissions are calculated for this link leave event.");
                if (linkLeaveFirstActWarnCnt == maxLinkLeaveFirstActWarnCnt) logger.warn(Gbl.FUTURE_SUPPRESSED);
            }
            linkLeaveFirstActWarnCnt++;
        } else if (!this.linkenter.get(personId).getFirst().equals(linkId)) {
            int maxLinkLeaveSomeActWarnCnt = 3;
            if (linkLeaveSomeActWarnCnt < maxLinkLeaveSomeActWarnCnt) {
                logger.warn("Person " + personId + " is ending an activity other than the first and leaving link " + linkId + " without having entered.");
                logger.warn("This indicates that there is some inconsistency in vehicle use; please check your inital plans file for consistency.");
                logger.warn("Thus, no emissions are calculated neither for this link leave event nor for the last link that was entered.");
                if (linkLeaveSomeActWarnCnt == maxLinkLeaveSomeActWarnCnt) logger.warn(Gbl.FUTURE_SUPPRESSED);
            }
            linkLeaveSomeActWarnCnt++;
        } else {
            double enterTime = this.linkenter.get(personId).getSecond();
            double travelTime;
            if (!this.agentarrival.containsKey(personId) || !this.agentdeparture.containsKey(personId)) {
                travelTime = leaveTime - enterTime;
            } else if (!this.agentarrival.get(personId).getFirst().equals(event.getLinkId())
                    || !this.agentdeparture.get(personId).getFirst().equals(event.getLinkId())) {

                travelTime = leaveTime - enterTime;
            } else {
                double arrivalTime = this.agentarrival.get(personId).getSecond();
                double departureTime = this.agentdeparture.get(personId).getSecond();
                travelTime = leaveTime - enterTime - departureTime + arrivalTime;
            }

            Id<Vehicle> vehicleId = Id.create(personId, Vehicle.class);

            // FIXME: Ignoring vehicle type for now... we'll see if this is relevant
            // -SAF '07 2015 (SmartBay Note)

            Map<WarmPollutant, Double> warmEmissions = calculateCO2Emission(
                    travelTime,
                    freeVelocity,
                    linkLength);
            Event warmEmissionEvent = new WarmEmissionEvent(enterTime, linkId, vehicleId, warmEmissions);
            this.eventsManager.processEvent(warmEmissionEvent);

        }
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if (!event.getLegMode().equals("car")) { // link travel time calculation not neccessary for other modes
            return;
        }
        Tuple<Id, Double> linkId2Time = new Tuple<>(event.getLinkId(), event.getTime());
        this.agentarrival.put(event.getPersonId(), linkId2Time);
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (!event.getLegMode().equals("car")) { // link travel time calculation not neccessary for other modes
            return;
        }
        Tuple<Id, Double> linkId2Time = new Tuple<>(event.getLinkId(), event.getTime());
        this.agentdeparture.put(event.getPersonId(), linkId2Time);
    }

    public int getLinkLeaveCnt() {
        return linkLeaveCnt;
    }

    public int getLinkLeaveWarnCnt() {
        return linkLeaveFirstActWarnCnt;
    }

    @Override
    public void reset(int iteration) {
        linkLeaveCnt = 0;
        linkLeaveFirstActWarnCnt = 0;

        linkenter.clear();
        agentarrival.clear();
        agentdeparture.clear();
        delegate.reset(iteration);

    }

    public Map<WarmPollutant, Double> calculateCO2Emission(
            Double travelTime,
            Double freeVelocity,
            Double linkLength) {


        Map<WarmPollutant, Double> co2Event = new HashMap<>();
        double linkLength_km = linkLength / 1000;
        double travelTime_h = travelTime / 3600;
        double freeFlowSpeed_kmh = freeVelocity * 3.6;
        double averageSpeed_kmh = linkLength_km / travelTime_h;
        double stopGoSpeedFromTable_kmh;
        double efFreeFlow_gpkm;
        double efStopGo_gpkm;

        double generatedEmissions;


        /**
         * These are from http://www.arb.ca.gov/emfac/2014/:
         *
         * EMFAC2014 (v1.0.7) Emission Rates
         *
         * Region Type: Air Basin
         * Region: San Francisco Bay Area
         * Calendar Year: 2015
         * Season: Annual
         * Vehicle Classification: EMFAC2011 Categories
         * Units: miles/day for VMT, g/mile for RUNEX, PMBW and PMTW
         *
         * Region	CalYr	VehClass	MdlYr	Speed	Fuel
         * San Francisco Bay Area	2015	LDA	Aggregated	55	GAS

         * Free Flow Data:
         *
         * Speed | CO2RUNEX
         * 55   | 276.28105
         *
         * Stop and go (10 mph) Data:
         *
         * Speed | CO2RUNEX
         * 10 | 757.47335
         *
         * Accessed Nov' 2015
         *
         * @author sfeygin
         * **/

        stopGoSpeedFromTable_kmh = 16.0934; // 10 mph to kmh
        efFreeFlow_gpkm = 171.768; // 276.28105 gpkm
        efStopGo_gpkm = 363.638; // 757.47335 gpkm

//        if(averageSpeed_kmh <= 0.0){
//            throw new RuntimeException("Average speed has been calculated to 0.0 or a negative value. Aborting...");
//        }
        if ((averageSpeed_kmh - freeFlowSpeed_kmh) > 1.0) {
            throw new RuntimeException("Average speed has been calculated to be greater than free flow speed; this might produce negative warm emissions. Aborting...");
        }
            /* NOTE: the following comparision does not make sense since HBEFA assumes free flow speeds to be different from speed limits.
             * For instance, for RUR/MW/80/Freeflow HBEFA assumes a free flow speed of 82.80 kmh.
			 * benjamin, amit 01'2014
			 * */
//			if(freeFlowSpeedFromTable_kmh - freeFlowSpeed_kmh > 1.0 || freeFlowSpeedFromTable_kmh - freeFlowSpeed_kmh <-1.0){
//				logger.warn("The given free flow speed does not match the table's value. Please check consistency of your scenario!");
//				logger.info("Using given speed value to avoid negative emission values...");
//			}

        if ((averageSpeed_kmh - freeFlowSpeed_kmh) >= -1.0) { // both speeds are assumed to be not very different > only FREEFLOW on link
            generatedEmissions = linkLength_km * efFreeFlow_gpkm;
            freeFlowKmCounter = freeFlowKmCounter + linkLength_km;
        } else if ((averageSpeed_kmh - stopGoSpeedFromTable_kmh) <= 0.0) { // averageSpeed is less than stopGoSpeed > only stop&go on link
            generatedEmissions = linkLength_km * efStopGo_gpkm;
            stopGoKmCounter = stopGoKmCounter + linkLength_km;
        } else {
            double distanceStopGo_km = (linkLength_km * stopGoSpeedFromTable_kmh * (freeFlowSpeed_kmh - averageSpeed_kmh)) / (averageSpeed_kmh * (freeFlowSpeed_kmh - stopGoSpeedFromTable_kmh));
            double distanceFreeFlow_km = linkLength_km - distanceStopGo_km;

            generatedEmissions = (distanceFreeFlow_km * efFreeFlow_gpkm) + (distanceStopGo_km * efStopGo_gpkm);
            stopGoKmCounter = stopGoKmCounter + distanceStopGo_km;
            freeFlowKmCounter = freeFlowKmCounter + distanceFreeFlow_km;
        }
        kmCounter = kmCounter + linkLength_km;
        co2Event.put(WarmPollutant.CO2_TOTAL, generatedEmissions);

        return co2Event;

    }


    @Override
    public void handleEvent(VehicleLeavesTrafficEvent event) {
        delegate.handleEvent(event);
    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent event) {
        delegate.handleEvent(event);
    }

}
