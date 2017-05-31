package graph;

import java.util.ArrayList;
import java.util.PriorityQueue;

/* See restrictions in Graph.java. */

/** A partial implementation of Graph containing elements common to
 *  directed and undirected graphs.
 *
 *  @author Ze Martinho
 */
abstract class GraphObj extends Graph {

    /** A new, empty Graph. */
    GraphObj() {
        connections = new ArrayList<ArrayList<Integer>>();
        if (isDirected()) {
            preds = new ArrayList<ArrayList<Integer>>();
        }
    }

    @Override
    public int vertexSize() {
        int total = 0;
        for (int i = 0; i < connections.size(); i += 1) {
            if (connections.get(i) != null) {
                total += 1;
            }
        }
        return total;
    }

    @Override
    public int maxVertex() {
        int i = connections.size() - 1;
        while (i > 0) {
            if (connections.get(i) == null) {
                i -= 1;
            } else {
                break;
            }
        }
        return i + 1;
    }

    @Override
    public int edgeSize() {
        ArrayList<Integer> counted = new ArrayList<Integer>();
        int total = 0;
        for (int i = 0; i < connections.size(); i += 1) {
            ArrayList<Integer> row = connections.get(i);
            if (row != null) {
                for (int j = 0; j < row.size(); j += 1) {
                    int id = edgeId(i + 1, row.get(j));
                    if (!counted.contains(id)) {
                        counted.add(id);
                        total += 1;
                    }
                }
            }
        }
        return total;
    }

    @Override
    public abstract boolean isDirected();

    @Override
    public int outDegree(int v) {
        if (!this.contains(v)) {
            return 0;
        }
        ArrayList<Integer> row = connections.get(v - 1);
        if (row == null) {
            return 0;
        }
        return row.size();
    }

    @Override
    public abstract int inDegree(int v);

    @Override
    public boolean contains(int u) {
        if (connections.size() >= u
                && u > 0 && connections.get(u - 1) != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(int u, int v) {
        if (contains(u) && contains(v)) {
            if (isDirected()) {
                return connections.get(u - 1).contains(v);
            } else {
                return connections.get(v - 1).contains(u)
                        || connections.get(u - 1).contains(v);
            }
        }
        return false;
    }

    @Override
    public int add() {
        if (!removed.isEmpty()) {
            int n = removed.remove();
            connections.set(n - 1, new ArrayList<Integer>());
            if (isDirected()) {
                preds.set(n - 1, new ArrayList<Integer>());
            }
            return n;
        } else {
            connections.add(new ArrayList<Integer>());
            if (isDirected()) {
                preds.add(new ArrayList<Integer>());
            }
            return maxVertex();
        }
    }

    @Override
    public int add(int u, int v) {
        if (!isDirected() && u != v) {
            connections.get(v - 1).add(u);
            connections.get(u - 1).add(v);
        } else if (isDirected()) {
            connections.get(u - 1).add(v);
            preds.get(v - 1).add(u);
        } else {
            connections.get(v - 1).add(u);
        }
        return edgeId(u, v);
    }

    @Override
    public void remove(int v) {
        Integer item = new Integer(v);
        for (int i = 0; i < connections.size(); i += 1) {
            if (connections.get(i) != null
                    && connections.get(i).contains(v)) {
                connections.get(i).remove(item);
            }
        }
        if (isDirected()) {
            for (int i = 0; i < preds.size(); i += 1) {
                if (preds.get(i) != null
                        && preds.get(i).contains(v)) {
                    preds.get(i).remove(item);
                }
            }
        }
        removed.add(v);
        connections.set(v - 1, null);
        if (isDirected()) {
            preds.get(v - 1).clear();
        }
    }

    @Override
    public void remove(int u, int v) {
        ArrayList<Integer> row = connections.get(u - 1);
        for (int i = 0; i < row.size(); i += 1) {
            if (row.get(i).equals(v)) {
                row.remove(i);
            }
        }
        if (isDirected()) {
            ArrayList<Integer> predRow = preds.get(v - 1);
            for (int i = 0; i < predRow.size(); i += 1) {
                if (predRow.get(i).equals(u)) {
                    predRow.remove(i);
                }
            }
        }
    }

    @Override
    public Iteration<Integer> vertices() {
        return new Iteration<Integer>() {
            private int k = 0;

            @Override
            public boolean hasNext() {
                return k < connections.size();
            }

            @Override
            public Integer next() {
                k += 1;
                while (hasNext()
                        && connections.get(k - 1) == null) {
                    k += 1;
                }
                return k;
            }
        };
    }

    @Override
    public int successor(int v, int k) {
        ArrayList<Integer> row = connections.get(v - 1);
        if (k < outDegree(v)) {
            return row.get(k);
        } else {
            return 0;
        }
    }

    @Override
    public abstract int predecessor(int v, int k);

    @Override
    public Iteration<Integer> successors(int v) {
        return new Iteration<Integer>() {
            private int k = 0;

            @Override
            public boolean hasNext() {
                return k < outDegree(v);
            }

            @Override
            public Integer next() {
                k += 1;
                return successor(v, k - 1);
            }
        };
    }

    @Override
    public abstract Iteration<Integer> predecessors(int v);

    @Override
    public Iteration<int[]> edges() {
        return new Iteration<int[]>() {
            private int k = 0;
            private int i = 0;

            @Override
            public boolean hasNext() {
                int track = k + 1;
                if (connections.get(k) != null
                        && connections.get(k).size() > i + 1) {
                    return true;
                }
                while (track < connections.size()) {
                    if (connections.get(track) != null
                            && !connections.get(track).isEmpty()) {
                        return true;
                    } else {
                        track += 1;
                    }
                }
                return false;
            }

            @Override
            public int[] next() {
                ArrayList<Integer> row = connections.get(k);
                i += 1;
                if (i == row.size() + 1) {
                    k += 1;
                    i = 1;
                    row = connections.get(k);
                }
                while ((row == null || row.size() == 0)
                        && hasNext()) {
                    k += 1;
                    row = connections.get(k);
                }
                return new int[]{k + 1, row.get(i - 1)};
            }
        };
    }

    @Override
    protected void checkMyVertex(int v) {
        assert (connections.get(v - 1) != null);
    }

    @Override
    /** Used the Cantor pairing function found at:
     * http://math.stackexchange.com/questions/
     * 23503/create-unique-number-from-2-numbers */
    protected int edgeId(int u, int v) {
        if (!isDirected()) {
            int i = Math.min(u, v);
            int j = Math.max(u, v);
            return (int) (0.5 * (i + j) * (i + j + 1) / 2) + j;
        } else {
            return ((u + v) * (u + v + 1) / 2) + v;
        }
    }

    /** A list of the successors for each vertex.
     * This is also the list of predecessors for an
     * undirected graph. */
    protected ArrayList<ArrayList<Integer>> connections;

    /** The list of precedessors for a directed graph. */
    protected ArrayList<ArrayList<Integer>> preds;

    /** The indices of vertices most recently removed. */
    protected PriorityQueue<Integer> removed = new PriorityQueue<>();
}
