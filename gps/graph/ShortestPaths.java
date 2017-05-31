package graph;

/* See restrictions in Graph.java. */


import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Iterator;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;

/** The shortest paths through an edge-weighted graph.
 *  By overrriding methods getWeight, setWeight, getPredecessor, and
 *  setPredecessor, the client can determine how to represent the weighting
 *  and the search results.  By overriding estimatedDistance, clients
 *  can search for paths to specific destinations using A* search.
 *  @author Ze Martinho
 */
public abstract class ShortestPaths {

    /** The shortest paths in G from SOURCE. */
    public ShortestPaths(Graph G, int source) {
        this(G, source, 0);
    }

    /** A shortest path in G from SOURCE to DEST. */
    public ShortestPaths(Graph G, int source, int dest) {
        _G = G;
        _source = source;
        _dest = dest;
        _weights = new ArrayList<Double>(_G.vertexSize());
        _preds = new ArrayList<Integer>(_G.vertexSize());
    }

    /** Initialize the shortest paths.  Must be called before using
     *  getWeight, getPredecessor, and pathTo. */
    public void setPaths() {
        class SetPathTraveral extends Traversal {
            /** Constructor for this specialized traversal.
             * @param G is the graph to be traversed. */
            protected SetPathTraveral(Graph G) {
                super(G, new TreeQueue<Integer>(_edgeWeightComparator));
            }
            @Override
            public void traverse(int v0) {
                for (int v : _G.vertices()) {
                    if (v == v0) {
                        _weights.add(0.0);
                        _preds.add(0);
                    } else {
                        _weights.add(Double.POSITIVE_INFINITY);
                        _preds.add(0);
                    }
                }
                for (int v : _G.vertices()) {
                    _fringe.add(v);
                }
                while (!_fringe.isEmpty()) {
                    int v = _fringe.remove();
                    if (v == getDest()
                            && _preds.get(v - 1) != 0) {
                        return;
                    }
                    for (int next : _G.successors(v)) {
                        double edgeWeight = getWeight(v, next);
                        if (getWeight(next) > edgeWeight
                                + getWeight(v)) {
                            setWeight(next, edgeWeight + _weights.get(v - 1));
                            setPredecessor(next, v);
                            _fringe.remove(next);
                            _fringe.add(next);
                        }
                    }
                }
            }
        }
        SetPathTraveral setPath = new SetPathTraveral(_G);
        setPath.traverse(getSource());
    }

    /** Returns the starting vertex. */
    public int getSource() {
        return _source;
    }

    /** Returns the target vertex, or 0 if there is none. */
    public int getDest() {
        return _dest;
    }

    /** Returns the current weight of vertex V in the graph.  If V is
     *  not in the graph, returns positive infinity. */
    public abstract double getWeight(int v);

    /** Set getWeight(V) to W. Assumes V is in the graph. */
    protected abstract void setWeight(int v, double w);

    /** Returns the current predecessor vertex of vertex V in the graph, or 0 if
     *  V is not in the graph or has no predecessor. */
    public abstract int getPredecessor(int v);

    /** Set getPredecessor(V) to U. */
    protected abstract void setPredecessor(int v, int u);

    /** Returns an estimated heuristic weight of the shortest path from vertex
     *  V to the destination vertex (if any).  This is assumed to be less
     *  than the actual weight, and is 0 by default. */
    protected double estimatedDistance(int v) {
        return 0.0;
    }

    /** Returns the current weight of edge (U, V) in the graph.  If (U, V) is
     *  not in the graph, returns positive infinity. */
    protected abstract double getWeight(int u, int v);

    /** Returns a list of vertices starting at _source and ending
     *  at V that represents a shortest path to V.  Invalid if there is a
     *  destination vertex other than V. */
    public List<Integer> pathTo(int v) {
        int j = v;
        ArrayList<Integer> path = new ArrayList<Integer>();
        path.add(v);
        while (getPredecessor(j) != 0) {
            j = getPredecessor(j);
            path.add(j);
        }
        Collections.reverse(path);
        return path;
    }

    /** Returns a list of vertices starting at the source and ending at the
     *  destination vertex. Invalid if the destination is not specified. */
    public List<Integer> pathTo() {
        return pathTo(getDest());
    }

    /** The graph being searched. */
    protected final Graph _G;
    /** The starting vertex. */
    private final int _source;
    /** The target vertex. */
    private final int _dest;
    /** A list of the weights of each vertex. */
    protected ArrayList<Double> _weights;
    /** The immediate previous vertex of each vertex. */
    protected ArrayList<Integer> _preds;
    /** The comparator for the TreeQueue used in the traversal. */
    private final Comparator<Integer> _edgeWeightComparator
            = new Comparator<Integer>() {
                @Override
                public int compare(Integer e0, Integer e1) {
                    double weight0 = getWeight(e0) + estimatedDistance(e0);
                    double weight1 = getWeight(e1) + estimatedDistance(e1);
                    if (weight0 == Double.POSITIVE_INFINITY
                            && weight1 == Double.POSITIVE_INFINITY) {
                        return (int) (estimatedDistance(e0)
                                - estimatedDistance(e1));
                    }
                    if (weight0 == weight1) {
                        return e0 - e1;
                    }
                    return (int) (weight0 - weight1);
                }
            };

    /** A TreeSet wrapped in a Queue for the fringe of the
     * traversal.
     * @param <E> parameter type of stored objects
     */
    private class TreeQueue<E> extends TreeSet<E> implements Queue<E> {

        /** The constructor, which requires a TreeSet.
        is the TreeSet to be wrapped.
         @param c is the comparator to be used. */
        public TreeQueue(Comparator<E> c) {
            super(c);
        }

        /** Adds objects like a TreeSet.
         * @param e will be added
         * @return boolean whether it was added or not. */
        public boolean add(E e) {
            return super.add(e);
        }

        /** Adds like before.
         * @param e is object to be added.
         * @return whether or not object was added.
         */
        public boolean offer(E e) {
            super.add(e);
            return true;
        }

        /** Remove the top element from the TreeSet.
         * @return the first element in the TreeQueue. */
        public E poll() {
            return super.pollFirst();
        }

        /** Necessary to implement Queue.
         * @return null for the our purposes. */
        public E element() {
            return null;
        }

        /** Get the first item of the TreeQueue.
         *
         * @return the first item of the TreeQueue.
         */
        public E remove() {
            return super.pollFirst();
        }

        /** Returns the size of the TreeSet.
         * @return the size of this object. */
        public int size() {
            return super.size();
        }

        /** Peek is not implemented in this case.
         * @return null in this specialized case. */
        public E peek() {
            return null;
        }

        /** Returns the iterator of the TreeSet.
         * @return an iteration over the vertices. */
        public Iterator<E> iterator() {
            return super.iterator();
        }
    }
}
