package org.sharetrace.experiment;

import akka.actor.typed.Behavior;
import java.time.Duration;
import java.time.Instant;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.sharetrace.Runner;
import org.sharetrace.data.Dataset;
import org.sharetrace.model.message.AlgorithmMessage;
import org.sharetrace.model.message.Parameters;
import org.sharetrace.model.message.RiskScore;
import org.sharetrace.model.message.RiskScoreMessage;
import org.sharetrace.util.IntervalCache;

public abstract class Experiment<T> implements Runnable {

  protected static final Duration DEFAULT_TTL = Duration.ofDays(14L);
  protected static final double DEFAULT_SEND_TOLERANCE = 0.6d;
  protected static final double DEFAULT_TRANSMISSION_RATE = 0.8d;
  protected static final Duration DEFAULT_TIME_BUFFER = Duration.ofDays(2L);
  protected static final long DEFAULT_CACHE_INTERVALS = DEFAULT_TTL.toDays() + 1L;
  protected static final long DEFAULT_BUFFER = 1L;
  protected static final Duration DEFAULT_CACHE_INTERVAL = Duration.ofDays(1L);
  protected static final Duration DEFAULT_CACHE_REFRESH_RATE = Duration.ofHours(1L);
  protected static final Duration DEFAULT_NODE_TIMEOUT = Duration.ofSeconds(5L);
  protected static final Duration DEFAULT_NODE_REFRESH_RATE = Duration.ofHours(1L);
  protected static final long DEFAULT_SEED = 12345L;

  @Override
  public void run() {
    Parameters parameters = parameters();
    Dataset<T> dataset = newDataset(parameters);
    Behavior<AlgorithmMessage> algorithm = newAlgorithm(dataset, parameters);
    Runner.run(algorithm);
  }

  protected Supplier<Instant> clock() {
    return Instant::now;
  }

  protected abstract Dataset<T> newDataset(Parameters parameters);

  protected abstract Behavior<AlgorithmMessage> newAlgorithm(
      Dataset<T> dataset, Parameters parameters);

  protected BiFunction<RiskScore, Parameters, RiskScore> transmitter() {
    return (received, parameters) ->
        RiskScore.builder()
            .value(received.value() * parameters.transmissionRate())
            .timestamp(received.timestamp())
            .build();
  }

  protected Parameters parameters() {
    return Parameters.builder()
        .sendTolerance(DEFAULT_SEND_TOLERANCE)
        .transmissionRate(DEFAULT_TRANSMISSION_RATE)
        .timeBuffer(DEFAULT_TIME_BUFFER)
        .scoreTtl(DEFAULT_TTL)
        .contactTtl(DEFAULT_TTL)
        .idleTimeout(DEFAULT_NODE_TIMEOUT) // TODO Scale based on graph size
        .refreshRate(DEFAULT_NODE_REFRESH_RATE)
        .build();
  }

  protected Supplier<IntervalCache<RiskScoreMessage>> cacheFactory() {
    return () ->
        IntervalCache.<RiskScoreMessage>builder()
            .nIntervals(DEFAULT_CACHE_INTERVALS)
            .nBuffer(DEFAULT_BUFFER)
            .interval(DEFAULT_CACHE_INTERVAL)
            .refreshRate(DEFAULT_CACHE_REFRESH_RATE)
            .clock(clock())
            .mergeStrategy(this::mergeStrategy)
            .prioritizeReads(false)
            .build();
  }

  protected RiskScoreMessage mergeStrategy(RiskScoreMessage oldScore, RiskScoreMessage newScore) {
    double oldValue = oldScore.score().value();
    double newValue = newScore.score().value();
    Instant oldTimestamp = oldScore.score().timestamp();
    Instant newTimestamp = newScore.score().timestamp();
    boolean isHigher = oldValue < newValue;
    boolean isOlder = oldValue == newValue && oldTimestamp.isAfter(newTimestamp);
    return isHigher || isOlder ? newScore : oldScore;
  }
}
