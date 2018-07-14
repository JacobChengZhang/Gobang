package Gomoku;

public interface PieceQuery {

  int getPieceColor(int x, int y);

  boolean checkPieceValidity(int x, int y);

}
