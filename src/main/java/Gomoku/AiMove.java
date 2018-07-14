package Gomoku;

import Gomoku.Referee.GameState;


public interface AiMove {

  // setColor and setPieceQuery will be called instantly after aiMove was created
  // set the color of AI
  // -1: Black    1:White
  void setColor(int color);

  // set the pieceQuery of AI
  // AI can use pieceQuery to make essential queries.
  void setPieceQuery(PieceQuery pq);

  // get the next move of AI
  Piece nextMove();

  // get the name of AI
  String getName();

  // will be called when game ends with a result (not droping out by human)
  void gameEnds(GameState ending);

}
