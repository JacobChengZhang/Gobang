package Gomoku;

public interface PieceQuery {

  int getPieceValue(int x, int y);

  boolean checkPieceValidity(int x, int y);

}
