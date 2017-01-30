package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.GraphDecorator;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.MultiGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.attribute.AttributeKind;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.attribute.SocialAttributes;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Purpose of Class:
 * <p>
 * A thin wrapper over a directed JUNG {@link MultiGraph} with {@link SocialNetwork}
 * attributes and methods.
 *
 * TODO: Remove Jung dependency
 * <p>
 * (Jung methods delegated through {@link GraphDecorator}
 * <p>
 * <p>
 * Date: 3/12/15
 * Time: 9:55 PM
 * Version: 1.0
 *
 * @author sidneyfeygin
 */
public class SocialNetworkImpl extends GraphDecorator<SocialVertex, SocialEdge> implements SocialNetwork {

    private Map<Id<Person>, SocialVertex> socialVertices;

    private SocialAttributes socialAttributes = new SocialAttributes();


    public SocialNetworkImpl(Graph<SocialVertex, SocialEdge> graph) {
        super(graph);
    }

    @Override
    public void setAttribute(AttributeKind socialAttributeKind, Double value) {
        this.socialAttributes.setValue(AttributeKind.SOCIAL_NORM, value);
    }


    @Override
    public Map<Id<Person>, SocialVertex> getSocialVertices() {
        if (socialVertices == null) {
            socialVertices = getVertices().stream().collect(Collectors.toMap(SocialVertex::getId, v -> v));
        }
        return socialVertices;
    }

    public void addRelationship(SocialVertex person1, SocialVertex person2, Double tieStrength) {
        addEdge(new SocialEdge(person1, person2, tieStrength), person1, person2, EdgeType.DIRECTED);
    }

    public Hypergraph<SocialVertex, SocialEdge> getGraph() {
        return this.delegate;
    }


    @Override
    public void addSocialVertex(SocialVertex socialVertex) {
        delegate.addVertex(socialVertex);
    }

    @Override
    public boolean isEmpty() {
        return getSocialVertices().isEmpty();
    }


}

