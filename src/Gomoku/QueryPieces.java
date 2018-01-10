package Gomoku;

public interface QueryPieces {
    int getPieceValue(int x, int y);

    boolean checkPieceValidity(PieceInfo pi);
}
