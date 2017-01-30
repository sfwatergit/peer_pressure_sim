package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.api;

import edu.uci.ics.jung.graph.util.Pair;

import java.util.Iterator;

/**
 * Created by sidneyfeygin on 12/2/15.
 */
public interface Edge extends Element {

    Pair<Vertex> getVertices();

    void setVertices(Pair<Vertex> vertices);

    Iterator<Vertex> vertices();

    Vertex getOpposite(Vertex v);

}
