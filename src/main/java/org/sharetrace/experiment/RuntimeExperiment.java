package org.sharetrace.experiment;

import java.util.Objects;
import java.util.Set;
import org.sharetrace.logging.Loggable;
import org.sharetrace.logging.metrics.GraphSizeMetrics;
import org.sharetrace.logging.metrics.RuntimeMetric;
import org.sharetrace.logging.settings.ExperimentSettings;
import org.sharetrace.util.Range;

public class RuntimeExperiment extends SyntheticExperiment {

  private final Range nNodesRange;

  private RuntimeExperiment(Builder builder) {
    super(builder);
    this.nNodesRange = builder.nNodesRange;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  protected Set<Class<? extends Loggable>> loggable() {
    // Events and path-based graph metrics becomes too expensive for large graphs
    return Set.of(GraphSizeMetrics.class, RuntimeMetric.class, ExperimentSettings.class);
  }

  @Override
  public void run() {
    for (double n : nNodesRange) {
      nNodes = Math.toIntExact((long) n);
      super.run();
    }
  }

  public static class Builder extends SyntheticExperiment.Builder {

    private Range nNodesRange;

    public Builder nNodesRange(Range nNodesRange) {
      this.nNodesRange = nNodesRange;
      return this;
    }

    @Override
    protected void preBuild() {
      Objects.requireNonNull(nNodesRange);
      super.preBuild();
    }

    @Override
    public Experiment build() {
      preBuild();
      return new RuntimeExperiment(this);
    }
  }
}
