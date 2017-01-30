package sandbox.sfwatergit.analysis.gis;

import com.google.inject.Inject;
import com.vividsolutions.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.PointFeatureFactory;
import org.matsim.core.utils.gis.PolylineFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.opengis.feature.simple.SimpleFeature;
import sandbox.sfwatergit.PeerPressureConstants;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.PressureDataManager;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;
import sandbox.sfwatergit.utils.PlanUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * Created by sidneyfeygin on 3/5/16.
 */
public class PressureShapeWriter implements IterationEndsListener {
    private final Set<Map<Id<Person>,Set<Id<Person>>>> pressureRelSet = new HashSet<>();
    private final Scenario scenario;
    private final PressureDataManager pressureDataManager;
    private final OutputDirectoryHierarchy controlerIo;
    private final PointFeatureFactory pointFeatureFactory;
    private final PolylineFeatureFactory polyLineFeatureFactory;
    private final SortedMap<Id<Person>, Coord> personId2Coordinates;
    private final SocialNetwork socialNetwork;
    private Map<Id<Person>, SimpleFeature> people;
    private Map<String, SimpleFeature> rels;

    @Inject
    public PressureShapeWriter(Scenario scenario, PressureDataManager pressureDataManager, OutputDirectoryHierarchy controlerIo){
        this.scenario=scenario;
        this.pressureDataManager = pressureDataManager;
        this.controlerIo = controlerIo;
        this.socialNetwork=(SocialNetwork)scenario.getScenarioElement(SocialNetwork.ELEMENT_NAME);
        final String homeActivityName = scenario.getConfig().planCalcScore().getActivityTypes().stream().filter(a -> a.startsWith("h")).findFirst().get();
        personId2Coordinates = PlanUtils.getPersonId2Coordinates(scenario.getPopulation(), homeActivityName);
        pointFeatureFactory = new PointFeatureFactory.Builder()
                .setCrs(MGC.getCRS(PeerPressureConstants.SC_CRS))
                .setName("HomeLocations").create();
        polyLineFeatureFactory = new PolylineFeatureFactory.Builder()
                .addAttribute("PresAmt",Integer.class)
                .setCrs(MGC.getCRS(PeerPressureConstants.SC_CRS))
                .create();
        buildPersonPointLayer();
        buildRelationshipLayer();
    }

    private void buildPersonPointLayer(){
        people = socialNetwork.getVertices().stream()
                .collect(Collectors.toMap(
                        SocialVertex::getId,
                        c -> pointFeatureFactory.createPoint(MGC.coord2Coordinate(personId2Coordinates.get(c.getId())))));
    }

    private void buildRelationshipLayer(){
        Coordinate[] coords = new Coordinate[2];
        rels = socialNetwork.getEdges().stream().collect(Collectors.toMap(
                SocialEdge::toString,
                e -> {
                    coords[0] = MGC.coord2Coordinate(personId2Coordinates.get(e.getVertices().getFirst().getId()));
                    coords[1] = MGC.coord2Coordinate(personId2Coordinates.get(e.getVertices().getSecond().getId()));
                    return polyLineFeatureFactory.createPolyline(coords);
                }
        ));
    }


    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {

        final int iteration = event.getIteration();
        if(iteration%10==0) {
            ShapeFileWriter.writeGeometries(people.values(), controlerIo.getIterationFilename(iteration, "people.shp"));
            ShapeFileWriter.writeGeometries(rels.values(), controlerIo.getIterationFilename(iteration, "rels.shp"));
        }
    }
}
