//package sandbox.sfwatergit.analysis.runtime;
//
//import com.google.common.collect.ImmutableSet;
//import com.google.common.collect.Sets;
//import sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm.PressurePersonData;
//import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
//import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.relationship.PressureRelationship;
//import sandbox.sfwatergit.peerinfluence.travelTime.charts.PressurePerson;
//import sandbox.sfwatergit.utils.PlanUtils;
//import org.matsim.api.core.v01.Coord;
//import org.matsim.api.core.v01.Id;
//import org.matsim.api.core.v01.population.Person;
//import org.matsim.api.core.v01.population.Population;
//import sandbox.sfwatergit.utils.gexf.*;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
///**
// *  Creates Gephi file w/ pressure data
// *
// * Created by sidneyfeygin on 8/24/15.
// */
//public class PressureDiagram {
//
//    private ObjectFactory gexfFactory;
//    private XMLGexfContent gexfContent;
//    private Map<Id<Person>,PressurePersonData> pressurePersonData;
//
//    public PressureDiagram() {
//        this.gexfContent = new XMLGexfContent();
//        this.gexfFactory = new ObjectFactory();
//        XMLGraphContent graph = this.gexfFactory.createXMLGraphContent();
//        graph.setDefaultedgetype(XMLDefaultedgetypeType.DIRECTED);
//        graph.setIdtype(XMLIdtypeType.STRING);
//        graph.setTimeformat(XMLTimeformatType.INTEGER);
//        this.gexfContent.setGraph(graph);
//    }
//
//    public void usePressureData(Map<Integer,List<PressurePersonData>> pressureData, int iteration, Population population, SocialNetwork socialNetwork, String outname){
//        final List<PressurePersonData> pressureDatas = pressureData.get(iteration);
//        Set<Id<Person>> personsInPressureDatas = Sets.newHashSet();
//        for (PressurePersonData data : pressureDatas) {
//            personsInPressureDatas.add(data.getId());
//            final PressurePersonData egoPressureData = data.getEgoPressureData();
//            final Map<Id<Person>, Double> alters = data.getAlterPressureMap();
//            final PressurePerson egoPerson = new PressurePerson(egoPressureData);
//            for (Map.Entry<Id<Person>, Double> entry : alters.entrySet()) {
//                final Id<Person> id = entry.getKey();
//                personsInPressureDatas.add(id);
//                final Person person = population.getPersons().get(id);
//                final String mode = PlanUtils.MAIN_MODE_IDENTIFIER.identifyMainMode(person.getSelectedPlan().getPlanElements());
//                final Coord home = PlanUtils.getPersonId2Coordinates(population, "home").get(id);
//                final PressurePerson alterPerson = new PressurePerson(id, PressurePerson.PersonType.ALTER,mode,entry.getValue(),home);
//                final PressureRelationship rel = new PressureRelationship(egoPerson, alterPerson, PressureRelationship.Direction.ALTER_EGO);
//                addRelationship(rel);
//            }
//
//        }
//
//        final Set<Id<Person>> popIds = population.getPersons().keySet();
//        final ImmutableSet<Id<Person>> notInPressureDatas = Sets.difference(popIds, personsInPressureDatas).immutableCopy();
//        for (Id<Person> notInPressureData : notInPressureDatas) {
//            final Person person = population.getPersons().get(notInPressureData);
//            final String mainMode = PlanUtils.MAIN_MODE_IDENTIFIER.identifyMainMode(person.getSelectedPlan().getPlanElements());
//            final Collection<Id<Person>> neighbors = socialNetwork.getNeighbors(notInPressureData);
//            PressurePerson nonPressurePerson = new PressurePerson(notInPressureData, PressurePerson.PersonType.EGO, mainMode, PlanUtils.getBestCarScore(person.getPlans()) - PlanUtils.getBestPtScore(person.getPlans()), PlanUtils.getPersonId2Coordinates(population,"home").get(notInPressureData));
//            for (Id<Person> neighbor : neighbors) {
//                final Person otherPerson = population.getPersons().get(neighbor);
//                final String otherMainMode = PlanUtils.MAIN_MODE_IDENTIFIER.identifyMainMode(otherPerson.getSelectedPlan().getPlanElements());
//                PressurePerson otherNonPressurePerson = new PressurePerson(neighbor, PressurePerson.PersonType.ALTER,otherMainMode, PlanUtils.getBestCarScore(otherPerson.getPlans()) - PlanUtils.getBestPtScore(otherPerson.getPlans()), PlanUtils.getPersonId2Coordinates(population,"home").get(notInPressureData));
//
//                PressureRelationship pressureRelationship = new PressureRelationship(nonPressurePerson, otherNonPressurePerson, PressureRelationship.Direction.ALTER_EGO);
//                addRelationship(pressureRelationship);
//            }
//
//        }
//
//        createGefx(outname);
//
//    }
//
//    public void createGefx(String filename){
//
//        XMLAttributesContent nodeAttributeContentsContainer = new XMLAttributesContent();
//        nodeAttributeContentsContainer.setClazz(XMLClassType.NODE);
//
//
//        XMLAttributeContent attributeContent = new XMLAttributeContent();
//        attributeContent.setId("utilityDifference");
//        attributeContent.setTitle("utility difference or cost to pressure");
//        attributeContent.setType(XMLAttrtypeType.DOUBLE);
//        nodeAttributeContentsContainer.getAttribute().add(attributeContent);
//
//        attributeContent = new XMLAttributeContent();
//        attributeContent.setId("mode");
//        attributeContent.setTitle("type of mode used");
//        attributeContent.setType(XMLAttrtypeType.STRING);
//        nodeAttributeContentsContainer.getAttribute().add(attributeContent);
//
//        attributeContent = new XMLAttributeContent();
//        attributeContent.setId("personType");
//        attributeContent.setTitle("whether ego or alter");
//        attributeContent.setType(XMLAttrtypeType.STRING);
//        nodeAttributeContentsContainer.getAttribute().add(attributeContent);
//
//        List<Object> attr = this.gexfContent.getGraph().getAttributesOrNodesOrEdges();
//        XMLNodesContent nodes = this.gexfFactory.createXMLNodesContent();
//        attr.add(nodes);
//
//        XMLEdgesContent edges = this.gexfFactory.createXMLEdgesContent();
//        attr.add(edges);
//
//        List<XMLEdgeContent> edgeList = edges.getEdge();
//        List<XMLNodeContent> nodeList = nodes.getNode();
//
//        this.gexfContent.getGraph().getAttributesOrNodesOrEdges().add(nodeAttributeContentsContainer);
//
//        for (PressureRelationship rel : rels){
//            final PressurePerson source = rel.getSource();
//            XMLNodeContent n = source.getXMLNodeContent();
//            n.setId(source.getId().toString());
//            nodeList.add(n);
//
//            final PressurePerson target = rel.getTarget();
//            XMLNodeContent n2 = target.getXMLNodeContent();
//            n2.setId(target.getId().toString());
//
//            nodeList.add(n2);
//
//            XMLEdgeContent e = this.createEdge(rel);
//            e.setWeight(rel.getWeight());
//            edgeList.add(e);
//        }
//
//
//
//        new GexfWriter(this.gexfContent).toFile(filename);
//
//
//    }
//
//    private XMLEdgeContent createEdge(PressureRelationship relationship){
//        return relationship.getEdgeContent();
//    }
//
//    public void addRelationship(PressureRelationship pr) {
//        this.rels.add(pr);
//    }
//
//
//}
