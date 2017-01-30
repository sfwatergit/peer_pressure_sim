package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.jung.factories;

import com.google.inject.Provider;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.graph.SparseGraph;
import org.apache.commons.collections15.Factory;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sidneyfeygin on 1/14/16.
 */
public class ScaleFreeSocialNetworkFactory extends SocialNetworkFactory implements Provider<SocialNetwork> {


    private Factory<SocialVertex> socialVertexFactory;
    private Factory<SocialEdge> socialEdgeFactory;
    private int numPeople;
    private int numRel;


    public ScaleFreeSocialNetworkFactory(Factory<SocialVertex> socialVertexFactory, Factory<SocialEdge> socialEdgeFactory, int numPeople, int numRel) {
        super(socialVertexFactory, socialEdgeFactory, numPeople);
        this.socialVertexFactory = socialVertexFactory;
        this.socialEdgeFactory = socialEdgeFactory;
        this.numPeople = numPeople;
        this.numRel = numRel;
    }

    @Override
    public SocialNetwork create() {
        int numFriendsPerAgent = numPeople*numRel/(2*numPeople-numRel);

        Set<SocialVertex> initialAgents = new HashSet<>();

        BarabasiAlbertGenerator<SocialVertex,SocialEdge> generator = new BarabasiAlbertGenerator<>(SparseGraph::new, socialVertexFactory, socialEdgeFactory, numFriendsPerAgent, numFriendsPerAgent, initialAgents);
        int numAgentsToAdd = numPeople- numFriendsPerAgent;
        generator.evolveGraph(numAgentsToAdd);
        return (SocialNetwork) generator.create();
    }


    @Override
    public SocialNetwork get() {
        return create();
    }
}
