package ataxx;

import org.junit.Test;
import static org.junit.Assert.*;
import static ataxx.Move.*;

/** Tests of the Board class.
 *  @author
 */
public class BoardTest {

    private static final String[]
        GAME1 = { "a7-b7", "a1-a2",
                  "a7-a6", "a2-a3",
                  "a6-a5", "a3-a4" };

    private static final String[]
        GAME2 = { "a7-b7", "a1-b1",
            "g1-f1", "b1-c2",
            "b7-c6", "c2-d1", "c6-d6",
            "d1-d2", "g1-e2", "g7-f7",
            "f1-g1", "f7-d7", "g1-g3",
            "d7-e6", "f1-g1", "g7-f7",
            "g3-f4", "c2-a3", "f4-e4",
            "a3-b4" };

    private static void makeMoves(Board b, String[] moves) {
        for (String s : moves) {
            b.makeMove(s.charAt(0), s.charAt(1),
                       s.charAt(3), s.charAt(4), false);
        }
    }

    @Test public void testUndo() {
        Board b0 = new Board();
        Board b1 = new Board(b0);
        makeMoves(b0, GAME1);
        Board b2 = new Board(b0);
        for (int i = 0; i < GAME1.length; i += 1) {
            b0.undo();
        }
        assertEquals("failed to return to start", b1, b0);
        makeMoves(b0, GAME1);
        assertEquals("second pass failed to reach same position", b2, b0);
    }

    @Test public void testTotalPossibleMovesTrivial() {
        Board b0 = new Board();
        assertEquals(b0.totalPossibleMoves(PieceColor.RED), 16);
        assertEquals(b0.totalPossibleMoves(PieceColor.BLUE), 16);

    }

    @Test public void testPossibleMovesSimple() {
        Board b0 = new Board();
        assertEquals(b0.possibleMoves('a', '7'), 8);
        assertEquals(b0.possibleMoves('g', '1') , 8);
    }

    @Test public void testTotalPossibleMoves() {
        Board b0 = new Board();
        b0.makeMove('a', '7', 'c', '5', false);
        b0.setBlock('b', '4');
        b0.setBlock('e', '7');
        assertEquals(b0.totalPossibleMoves(PieceColor.RED), 28);
    }

    @Test public void testMoveOnBlocks() {
        Board b0 = new Board();
        b0.setBlock("a3");
        Move onABlock = move('a', '1', 'a', '3');
        assertFalse(b0.legalMove(onABlock));
    }

    @ Test public void testMoveNonEmpty() {
        Board b0 = new Board();
        b0.makeMove('a', '7', 'a', '5', false);
        b0.makeMove('a', '1', 'a', '3', false);
        Move onABlock = move('a', '5', 'a', '3');
        assertFalse(b0.legalMove(onABlock));
    }
    @Test public void testUndoComplex() {
        Board b0 = new Board();
        Board b1 = new Board(b0);
        makeMoves(b0, GAME2);
        Board b2 = new Board(b0);
        for (int i = 0; i < GAME2.length; i += 1) {
            b0.undo();
        }
        assertEquals("failed to return to start", b1, b0);
        makeMoves(b0, GAME2);
        assertEquals("second pass failed to reach same position", b2, b0);
    }
}
