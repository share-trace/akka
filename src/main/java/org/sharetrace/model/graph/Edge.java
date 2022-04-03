package org.sharetrace.model.graph;

import org.jgrapht.graph.DefaultEdge;

/**
 * An edge which allows access to its endpoints. Whether {@link #source()} and {@link #target()}
 * indicate that the edge is directed or undirected is left to the graph implementation.
 *
 * @param <T> The type of the edge identifier.
 */
@SuppressWarnings("unchecked")
public class Edge<T> extends DefaultEdge {

  /** Returns an endpoint (if undirected) or starting point (if directed) of the edge. */
  public T source() {
    return (T) getSource();
  }

  /** Returns an endpoint (if undirected) or ending point (if directed) of the edge. */
  public T target() {
    return (T) getTarget();
  }
}
