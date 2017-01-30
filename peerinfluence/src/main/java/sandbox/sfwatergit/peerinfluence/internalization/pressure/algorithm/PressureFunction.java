package sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm;

import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

import java.util.function.ToDoubleFunction;

/**
 * Created by sidneyfeygin on 1/18/16.
 */
@FunctionalInterface
public interface PressureFunction extends ToDoubleFunction<SocialVertex> {
}
