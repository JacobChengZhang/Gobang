package Gomoku;

public class Pieces implements QueryPieces{
    private static Pieces pieces = null;

    // store pieces in this two dimension array
    // 1: white   0:nil    -1:black
    private int[][] p;

    private PieceInfo winningPi1 = null;
    private PieceInfo winningPi2 = null;

    private Pieces() {
        this.p = new int[Constants.getOrder()][Constants.getOrder()];
    }

    static Pieces getInstance() {
        if (pieces == null) {
            pieces = new Pieces();
        }
        return pieces;
    }

    @Override
    public boolean checkPieceValidity(int x, int y) {
        return (x >= 0 && x < Constants.getOrder() && y >= 0 && y < Constants.getOrder() && p[x][y] == 0);
    }

    @Override
    public int getPieceValue(int x, int y) {
        return p[x][y];
    }

    boolean setPieceValue(PieceInfo pi) {
        if (checkPieceValidity(pi.getX(), pi.getY())) {
            p[pi.getX()][pi.getY()] = pi.getColor();
            return true;
        }
        else {
            return false;
        }
    }

    void setWinningPieceInfo(PieceInfo pi1, PieceInfo pi2) {
        winningPi1 = pi1;
        winningPi2 = pi2;
    }

    PieceInfo getWinningPieceInfo(int index) {
        if (index == 1) {
            return winningPi1;
        }
        else if (index == 2) {
            return winningPi2;
        }
        else {
            return null;
        }
    }

    void clearPieces() {
        this.p = new int[Constants.getOrder()][Constants.getOrder()];
        winningPi1 = null;
        winningPi2 = null;
    }
}
