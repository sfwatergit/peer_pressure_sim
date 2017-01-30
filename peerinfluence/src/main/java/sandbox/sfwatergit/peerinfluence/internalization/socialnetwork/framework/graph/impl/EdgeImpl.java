package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl;

import edu.uci.ics.jung.graph.util.Pair;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.api.Edge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.api.Vertex;

import java.util.Iterator;

/**
 * Undirected and unweighted abstract edge. After illenberger
 * <p>
 * Created by sidneyfeygin on 12/2/15.
 */
public abstract class EdgeImpl implements Edge {

    Pair<Vertex> vertices;


    EdgeImpl() {
    }

    EdgeImpl(Vertex v1, Vertex v2) {
        vertices = new Pair<>(v1, v2);
    }

    @Override
    public Vertex getOpposite(Vertex v) {
        if (vertices.getFirst().equals(v))
            return vertices.getSecond();
        else if (vertices.getSecond().equals(v))
            return vertices.getFirst();
        else
            return null;
    }

    @Override
    public Pair<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public void setVertices(Pair<Vertex> vertices) {
        this.vertices = vertices;
    }

    @Override
    public Iterator<Vertex> vertices() {
        return null;
    }

    @Override
    public String key() {
        return null;
    }

    @Override
    public Object value() {
        return null;
    }
}
