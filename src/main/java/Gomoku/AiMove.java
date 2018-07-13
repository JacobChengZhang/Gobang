package Gomoku;

import Gomoku.Referee.GameState;

public interface AiMove {

  Piece nextMove(); // get the next move of AI_Herald (validity of this move should be check again)

  String getName();

  int getColor(); //TODO can be removed?

  /**
   * called when game ends with a result(not ended by player)
   *
   * @param ending
   */
  void gameEnds(GameState ending);

}
