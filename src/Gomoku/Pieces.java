package Gomoku;

public class Pieces implements QueryPieces{
    private static Pieces pieces = null;

    // store pieces in this two dimension array
    // 1: white   0:nil    -1:black
    private int[][] p;

    // piece info for retracting
    // PvP mode can refract for one move, PvAI mode can refract two (retractPi2 is always null in PvP mode)
    private PieceInfo retractPi1 = null;
    private PieceInfo retractPi2 = null;

    private PieceInfo winningPi1 = null;
    private PieceInfo winningPi2 = null;

    private Pieces() {
        this.p = new int[Constants.getOrder()][Constants.getOrder()];
    }

    static Pieces getInstance() {
        if (pieces == null || pieces.p.length != Constants.getOrder()) {
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

    private void resetPieceValue(int x, int y) {
        if (x >= 0 && x < Constants.getOrder() && y >= 0 && y < Constants.getOrder()) {
            p[x][y] = 0;
        }
    }

    void recordPieceForRetract(PieceInfo pi) {
        if (pi == null) {
            return;
        }

        switch (Constants.getMode()) {
            case PvAI: {
                if (retractPi1 == null) {
                    retractPi1 = pi;
                }
                else if (retractPi2 == null) {
                    retractPi2 = pi;
                }
                else {
                    retractPi1 = retractPi2;
                    retractPi2 = pi;
                }
                break;
            }
            case PvP: {
                retractPi1 = pi;
                break;
            }
            default: {
                break;
            }
        }
    }

    boolean retract() {
        if (retractPi1 == null) {
            return false;
        }

        switch (Constants.getMode()) {
            case PvAI: {
                if (retractPi2 == null) {
                    return false;
                }
                resetPieceValue(retractPi1.getX(), retractPi1.getY());
                resetPieceValue(retractPi2.getX(), retractPi2.getY());
                retractPi1 = null;
                retractPi2 = null;
                break;
            }
            case PvP: {
                resetPieceValue(retractPi1.getX(), retractPi1.getY());
                retractPi1 = null;
                break;
            }
            default: {
                System.out.println("Caught a bug in btnRetractFunc.");
                break;
            }
        }

        return true;
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
        retractPi1 = null;
        retractPi2 = null;
        winningPi1 = null;
        winningPi2 = null;
    }
}
