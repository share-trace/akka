package org.sharetrace.propagation.model;

import org.jgrapht.graph.DefaultEdge;

@SuppressWarnings("unchecked")
public class Edge<T> extends DefaultEdge {

  public T source() {
    return (T) getSource();
  }

  public T target() {
    return (T) getTarget();
  }
}
