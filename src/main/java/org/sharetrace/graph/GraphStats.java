package org.sharetrace.graph;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.GraphMetrics;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.scoring.ClosenessCentrality;
import org.jgrapht.alg.scoring.ClusteringCoefficient;
import org.jgrapht.alg.scoring.Coreness;
import org.jgrapht.alg.scoring.EigenvectorCentrality;
import org.jgrapht.alg.scoring.KatzCentrality;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.alg.shortestpath.GraphMeasurer;

public final class GraphStats<V, E> {

  private static final double NOT_COMPUTED = -1d;
  private final Graph<V, E> graph;
  private final ShortestPathAlgorithm<V, E> shortestPathAlgorithm;
  private GraphMeasurer<V, E> measurer;
  private HarmonicCentrality<V, E> harmonicCentrality;
  private KatzCentrality<V, E> katzCentrality;
  private EigenvectorCentrality<V, E> eigenvectorCentrality;
  private ClusteringCoefficient<V, E> clusteringCoefficient;
  private Coreness<V, E> coreness;
  private long nTriangles = (long) NOT_COMPUTED;
  private int center = (int) NOT_COMPUTED;
  private int periphery = (int) NOT_COMPUTED;
  private int girth = (int) NOT_COMPUTED;
  private int degeneracy = (int) NOT_COMPUTED;

  public GraphStats(Graph<V, E> graph) {
    this.graph = graph;
    this.shortestPathAlgorithm = new FloydWarshallShortestPaths<>(graph);
  }

  public int girth() {
    if (girth == NOT_COMPUTED) {
      girth = GraphMetrics.getGirth(graph);
    }
    return girth;
  }

  public long nTriangles() {
    if (nTriangles == NOT_COMPUTED) {
      nTriangles = GraphMetrics.getNumberOfTriangles(graph);
    }
    return nTriangles;
  }

  public long nNodes() {
    return graph.iterables().vertexCount();
  }

  public long nEdges() {
    return graph.iterables().edgeCount();
  }

  public double radius() {
    return getMeasurer().getRadius();
  }

  private GraphMeasurer<V, E> getMeasurer() {
    if (measurer == null) {
      measurer = new GraphMeasurer<>(graph);
    }
    return measurer;
  }

  public double diameter() {
    return getMeasurer().getDiameter();
  }

  public int periphery() {
    if (periphery == NOT_COMPUTED) {
      periphery = getMeasurer().getGraphPeriphery().size();
    }
    return periphery;
  }

  public int center() {
    if (center == NOT_COMPUTED) {
      center = getMeasurer().getGraphCenter().size();
    }
    return center;
  }

  public Map<V, Double> harmonicCentralityScores() {
    return getHarmonicCentrality().getScores();
  }

  private HarmonicCentrality<V, E> getHarmonicCentrality() {
    if (harmonicCentrality == null) {
      harmonicCentrality = new HarmonicCentrality<>(graph, shortestPathAlgorithm);
    }
    return harmonicCentrality;
  }

  public double globalClusteringCoefficient() {
    return getClusteringCoefficient().getGlobalClusteringCoefficient();
  }

  private ClusteringCoefficient<V, E> getClusteringCoefficient() {
    if (clusteringCoefficient == null) {
      clusteringCoefficient = new ClusteringCoefficient<>(graph);
    }
    return clusteringCoefficient;
  }

  public Map<V, Double> localClusteringCoefficients() {
    return getClusteringCoefficient().getScores();
  }

  public Map<V, Double> katzCentralityScores() {
    return getKatzCentrality().getScores();
  }

  public KatzCentrality<V, E> getKatzCentrality() {
    if (katzCentrality == null) {
      katzCentrality = new KatzCentrality<>(graph);
    }
    return katzCentrality;
  }

  public Map<V, Double> eigenvectorCentralityScores() {
    return getEigenvectorCentrality().getScores();
  }

  public EigenvectorCentrality<V, E> getEigenvectorCentrality() {
    if (eigenvectorCentrality == null) {
      eigenvectorCentrality = new EigenvectorCentrality<>(graph);
    }
    return eigenvectorCentrality;
  }

  public int degeneracy() {
    if (degeneracy == NOT_COMPUTED) {
      degeneracy = getCoreness().getDegeneracy();
    }
    return degeneracy;
  }

  private Coreness<V, E> getCoreness() {
    if (coreness == null) {
      coreness = new Coreness<>(graph);
    }
    return coreness;
  }

  // Copied from JGraphT's implementation, but reuses a ShortestPathAlgorithm instance.
  private static final class HarmonicCentrality<V, E> extends ClosenessCentrality<V, E> {

    private final ShortestPathAlgorithm<V, E> shortestPathAlgorithm;

    public HarmonicCentrality(
        Graph<V, E> graph, ShortestPathAlgorithm<V, E> shortestPathAlgorithm) {
      super(graph);
      this.shortestPathAlgorithm = shortestPathAlgorithm;
    }

    @Override
    protected void compute() {
      // Modified from original to use fastutil Map.
      scores = new Object2DoubleOpenHashMap<>();
      ShortestPathAlgorithm<V, E> alg = getShortestPathAlgorithm();
      int n = graph.vertexSet().size();
      for (V v : graph.vertexSet()) {
        double sum = 0d;
        SingleSourcePaths<V, E> paths = alg.getPaths(v);
        for (V u : graph.vertexSet()) {
          if (!u.equals(v)) {
            sum += 1.0 / paths.getWeight(u);
          }
        }
        if (normalize && n > 1) {
          scores.put(v, sum / (n - 1));
        } else {
          scores.put(v, sum);
        }
      }
    }

    @Override
    protected ShortestPathAlgorithm<V, E> getShortestPathAlgorithm() {
      return shortestPathAlgorithm;
    }
  }
}