package org.sharetrace.logging.metrics;

import org.immutables.value.Value;

@Value.Immutable
interface BaseGraphEccentricityMetrics extends LoggableMetric {

  double radius();

  double diameter();

  long center();

  long periphery();
}
