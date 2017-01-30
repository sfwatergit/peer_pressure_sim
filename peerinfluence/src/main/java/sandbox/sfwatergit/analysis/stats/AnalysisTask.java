package sandbox.sfwatergit.analysis.stats;

import edu.uci.ics.jung.graph.Graph;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

/**
 * Created by sidneyfeygin on 1/16/16.
 */
@FunctionalInterface
public interface AnalysisTask<R extends Number> {
    R analyze(Graph<SocialVertex, SocialEdge> graph);
}
