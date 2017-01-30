package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.jung.factories;

import org.apache.commons.collections15.Factory;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;

/**
 * Created by sidneyfeygin on 1/14/16.
 */
public class SocialEdgeFactory implements Factory<SocialEdge> {
    @Override
    public SocialEdge create() {
        return new SocialEdge();
    }
}
