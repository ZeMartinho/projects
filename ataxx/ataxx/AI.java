package ataxx;


import java.util.LinkedList;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;


/** A Player that computes its own moves.
 *  @author Ze Martinho
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        if (!board().canMove(myColor())) {
            game().reportMove(myColor().toString() + " passes.");
            return Move.pass();
        } else {
            Move move = findMove();
            game().reportMove(myColor().toString()
                    + " moves " + move.toString());
            return move;
        }
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (myColor() == RED) {
            findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            b.setWhoseMove(BLUE);
            findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** Used to communicate best moves found by findMove, when asked for. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value >= BETA if SENSE==1,
     *  and minimal value or value <= ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels before using a static estimate. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        int bestSoFar = -sense * INFTY;
        if (saveMove) {
            _lastFoundMove = null;
            if ((sense == 1 && board.canMove(RED))
                    || (sense == -1 && board.canMove(BLUE))) {
                _lastFoundMove = randomMove(sense, board);
                board.makeMove(_lastFoundMove, true);
            }
            bestSoFar = staticScore(board);
            board.undo();
        } else if (board.gameOver() || depth == 0) {
            return staticScore(board);
        }
        for (int i = 0; ((i < board.totalPossibleMoves(sense))
                && (i < MOVELIMIT)); i += 1) {
            Move move = null;
            if ((sense == 1 && board.canMove(RED))
                    || (sense == -1 && board.canMove(BLUE))) {
                move = randomMove(sense, board);
            } else {
                return staticScore(board);
            }
            board.makeMove(move, true);
            int response = findMove(board,
                    depth - 1, false, -sense, alpha, beta);
            if ((sense == 1 && bestSoFar < response)
                    || (sense == -1 && bestSoFar > response)) {
                bestSoFar = response;
                if (saveMove) {
                    _lastFoundMove = move;
                }
                if (sense == 1) {
                    alpha = max(alpha, response);
                } else if (sense == -1) {
                    beta = min(beta, response);
                }
                if (alpha >= beta) {
                    board.undo();
                    break;
                }
            }
            if (!board.allMoves().isEmpty()) {
                board.undo();
            }
        }
        return bestSoFar;
    }
    /** A function to generate a random move for RED if sense equals 1,
     * and blue if sense equals -1.
     * @param sense indicates whether the move is for RED (-1) or BLUE (1)
     * @param board indicates which board to assess for this move.
     * @return a move for player sense on board board.
     * */
    Move randomMove(int sense, Board board) {
        Move randMove;
        PieceColor who = null;
        if (sense == 1) {
            who = RED;
        } else if (sense == -1) {
            who = BLUE;
        }
        int col = game().nextRandom(7);
        int row = game().nextRandom(7);
        char c = (char) ('a' + (char) col);
        char r = (char) ('1' + (char) row);
        if (board.get(c, r) == who) {
            int coin = game().nextRandom(2);
            if (coin == 1) {
                for (char i = (char) (c - 1); i <= (char) (c + 1); i += 1) {
                    for (char j = (char) (r - 1); j <= (char) (r + 1); j += 1) {
                        randMove = Move.move(c, r, i, j);
                        if (randMove != null && board.legalMove(randMove)) {
                            return randMove;
                        }
                    }
                }
            } else {
                for (char i = (char) (c - 2); i <= (char) (c + 2); i += 1) {
                    for (char j = (char) (r - 2); j <= (char) (r + 2); j += 1) {
                        randMove = Move.move(c, r, i , j);
                        if (randMove != null && board.legalMove(randMove)) {
                            return randMove;
                        }
                    }
                }
            }
        }
        return randomMove(sense, board);
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        if (board.gameOver()) {
            if (board.numPieces(RED) > board.numPieces(BLUE)) {
                return WINNING_VALUE;
            } else if (board.numPieces(RED) < board.numPieces(BLUE)) {
                return -WINNING_VALUE;
            } else {
                return 0;
            }
        } else {
            return (board.numPieces(RED)
                    - board.numPieces(BLUE)
                    + board.totalPossibleMoves(RED)
                    - board.totalPossibleMoves(BLUE));
        }
    }

    /** The maximum number of moves to consider in the AI. */
    private static final int MOVELIMIT = 50;
}
