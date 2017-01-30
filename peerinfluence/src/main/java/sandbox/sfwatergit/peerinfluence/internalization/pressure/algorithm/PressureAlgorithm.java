package sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm;

import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

import java.util.Set;

/**
 * Created by sidneyfeygin on 1/18/16.
 */
public interface PressureAlgorithm {


    Double computeEgoPressure(Set<SocialVertex> rels, PressureFunction pressureFunction);


}
