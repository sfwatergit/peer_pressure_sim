package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.api;

import java.util.Collection;

/**
 * Created by sidneyfeygin on 12/7/15.
 */
public interface Graph<V extends Vertex, E extends Edge> {

    boolean addEdge(E edge);

    boolean addVertex(V vertex);

    boolean addEdges(Collection<E> edges);

    boolean addVertex(Collection<V> vertices);

    boolean removeEdge(E edge);

    boolean removeVertex(V vertex);

    boolean removeEdges(Collection<E> edges);

    boolean removeVertices(Collection<V> vertices);

    boolean hasVertex(V vertex);


    boolean hasEdge(E edge);

    V getVertex(V vertex);


    E getEdge(E edge);

    E getEdge(V v0, V v1);

    Collection<E> getEdges();

    Collection<V> getVertices();

    int numEdges();

    int numVertices();

    boolean isDirected();


}
