package Gomoku;

public interface AiMove {
    PieceInfo nextMove(); // get the next move of AI_Herald (validity of this move should be check again)

    int getColor();

    /**
     * called when game ends with a result(not ended by player)
     * @param result
     */
    void gameEnd(int result);
}
