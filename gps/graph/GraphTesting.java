package graph;

import org.junit.Test;

import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;

import static org.junit.Assert.*;

/** Unit tests for the Graph class.
 *  @author
 */
public class GraphTesting {

    @Test
    public void emptyGraph() {
        DirectedGraph g = new DirectedGraph();
        assertEquals("Initial graph has vertices", 0, g.vertexSize());
        assertEquals("Initial graph has edges", 0, g.edgeSize());
    }

    @Test
    public void directedGraphAddTest() {
        DirectedGraph g = new DirectedGraph();
        for (int i = 0; i < 10; i += 1) {
            g.add();
        }
        g.add(1, 2);
        g.add(3, 4);
        g.add(7, 10);
        assertEquals("Vertices incorrect", 10, g.vertexSize());
        assertTrue(g.contains(1, 2));
        assertFalse(g.contains(2, 1));
        assertFalse(g.contains(10, 7));
        assertTrue(g.contains(3, 4));
        assertTrue(g.contains(7, 10));
        assertFalse(g.contains(7, 1));
        assertFalse(g.contains(3, 10));
    }

    @Test
    public void directedGraphRemoveTest() {
        DirectedGraph g = new DirectedGraph();
        for (int i = 0; i < 10; i += 1) {
            g.add();
        }
        g.add(1, 2);
        g.add(3, 4);
        g.add(7, 10);
        g.remove(2);
        assertFalse(g.contains(2));
        assertFalse(g.contains(1, 2));
        assertFalse(g.contains(2, 1));
        assertTrue(g.contains(3, 4));
        assertEquals("Vertices incorrect", 9, g.vertexSize());
        g.remove(3, 4);
        assertFalse(g.contains(3, 4));
    }

    @Test
    public void directedGraphSuccessorsPredecessorsTest() {
        DirectedGraph g = new DirectedGraph();
        for (int i = 0; i < 10; i += 1) {
            g.add();
        }
        g.add(1, 2);
        g.add(1, 3);
        g.add(1, 10);
        g.add(1, 9);
        g.add(1, 7);
        assertEquals("Wrong outDegree", 5, g.outDegree(1));
        assertEquals("Wrong successor picked", 2, g.successor(1, 0));
        assertEquals("Wrong successor picked", 3, g.successor(1, 1));
        assertEquals("Wrong successor picked", 10, g.successor(1, 2));
        assertEquals("Wrong successor picked", 9, g.successor(1, 3));
        assertEquals("Wrong successor picked", 7, g.successor(1, 4));
        g.add(3, 2);
        g.add(6, 2);
        g.add(8, 2);
        assertEquals("Wrong inDegree", 4, g.inDegree(2));
        assertEquals("Wrong predecessor picked", 1, g.predecessor(2, 0));
        assertEquals("Wrong predecessor picked", 3, g.predecessor(2, 1));
        assertEquals("Wrong predecessor picked", 6, g.predecessor(2, 2));
        assertEquals("Wrong predecessor picked", 8, g.predecessor(2, 3));
    }

    @Test
    public void directedGraphIterations() {
        DirectedGraph g = new DirectedGraph();
        for (int i = 0; i < 10; i += 1) {
            g.add();
        }
        g.add(1, 2);
        g.add(1, 3);
        g.add(1, 10);
        g.add(1, 9);
        g.add(1, 7);
        Iteration<Integer> iter = g.successors(1);
        assertEquals("Wrong successor picked", new Integer(2), iter.next());
        assertEquals("Wrong successor picked", new Integer(3), iter.next());
        assertEquals("Wrong successor picked", new Integer(10), iter.next());
        assertEquals("Wrong successor picked", new Integer(9), iter.next());
        assertEquals("Wrong successor picked", new Integer(7), iter.next());
        assertFalse(iter.hasNext());
        g.add(3, 2);
        g.add(6, 2);
        g.add(8, 2);
        Iteration<Integer> iterP = g.predecessors(2);
        assertEquals("Wrong predecessor picked", new Integer(1), iterP.next());
        assertEquals("Wrong predecessor picked", new Integer(3), iterP.next());
        assertEquals("Wrong predecessor picked", new Integer(6), iterP.next());
        assertEquals("Wrong predecessor picked", new Integer(8), iterP.next());
    }

    @Test
    public void directedGraphEdgesTest() {
        DirectedGraph g = new DirectedGraph();
        for (int i = 0; i < 10; i += 1) {
            g.add();
        }
        g.add(1, 2);
        g.add(1, 3);
        g.add(2, 3);
        g.add(5, 6);
        g.add(5, 8);
        g.add(9, 10);
        Iteration<int[]> iter = g.edges();
        int[] next = iter.next();
        assertEquals("Wrong successor picked", 1, next[0]);
        assertEquals("Wrong successor picked", 2, next[1]);
        int[] nexttwo = iter.next();
        assertEquals("Wrong successor picked", 1, nexttwo[0]);
        assertEquals("Wrong successor picked", 3, nexttwo[1]);
        int[] nextthree = iter.next();
        assertEquals("Wrong successor picked", 2, nextthree[0]);
        assertEquals("Wrong successor picked", 3, nextthree[1]);
        int[] nextfour = iter.next();
        assertEquals("Wrong successor picked", 5, nextfour[0]);
        assertEquals("Wrong successor picked", 6, nextfour[1]);
        int[] nextfive = iter.next();
        assertEquals("Wrong successor picked", 5, nextfive[0]);
        assertEquals("Wrong successor picked", 8, nextfive[1]);
        int[] nextsix = iter.next();
        assertEquals("Wrong successor picked", 9, nextsix[0]);
        assertEquals("Wrong successor picked", 10, nextsix[1]);
        assertFalse(iter.hasNext());
    }
    @Test
    public void directedGraphVerticesIterationTest() {
        DirectedGraph g = new DirectedGraph();
        for (int i = 0; i < 10; i += 1) {
            g.add();
        }
        g.add(1, 2);
        g.add(1, 3);
        g.add(5, 6);
        g.add(5, 8);
        Iteration<Integer> iter = g.vertices();
        g.remove(3);
        for (int i = 0; i < 10; i += 1) {
            if (i == 2) {
                i += 1;
            }
            assertEquals("Vertices interation error",
                    new Integer(i + 1), iter.next());
        }
        assertFalse(iter.hasNext());
    }

    @Test
    public void depthFirstTraversalTest() {
        DirectedGraph g = new DirectedGraph();
        for (int i = 0; i < 10; i += 1) {
            g.add();
        }
        g.add(1, 2);
        g.add(1, 3);
        g.add(2, 3);
        g.add(5, 6);
        g.add(5, 8);
        g.add(9, 10);
        g.add(3, 4);
        g.add(2, 7);
        g.add(7, 5);
        g.add(8, 9);
        DepthFirstTraversal dft = new DepthFirstTraversal(g);
        dft.traverse(1);
        for (int i = 1; i < 11; i += 1) {
            assertTrue(dft.marked(i));
        }
    }

    @Test
    public void breadthFirstTraversalTest() {
        DirectedGraph g = new DirectedGraph();
        for (int i = 0; i < 10; i += 1) {
            g.add();
        }
        g.add(1, 2);
        g.add(1, 3);
        g.add(2, 3);
        g.add(5, 6);
        g.add(5, 8);
        g.add(9, 10);
        g.add(3, 4);
        g.add(2, 7);
        g.add(7, 5);
        g.add(8, 9);
        BreadthFirstTraversal bft = new BreadthFirstTraversal(g);
        bft.traverse(1);
        for (int i = 1; i < 11; i += 1) {
            assertTrue(bft.marked(i));
        }
    }
    @Test
    public void labeledGraphTest() {
        DirectedGraph g = new DirectedGraph();
        LabeledGraph<String, String> tested
                = new LabeledGraph<String, String>(g);
        for (int i = 1; i < 1001; i += 1) {
            tested.add();
            tested.add();
            tested.add(i, i + 1, "Hello " + i);
            tested.add(i + 1, i, "Bye " + i);
        }
        for (int i = 1; i < 1001; i += 1) {
            assertTrue(tested.getLabel(i, i + 1).equals("Hello " + i));
            assertTrue(tested.getLabel(i + 1, i).equals("Bye " + i));
        }
    }

    @Test
    public void shortestPathTestShort() {
        DirectedGraph g = new DirectedGraph();
        HashMap<String, int[]> map = new HashMap<>();
        LabeledGraph<String, String> tested
                = new LabeledGraph<String, String>(g);
        for (int i = 1; i < 10; i += 1) {
            tested.add();
            tested.add();
            tested.add(i, i + 1, "Hello " + i);
            tested.add(i + 1, i, "Bye " + i);
            map.put("Hello " + i, new int[]{i, i + 1});
            map.put("Bye " + i, new int[]{i + 1, i});
        }
        SimpleShortestPaths path = new SimpleShortestPaths(tested, 1, 9) {
            @Override
            protected double getWeight(int u, int v) {
                return map.get(tested.getLabel(u, v))[0]
                        - map.get(tested.getLabel(u, v))[1];
            }
        };
        path.setPaths();
        assertTrue(path.pathTo(9).equals(
                Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    protected int edgeId(int u, int v) {
        return ((u + v) * (u + v + 1) / 2) + v;
    }

    @Test
    public void shortestPathTest() {
        DirectedGraph g = new DirectedGraph();
        HashMap<String, int[]> map = new HashMap<>();
        LabeledGraph<String, String> tested
                = new LabeledGraph<String, String>(g);
        for (int i = 1; i < 2001; i += 1) {
            tested.add();
            tested.add();
            tested.add(i, i + 1, "Hello " + edgeId(i, i + 1));
            tested.add(i + 1, i, "Bye " + edgeId(i + 1, i));
            map.put("Hello " + edgeId(i, i + 1), new int[]{i, i + 1});
            map.put("Bye " + edgeId(i + 1, i), new int[]{i + 1, i});
            for (int j = i; j > 1 && j > i - 100; j -= 1) {
                tested.add();
                tested.add();
                tested.add(i, j, "Hello " + edgeId(i, j));
                tested.add(j, i, "Bye " + edgeId(j, i));
                map.put("Hello " + edgeId(i, j), new int[]{i, j});
                map.put("Bye " + edgeId(j, i), new int[]{j, i});
            }
        }
        SimpleShortestPaths path = new SimpleShortestPaths(tested, 1, 2000) {

            @Override
            protected double getWeight(int u, int v) {
                return map.get(tested.getLabel(u, v))[0]
                        - map.get(tested.getLabel(u, v))[1];
            }
        };
        path.setPaths();
        ArrayList<Integer> simpleRightPath = new ArrayList<Integer>();
        for (int i = 1; i < 901; i += 1) {
            simpleRightPath.add(i);
        }
        path.pathTo(2000);
    }
}
