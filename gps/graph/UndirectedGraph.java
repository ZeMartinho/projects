package graph;

/* See restrictions in Graph.java. */

/** Represents an undirected graph.  Out edges and in edges are not
 *  distinguished.  Likewise for successors and predecessors.
 *
 *  @author Ze Martinho
 */
public class UndirectedGraph extends GraphObj {

    @Override
    public boolean isDirected() {
        return false;
    }

    @Override
    public int inDegree(int v) {
        return outDegree(v);
    }

    @Override
    public int predecessor(int v, int k) {
        if (k < connections.get(v - 1).size()) {
            return connections.get(v - 1).get(k);
        }
        return 0;
    }

    @Override
    public Iteration<Integer> predecessors(int v) {
        return successors(v);
    }
}
