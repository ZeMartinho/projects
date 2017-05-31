package graph;

import java.util.ArrayList;

/* See restrictions in Graph.java. */

/** Represents a general unlabeled directed graph whose vertices are denoted by
 *  positive integers. Graphs may have self edges.
 *
 *  @author Ze Martinho
 */
public class DirectedGraph extends GraphObj {

    @Override
    public boolean isDirected() {
        return true;
    }

    @Override
    public int inDegree(int v) {
        ArrayList<Integer> row = preds.get(v - 1);
        return row.size();
    }

    @Override
    public int predecessor(int v, int k) {
        if (k < preds.get(v - 1).size()) {
            return preds.get(v - 1).get(k);
        }
        return 0;
    }

    @Override
    public Iteration<Integer> predecessors(int v) {
        return new Iteration<Integer>() {
            private int k = 0;

            @Override
            public boolean hasNext() {
                return (k < inDegree(v));
            }

            @Override
            public Integer next() {
                k += 1;
                return predecessor(v, k - 1);
            }
        };
    }
}
