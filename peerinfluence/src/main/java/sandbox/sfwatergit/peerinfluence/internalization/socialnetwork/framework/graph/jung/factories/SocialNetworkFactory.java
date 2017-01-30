package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.jung.factories;

import edu.uci.ics.jung.graph.Graph;
import org.apache.commons.collections15.Factory;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

/**
 * Created by sidneyfeygin on 1/14/16.
 */
abstract class SocialNetworkFactory implements Factory<Graph<SocialVertex,SocialEdge>> {

    private Factory<SocialVertex> socialVertexFactory;
    private Factory<SocialEdge> socialEdgeFactory;
    private int numPeople;

    SocialNetworkFactory(Factory<SocialVertex> socialVertexFactory,
                         Factory<SocialEdge> socialEdgeFactory,
                         int numPeople){


        this.socialVertexFactory = socialVertexFactory;
        this.socialEdgeFactory = socialEdgeFactory;
        this.numPeople = numPeople;
    }


    public Factory<SocialVertex> getSocialVertexFactory() {
        return socialVertexFactory;
    }

    public Factory<SocialEdge> getSocialEdgeFactory() {
        return socialEdgeFactory;
    }

    public int getNumPeople() {
        return numPeople;
    }

    @Override
    public abstract SocialNetwork create();

}
