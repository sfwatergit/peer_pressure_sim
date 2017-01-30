package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework;

import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.api.GraphFactory;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

/**
 * Created by sidneyfeygin on 12/9/15.
 */
public class SocialNetworkFactory implements GraphFactory<SocialVertex,SocialEdge> {


    @Override
    public SocialVertex createVertex() {
        return new SocialVertex();
    }


}
