package AI;

import Gomoku.*;

// The AI_Herald naming follows Herald, Guardian, Crusader, Archon, Legend, Ancient, and Divine which are quoted from DotA2 Rank Medals.

public class AI_Herald implements AiMove { // TODO
    private int color;
    private QueryPieces pieces = null;

    @Override
    public int getColor() {
        return color;
    }

    // this is a attempt move, will be checked before take effect
    @Override
    public PieceInfo nextMove() {
        for (int i = 0; i < Constants.getOrder(); i++) {
            for (int j = 0; j < Constants.getOrder(); j++) {
                if (Pieces.getInstance().getPieceValue(i, j) == 0) {
                    return new PieceInfo(i, j, color);
                }
            }
        }
        return new PieceInfo(0, 0, color);
    }

    public AI_Herald(int color) {
        this.color = color;
        pieces = Pieces.getInstance();
    }

}
