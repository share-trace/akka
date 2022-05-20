package org.sharetrace.graph;

import org.jgrapht.Graph;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.nio.GraphExporter;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.opt.graph.fastutil.FastutilMapGraph;
import org.sharetrace.RiskPropagation;
import org.sharetrace.logging.Loggable;
import org.sharetrace.logging.Loggables;
import org.sharetrace.logging.Logging;
import org.sharetrace.logging.metrics.*;
import org.sharetrace.util.DescriptiveStats;
import org.sharetrace.util.TypedSupplier;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A simple graph in which a node represents a person and an edge between two nodes indicates that
 * the associated persons of the incident nodes came in contact. Nodes identifiers are zero-based
 * contiguous natural numbers. In an instance of {@link RiskPropagation}, the topology of this graph
 * is mapped to a collection {@link Node} actors.
 *
 * @see Node
 * @see Edge
 */
public class ContactGraph implements TemporalGraph {

  private static final Logger logger = Logging.metricLogger();
  private final Loggables loggables;
  private final Graph<Integer, Edge<Integer>> graph;

  private ContactGraph(
      Graph<Integer, Edge<Integer>> graph, Set<Class<? extends Loggable>> loggable) {
    this.graph = graph;
    this.loggables = Loggables.create(loggable, logger);
    logMetrics();
  }

  private static TypedSupplier<LoggableMetric> sizeMetrics(GraphStats<?, ?> stats) {
    return TypedSupplier.of(GraphSizeMetrics.class, () -> graphSizeMetrics(stats));
  }

  private static TypedSupplier<LoggableMetric> cycleMetrics(GraphStats<?, ?> stats) {
    return TypedSupplier.of(GraphCycleMetrics.class, () -> graphCycleMetrics(stats));
  }

  private static TypedSupplier<LoggableMetric> eccentricityMetrics(GraphStats<?, ?> stats) {
    return TypedSupplier.of(GraphEccentricityMetrics.class, () -> graphEccentricityMetrics(stats));
  }

  private static TypedSupplier<LoggableMetric> scoringMetrics(GraphStats<?, ?> stats) {
    return TypedSupplier.of(GraphScoringMetrics.class, () -> graphScoringMetrics(stats));
  }

  private static GraphSizeMetrics graphSizeMetrics(GraphStats<?, ?> stats) {
    return GraphSizeMetrics.builder().nNodes(stats.nNodes()).nEdges(stats.nEdges()).build();
  }

  private static GraphCycleMetrics graphCycleMetrics(GraphStats<?, ?> stats) {
    return GraphCycleMetrics.builder().nTriangles(stats.nTriangles()).girth(stats.girth()).build();
  }

  private static GraphEccentricityMetrics graphEccentricityMetrics(GraphStats<?, ?> stats) {
    return GraphEccentricityMetrics.builder()
        .radius(stats.radius())
        .diameter(stats.diameter())
        .center(stats.center())
        .periphery(stats.periphery())
        .build();
  }

  private static GraphScoringMetrics graphScoringMetrics(GraphStats<?, ?> stats) {
    return GraphScoringMetrics.builder()
        .degeneracy(stats.degeneracy())
        .globalClusteringCoefficient(stats.globalClusteringCoefficient())
        .localClusteringCoefficient(DescriptiveStats.of(stats.localClusteringCoefficients()))
        .harmonicCentrality(DescriptiveStats.of(stats.harmonicCentralities()))
        .katzCentrality(DescriptiveStats.of(stats.katzCentralities()))
        .eigenvectorCentrality(DescriptiveStats.of(stats.eigenvectorCentralities()))
        .build();
  }

  private static String newGraphLabel() {
    return UUID.randomUUID().toString();
  }

  private static Writer newGraphWriter(String graphLabel) throws IOException {
    Path graphsPath = Logging.graphsLogPath();
    if (!Files.exists(graphsPath)) {
      Files.createDirectories(graphsPath);
    }
    Path filePath = Path.of(graphsPath.toString(), graphLabel + ".graphml");
    return Files.newBufferedWriter(filePath);
  }

  private static GraphExporter<Integer, Edge<Integer>> newGraphExporter() {
    GraphMLExporter<Integer, Edge<Integer>> exporter = new GraphMLExporter<>();
    exporter.setVertexIdProvider(String::valueOf);
    return exporter;
  }

  public static ContactGraph create(
      GraphGenerator<Integer, Edge<Integer>, ?> generator,
      Set<Class<? extends Loggable>> loggable) {
    Graph<Integer, Edge<Integer>> graph = newGraph();
    generator.generateGraph(graph);
    return new ContactGraph(graph, loggable);
  }

  private static Graph<Integer, Edge<Integer>> newGraph() {
    return new FastutilMapGraph<>(nodeIdFactory(), Edge::new, DefaultGraphType.simple());
  }

  private static Supplier<Integer> nodeIdFactory() {
    int[] id = new int[1];
    return () -> id[0]++;
  }

  private void logMetrics() {
    GraphStats<?, ?> stats = GraphStats.of(graph);
    loggables.info(LoggableMetric.KEY, sizeMetrics(stats));
    loggables.info(LoggableMetric.KEY, cycleMetrics(stats));
    loggables.info(LoggableMetric.KEY, eccentricityMetrics(stats));
    loggables.info(LoggableMetric.KEY, scoringMetrics(stats));
    logGraph();
  }

  private void logGraph() {
    if (loggables.loggable().contains(GraphTopologyMetric.class)) {
      String graphLabel = newGraphLabel();
      loggables.info(LoggableMetric.KEY, GraphTopologyMetric.of(graphLabel));
      try (Writer writer = newGraphWriter(graphLabel)) {
        newGraphExporter().exportGraph(graph, writer);
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public IntStream nodes() {
    return graph.vertexSet().stream().mapToInt(Integer::intValue);
  }

  @Override
  public Stream<List<Integer>> edges() {
    return graph.edgeSet().stream().map(edge -> List.of(edge.source(), edge.target()));
  }

  @Override
  public long nNodes() {
    return graph.iterables().vertexCount();
  }

  @Override
  public long nEdges() {
    return graph.iterables().edgeCount();
  }
}
