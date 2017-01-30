package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.jung.factories;

import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.socialperson.SocialPerson;

/**
 * Created by sidneyfeygin on 1/14/16.
 */
public interface  SocialVertexFactory  {
    SocialVertex create(SocialPerson person);
}
