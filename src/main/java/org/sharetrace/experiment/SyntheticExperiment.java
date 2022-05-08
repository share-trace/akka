package org.sharetrace.experiment;

import java.util.Random;
import org.jgrapht.generate.GraphGenerator;
import org.sharetrace.data.Dataset;
import org.sharetrace.data.SyntheticDatasetBuilder;
import org.sharetrace.graph.Edge;
import org.sharetrace.message.Parameters;

public abstract class SyntheticExperiment extends Experiment {

  protected final GraphType graphType;
  protected final long seed;
  protected int nNodes;
  protected int nEdges;

  protected SyntheticExperiment(GraphType graphType, int nNodes, int nEdges, long seed) {
    this.graphType = graphType;
    this.nNodes = nNodes;
    this.nEdges = nEdges;
    this.seed = seed;
  }

  @Override
  protected Dataset<Integer> newDataset(Parameters parameters) {
    return SyntheticDatasetBuilder.create()
        .addAllLoggable(loggable())
        .generator(newGenerator())
        .clock(clock())
        .scoreTtl(parameters.scoreTtl())
        .contactTtl(parameters.contactTtl())
        .random(new Random(seed))
        .build();
  }

  protected GraphGenerator<Integer, Edge<Integer>, ?> newGenerator() {
    return GraphGeneratorBuilder.<Integer, Edge<Integer>>create(graphType)
        .nNodes(nNodes)
        .nEdges(nEdges)
        .random(new Random(seed))
        .build();
  }
}
