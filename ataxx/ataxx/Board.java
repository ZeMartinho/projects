package ataxx;

/* Author: P. N. Hilfinger, (C) 2008. */

import java.util.Stack;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Formatter;
import java.util.Arrays;
import java.util.List;

import static ataxx.PieceColor.*;
import static ataxx.GameException.error;

/** An Ataxx board.   The squares are labeled by column (a char value between
 *  'a' - 2 and 'g' + 2) and row (a char value between '1' - 2 and '7'
 *  + 2) or by linearized index, an integer described below.  Values of
 *  the column outside 'a' and 'g' and of the row outside '1' to '7' denote
 *  two layers of border squares, which are always blocked.
 *  This artificial border (which is never actually printed) is a common
 *  trick that allows one to avoid testing for edge conditions.
 *  For example, to look at all the possible moves from a square, sq,
 *  on the normal board (i.e., not in the border region), one can simply
 *  look at all squares within two rows and columns of sq without worrying
 *  about going off the board. Since squares in the border region are
 *  blocked, the normal logic that prevents moving to a blocked square
 *  will apply.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author Ze Martinho
 */
class Board extends Observable {

    /**
     * Number of squares on a side of the board.
     */
    static final int SIDE = 7;
    /**
     * Length of a side + an artificial 2-deep border region.
     */
    static final int EXTENDED_SIDE = SIDE + 4;

    /**
     * Number of non-extending moves before game ends.
     */
    static final int JUMP_LIMIT = 25;


    /**
     * A new, cleared board at the start of the game.
     */
    Board() {
        _board = new PieceColor[EXTENDED_SIDE * EXTENDED_SIDE];
        clear();
        undoStack = new Stack<Move>();
        changedColorStack = new Stack<Integer>();
        moveList = new LinkedList<Move>();
        _whoseMove = RED;

    }

    /**
     * A copy of B.
     */
    Board(Board b) {
        _board = b._board.clone();
    }

    /**
     * Return the linearized index of square COL ROW.
     */
    static int index(char col, char row) {
        return (row - '1' + 2) * EXTENDED_SIDE + (col - 'a' + 2);
    }

    /**
     * Return the linearized index of the square that is DC columns and DR
     * rows away from the square with index SQ.
     */
    static int neighbor(int sq, int dc, int dr) {
        return sq + dc + dr * EXTENDED_SIDE;
    }

    /**
     * Clear me to my starting state, with pieces in their initial
     * positions and no blocks.
     */
    void clear() {
        _whoseMove = RED;
        _board[index('a', '1')] = BLUE;
        _board[index('a', '7')] = RED;
        _board[index('g', '1')] = RED;
        _board[index('g', '7')] = BLUE;
        for (char i = 'a'; i <= 'g'; i += 1) {
            for (char j = '1'; j <= '7'; j += 1) {
                if ((i != 'a' && i != 'g') || (j != '1' && j != '7')) {
                    _board[index(i, j)] = EMPTY;
                }
            }
        }
        redPieces = 2;
        bluePieces = 2;
        moveList = new LinkedList<Move>();
        undoStack = new Stack<Move>();
        changedColorStack = new Stack<Integer>();
        setChanged();
        notifyObservers();
    }

    /**
     * Return true iff the game is over: i.e., if neither side has
     * any moves, if one side has no pieces, or if there have been
     * MAX_JUMPS consecutive jumps without intervening extends.
     */
    boolean gameOver() {
        if (redPieces() == 0 || bluePieces() == 0
            || (numJumps() >= JUMP_LIMIT)
            || (!canMove(BLUE) && !canMove(RED))) {
            return true;
        }
        return false;
    }

    /**
     * Return number of red pieces on the board.
     */
    int redPieces() {
        return numPieces(RED);
    }

    /**
     * Return number of blue pieces on the board.
     */
    int bluePieces() {
        return numPieces(BLUE);
    }

    /**
     * Return number of COLOR pieces on the board.
     */
    int numPieces(PieceColor color) {
        if (color == RED) {
            return redPieces;
        } else if (color == BLUE) {
            return bluePieces;
        } else {
            throw new GameException("Wrong color called on.");
        }
    }

    /**
     * Increment numPieces(COLOR) by K.
     */
    private void incrPieces(PieceColor color, int k) {
        if (color == RED) {
            redPieces += k;
        } else if (color == BLUE) {
            bluePieces += k;
        } else {
            throw new GameException("Wrong color");
        }
    }

    /**
     * The current contents of square CR, where 'a'-2 <= C <= 'g'+2, and
     * '1'-2 <= R <= '7'+2.  Squares outside the range a1-g7 are all
     * BLOCKED.  Returns the same value as get(index(C, R)).
     */
    PieceColor get(char c, char r) {
        return _board[index(c, r)];
    }

    /**
     * Return the current contents of square with linearized index SQ.
     */
    PieceColor get(int sq) {
        return _board[sq];
    }

    /**
     * Set get(C, R) to V, where 'a' <= C <= 'g', and
     * '1' <= R <= '7'.
     */
    private void set(char c, char r, PieceColor v) {
        set(index(c, r), v);
    }

    /**
     * Set square with linearized index SQ to V.  This operation is
     * undoable.
     */
    private void set(int sq, PieceColor v) {
        addUndo(sq, v);
        _board[sq] = v;
    }

    /**
     * Set square at C R to V (not undoable).
     */
    private void unrecordedSet(char c, char r, PieceColor v) {
        _board[index(c, r)] = v;
    }

    /**
     * Set square at linearized index SQ to V (not undoable).
     */
    private void unrecordedSet(int sq, PieceColor v) {
        _board[sq] = v;
    }

    /**
     * Return true iff MOVE is legal on the current board.
     */
    boolean legalMove(Move move) {
        if (move.col0() < 'a' || move.col0() > 'g'
                || move.row0() < '1' || move.row0() > '7') {
            return false;
        } else if (move.col1() < 'a' || move.col1() > 'g'
                || move.row1() < '1' || move.row1() > '7') {
            return false;
        } else if (_board[index(move.col0(), move.row0())].equals(EMPTY)) {
            return false;
        } else if (!_board[index(move.col1(), move.row1())].equals(EMPTY)) {
            return false;
        } else {
            return move.isExtend() || move.isJump() || move.isPass();
        }
    }

    /**
     * Return true iff player WHO can move, ignoring whether it is
     * that player's move and whether the game is over.
     */
    boolean canMove(PieceColor who) {
        for (char i = 'a'; i <= 'g'; i += 1) {
            for (char j = '1'; j <= '7'; j += 1) {
                if (_board[index(i, j)] == who) {
                    for (char k = (char) (i - 2); k <= (char) (i + 2); k += 1) {
                        for (char l = (char) (j - 2);
                             l <= (char) (j + 2); l += 1) {
                            if (k >= 'a' && k <= 'g' && l >= '1' && l <= '7') {
                                Move move = Move.move(i, j, k, l);
                                if (move != null
                                    && legalMove(Move.move(i, j, k, l))) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Return the color of the player who has the next move.  The
     * value is arbitrary if gameOver().
     */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /**
     * Return total number of moves and passes since the last
     * clear or the creation of the board.
     */
    int numMoves() {
        return numPass + numJumps;
    }

    /**
     * Return number of non-pass moves made in the current game since the
     * last extend move added a piece to the board (or since the
     * start of the game). Used to detect end-of-game.
     */
    int numJumps() {
        return numJumps;
    }

    /**
     * Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     * other than pass, assumes that legalMove(C0, R0, C1, R1).
     * @param c0 the original column coordinate.
     * @param r0 the original row coordinate.
     * @param c1 the new column coordinate.
     * @param r1 the new row coordinate.
     * @param started indicates whether the game has started yet.
     */
    void makeMove(char c0, char r0, char c1, char r1, boolean started) {
        if (c0 == '-') {
            makeMove(Move.pass(), started);
        } else {
            makeMove(Move.move(c0, r0, c1, r1), started);
        }
    }

    /**
     * Make the MOVE on this Board, assuming it is legal.
     * @param move is the move to perform by the board.
     * @param started is the boolean to indicate whether the
     * board should begin recording moves iff the game has started.
     */
    void makeMove(Move move, boolean started) {
        try {
            if (move == null) {
                System.out.println("Invalid move entered.");
                return;
            }
            if (move.isPass()) {
                _whoseMove = _whoseMove.opposite();
                return;
            }
            PieceColor mover = _board[index(move.col0(), move.row0())];
            if (started) {
                moveList.add(move);
            }
            assert legalMove(move);
            assert mover.equals(whoseMove());
            if (move.isPass()) {
                pass();
                return;
            } else if (move.isJump()) {
                numJumps += 1;
                _board[index(move.col0(), move.row0())] = EMPTY;
            }
            if (move.isExtend()) {
                numJumps = 0;
                if (mover == RED) {
                    redPieces += 1;
                } else {
                    bluePieces += 1;
                }
            }
            _board[index(move.col1(), move.row1())] = mover;
            changedColorStack.push(1000);
            for (char i = (char) (move.col1() - 1);
                 i < move.col1() + 2; i += 1) {
                for (char j = (char) (move.row1() - 1);
                     j < move.row1() + 2; j += 1) {
                    int ind = index(i, j);
                    if (_board[ind] == mover.opposite()) {
                        _board[ind] = mover;
                        changedColorStack.push(ind);
                        if (mover == RED) {
                            redPieces += 1;
                            bluePieces -= 1;
                        } else {
                            bluePieces += 1;
                            redPieces -= 1;
                        }
                    }
                }
            }
            PieceColor opponent = _whoseMove.opposite();
            _whoseMove = opponent;
            undoStack.push(move);
            setChanged();
            notifyObservers();
        } catch (AssertionError error) {
            System.out.println("Invalid move command.");
        }
    }

    /**
     * Update to indicate that the current player passes, assuming it
     * is legal to do so.  The only effect is to change whoseMove().
     */
    void pass() {
        assert !canMove(_whoseMove);
        _whoseMove = _whoseMove.opposite();
        numPass += 1;
        setChanged();
        notifyObservers();
    }

    /**
     * Returns the number of possible moves for a particular piece
     * of color 'who'.
     * @param c is the column examined.
     * @param r is the row examined.
     * @return number of possible moves for a  piece.
     */
    int possibleMoves(char c, char r) {
        int possibleMoves = 0;
        for (char i = (char) (c - 2); i <= (char) (c + 2); i += 1) {
            for (char j = (char) (r - 2); j <= (char) (r + 2); j += 1) {
                if (i >= 'a' && i <= 'g' && j >= '1' && j <= '7') {
                    Move move = Move.move(c, r, i, j);
                    if (move != null && legalMove(move)) {
                        possibleMoves += 1;
                    }
                }
            }
        }
        return possibleMoves;
    }

    /**
     * Returns the total number of possible legal moves a
     * player of color 'who' can make.
     * @param who is the player for which the total possible number
     *            of moves is returned.
     * @return total number of possible moves for a player.
     */
    int totalPossibleMoves(PieceColor who) {
        int totalPossibleMoves = 0;
        for (char c = 'a'; c <= 'g'; c += 1) {
            for (char r = '1'; r <= '7'; r += 1) {
                if (_board[index(c, r)] == who) {
                    totalPossibleMoves += possibleMoves(c, r);
                }
            }
        }
        return totalPossibleMoves;
    }

    /** Returns the number of total possible moves for a particular player.
     *
     * @param sense is RED iff sense = 1 and BLUE iff sense = -1.
     * @return the total number of possible moves for sense.
     */
    int totalPossibleMoves(int sense) {
        PieceColor who = null;
        if (sense == 1) {
            who = RED;
        } else if (sense == -1) {
            who = BLUE;
        }
        return totalPossibleMoves(who);
    }

    /** Undo the last move. */
    void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        Move undid = undoStack.pop();
        if (undid.isJump()) {
            _board[index(undid.col0(), undid.row0())] = _whoseMove.opposite();
        } else if (undid.isExtend()) {
            if (_whoseMove.opposite() == RED) {
                redPieces -= 1;
            } else {
                bluePieces -= 1;
            }
        }
        _board[index(undid.col1(), undid.row1())] = EMPTY;
        while (true) {
            int ind = changedColorStack.pop();
            if (ind == 1000) {
                break;
            } else {
                _board[ind] = _whoseMove;
                if (_whoseMove == RED) {
                    redPieces += 1;
                    bluePieces -= 1;
                } else {
                    bluePieces += 1;
                    redPieces -= 1;
                }
            }
        }
        PieceColor opponent = _whoseMove.opposite();
        _whoseMove = opponent;
        setChanged();
        notifyObservers();
    }

    /** Indicate beginning of a move in the undo stack. */
    private void startUndo() {
    }

    /** Add an undo action for changing SQ to NEWCOLOR on current
     *  board. */
    private void addUndo(int sq, PieceColor newColor) {
    }

    /** Sets board instance variable _whoseMove to the PieceColor given.
     * @param who is the PieceColor to which _whoseMove is set.
     * */
    void setWhoseMove(PieceColor who) {
        _whoseMove = who;
    }

    /** Return true iff it is legal to place a block at C R. */
    boolean legalBlock(char c, char r) {
        if ((c == 'a' || c == 'g')
            && (r == '1' || r == '7')) {
            return false;
        } else if (c < 'a' || c > 'g'
                || r < '1' || r > '7') {
            return false;
        } else if (_board[index(c, r)] != EMPTY
                || _board[index((char) ('a' + 'g' - c),
                        (char) ('1' + '7' - r))] != EMPTY
                || _board[index(c, (char) ('1' + '7' - r))] != EMPTY
                || _board[index((char) ('a' + 'g' - c), r)] != EMPTY) {
            return false;
        }
        return true;
    }

    /** Return true iff it is legal to place a block at CR. */
    boolean legalBlock(String cr) {
        return legalBlock(cr.charAt(0), cr.charAt(1));
    }

    /** Set a block on the square C R and its reflections across the middle
     *  row and/or column, if that square is unoccupied and not
     *  in one of the corners. Has no effect if any of the squares is
     *  already occupied by a block.  It is an error to place a block on a
     *  piece. */
    void setBlock(char c, char r) {
        if (!legalBlock(c, r)) {
            throw error("illegal block placement");
        }
        _board[index(c, r)] = BLOCKED;
        _board[index((char) ('a' + 'g' - c), (char) ('1' + '7' - r))] = BLOCKED;
        _board[index(c, (char) ('1' + '7' - r))] = BLOCKED;
        _board[index((char) ('a' + 'g' - c), r)] = BLOCKED;

        setChanged();
        notifyObservers();
    }

    /** Place a block at CR. */
    void setBlock(String cr) {
        setBlock(cr.charAt(0), cr.charAt(1));
    }

    /** Return a list of all moves made since the last clear (or start of
     *  game). */
    List<Move> allMoves() {
        return moveList;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /* .equals used only for testing purposes. */
    @Override
    public boolean equals(Object obj) {
        Board other = (Board) obj;
        return Arrays.equals(_board, other._board);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_board);
    }

    /** Return a text depiction of the board (not a dump).  If LEGEND,
     *  supply row and column numbers around the edges. */
    String toString(boolean legend) {
        Formatter out = new Formatter();
        for (char i = '7'; i >= '1'; i -= 1) {
            if (legend) {
                out.format(" " + Character.toString(i) + " ");
            } else if (!legend) {
                out.format("  ");
            }
            for (char col = 'a'; col < 'h'; col += 1) {
                if (_board[index(col, i)] == RED) {
                    out.format("r");
                } else if (_board[index(col, i)] == BLUE) {
                    out.format("b");
                } else if (_board[index(col, i)] == BLOCKED) {
                    out.format("X");
                } else {
                    out.format("-");
                }
                if (col != 'g') {
                    out.format(" ");
                } else if (col == 'g' && i != '1') {
                    out.format("%n");
                }
            }
        }
        if (legend) {
            out.format("%n");
            out.format("   ");
            for (char j = 'a'; j < 'h'; j += 1) {
                if (j == 'g') {
                    out.format(Character.toString(j));
                } else {
                    out.format("%s ", Character.toString(j));
                }
            }
        }
        return out.toString();
    }

    /** For reasons of efficiency in copying the board,
     *  we use a 1D array to represent it, using the usual access
     *  algorithm: row r, column c => index(r, c).
     *
     *  Next, instead of using a 7x7 board, we use an 11x11 board in
     *  which the outer two rows and columns are blocks, and
     *  row 2, column 2 actually represents row 0, column 0
     *  of the real board.  As a result of this trick, there is no
     *  need to special-case being near the edge: we don't move
     *  off the edge because it looks blocked.
     *
     *  Using characters as indices, it follows that if 'a' <= c <= 'g'
     *  and '1' <= r <= '7', then row c, column r of the board corresponds
     *  to board[(c -'a' + 2) + 11 (r - '1' + 2) ], or by a little
     *  re-grouping of terms, board[c + 11 * r + SQUARE_CORRECTION]. */
    private final PieceColor[] _board;

    /** Player that is on move. */
    private PieceColor _whoseMove = RED;

    /** The number of jumps that have occured consecutively. */
    private int numJumps;

    /** The total amount of passes in the game. */
    private int numPass;

    /** The number of red pieces currently on the board. */
    private int redPieces;

    /** The number of blue pieces currently on the board. */
    private int bluePieces;

    /** A stack of moves that can be undone once the game has started. */
    private Stack<Move> undoStack = new Stack<Move>();

    /** A stack of indeces for the _board array's
     * pieces whose colors were changed in the previous move. */
    private Stack<Integer> changedColorStack = new Stack<Integer>();

    /** A list of moves that have occured since the game started. */
    private LinkedList<Move> moveList = new LinkedList<Move>();

}
