package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.api;

import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.EdgeImpl;

/**
 * Created by sidneyfeygin on 12/9/15.
 */
public interface GraphFactory<V extends Vertex,E extends EdgeImpl> {

    V createVertex();
}
