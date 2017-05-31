package ataxx;

import static ataxx.PieceColor.*;

/** A Player that receives its moves from its Game's getMoveCmnd method.
 *  @author Ze Martinho
 */
class Manual extends Player {

    /** A Player that will play MYCOLOR on GAME, taking its moves from
     *  GAME. */
    Manual(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        if (myColor() == RED) {
            setMoveArray(game().getMoveCmnd("Red: ").operands());
        } else if (myColor() == BLUE) {
            setMoveArray(game().getMoveCmnd("Blue: ").operands());
        }
        Move move = Move.move(moveArray[0].charAt(0),
                moveArray[1].charAt(0), moveArray[2].charAt(0),
                moveArray[3].charAt(0));
        if (move != null && board().legalMove(move)) {
            return move;
        } else {
            return null;
        }
    }

    /** An array of Strings with the move coordinates for the manual player.
     *
      * @param arg the String array to be set as the move.
     */
    void setMoveArray(String[] arg) {
        moveArray = arg;
    }

    /** A constantly changing string array with the move coordinates
     * of this manual player.
     */
    private String[] moveArray;

}
