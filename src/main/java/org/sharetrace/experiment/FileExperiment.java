package org.sharetrace.experiment;

import java.nio.file.Path;
import java.time.Instant;
import org.sharetrace.data.Dataset;
import org.sharetrace.data.factory.FileDatasetBuilder;
import org.sharetrace.data.factory.RiskScoreFactory;
import org.sharetrace.data.sampling.RiskScoreSampler;
import org.sharetrace.data.sampling.Sampler;
import org.sharetrace.data.sampling.TimeSampler;
import org.sharetrace.message.RiskScore;

public class FileExperiment extends Experiment {

  private static final String WHITESPACE_DELIMITER = "\\s+";
  private final Path path;
  private final String delimiter;
  private final Sampler<RiskScore> riskScoreSampler;

  public FileExperiment(GraphType graphType, Path path, String delimiter, int nRepeats, long seed) {
    super(graphType, nRepeats, seed);
    this.path = path;
    this.delimiter = delimiter;
    this.riskScoreSampler = newRiskScoreSampler();
  }

  protected Sampler<RiskScore> newRiskScoreSampler() {
    return RiskScoreSampler.builder().timeSampler(newTimeSampler()).seed(seed).build();
  }

  protected Sampler<Instant> newTimeSampler() {
    return TimeSampler.builder().seed(seed).referenceTime(referenceTime).ttl(scoreTtl()).build();
  }

  public static void runWhitespaceDelimited(
      GraphType graphType, Path path, int nRepeats, long seed) {
    new FileExperiment(graphType, path, WHITESPACE_DELIMITER, nRepeats, seed).run();
  }

  @Override
  protected Dataset newDataset() {
    return FileDatasetBuilder.create()
        .delimiter(delimiter)
        .path(path)
        .addAllLoggable(loggable())
        .referenceTime(referenceTime)
        .riskScoreFactory(riskScoreFactory())
        .build();
  }

  private RiskScoreFactory riskScoreFactory() {
    return x -> riskScoreSampler.sample();
  }
}
