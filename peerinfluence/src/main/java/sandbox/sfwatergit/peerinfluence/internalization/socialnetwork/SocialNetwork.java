package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.MultiGraph;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.attribute.AttributeKind;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

import java.util.Map;

/**
 * SocialNetwork interface
 * <p>
 * Created by sidneyfeygin on 5/12/15.
 */
public interface SocialNetwork extends DirectedGraph<SocialVertex, SocialEdge>, MultiGraph<SocialVertex, SocialEdge> {

    String ELEMENT_NAME = "socialnetworkmodule";


    Map<Id<Person>, SocialVertex> getSocialVertices();

    void addRelationship(SocialVertex person1, SocialVertex person2, Double tieStrength);

    void setAttribute(AttributeKind socialAttributeKind, Double value);

    Hypergraph<SocialVertex, SocialEdge> getGraph();

    void addSocialVertex(SocialVertex socialVertex);

    boolean isEmpty();
}
