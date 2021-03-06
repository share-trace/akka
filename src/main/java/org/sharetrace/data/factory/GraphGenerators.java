package org.sharetrace.data.factory;

import java.util.Optional;
import org.immutables.builder.Builder;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.generate.RandomRegularGraphGenerator;
import org.jgrapht.generate.ScaleFreeGraphGenerator;
import org.jgrapht.generate.WattsStrogatzGraphGenerator;
import org.sharetrace.experiment.GraphType;

class GraphGenerators {

  private GraphGenerators() {}

  @Builder.Factory
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static <V, E> GraphGenerator<V, E, ?> graphGenerator(
      @Builder.Parameter GraphType graphType,
      @Builder.Parameter int nNodes,
      @Builder.Parameter long seed,
      Optional<Integer> nInitialNodes,
      Optional<Integer> nNewEdges,
      Optional<Integer> nEdges,
      Optional<Integer> degree,
      Optional<Integer> kNearestNeighbors,
      Optional<Double> rewiringProbability) {
    switch (graphType) {
        // Random
      case GNM_RANDOM:
        return new GnmRandomGraphGenerator<>(
            nNodes, getOrThrow(nEdges, "nEdges", GraphType.GNM_RANDOM), seed, false, false);
      case RANDOM_REGULAR:
        return new RandomRegularGraphGenerator<>(
            nNodes, getOrThrow(degree, "degree", GraphType.RANDOM_REGULAR), seed);
        // Non-random
      case BARABASI_ALBERT:
        return new BarabasiAlbertGraphGenerator<>(
            getOrThrow(nInitialNodes, "nInitialNodes", GraphType.BARABASI_ALBERT),
            getOrThrow(nNewEdges, "nNewEdges", GraphType.BARABASI_ALBERT),
            nNodes,
            seed);
      case WATTS_STROGATZ:
        return new WattsStrogatzGraphGenerator<>(
            nNodes,
            getOrThrow(kNearestNeighbors, "kNearestNeighbors", GraphType.WATTS_STROGATZ),
            getOrThrow(rewiringProbability, "rewiringProbability", GraphType.WATTS_STROGATZ),
            seed);
      case SCALE_FREE:
        return new ScaleFreeGraphGenerator<>(nNodes, seed);
      default:
        throw generatorCreationFailedException(graphType);
    }
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private static <T> T getOrThrow(Optional<T> optional, String name, GraphType graphType) {
    return optional.orElseThrow(() -> missingParameterException(name, graphType));
  }

  private static RuntimeException generatorCreationFailedException(GraphType graphType) {
    return new IllegalArgumentException("Failed to create graph generator for " + graphType);
  }

  private static RuntimeException missingParameterException(String name, GraphType graphType) {
    return new IllegalArgumentException("Missing parameter " + name + " for graph " + graphType);
  }
}
