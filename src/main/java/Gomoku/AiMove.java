package Gomoku;

public interface AiMove {
    PieceInfo nextMove(); // get the next move of AI_Herald (validity of this move should be check again)

    int getColor();
}
