package Gomoku;

public interface QueryPieces {
    int getPieceValue(int x, int y);

    boolean checkPieceValidity(int x, int y);
}
