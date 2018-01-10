package Gomoku;

public interface AiMove {
    // get the next move of AI_Herald (validity of this move should be check again)
    PieceInfo nextMove();

    int getColor();
}
