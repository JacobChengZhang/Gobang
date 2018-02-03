package Gomoku;

import java.util.Stack;

public class Pieces implements QueryPieces{
    // store pieces in this two dimension array
    // 1: white   0:nil    -1:black
    private int[][] p;

    // piece info stack for retract in both PvAI and PvP mode
    private Stack<PieceInfo> retractStack = new Stack<>();

    private PieceInfo winningPi1 = null;
    private PieceInfo winningPi2 = null;

    Pieces() {
        this.p = new int[Constants.getOrder()][Constants.getOrder()];
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
            //also make winningPi disappear
            setWinningPieceInfo(null, null);

            // call board to make changes relatively
            Board.getInstance().removeLastPiece();
        }
    }

    void piecePushStack(PieceInfo pi) {
        if (pi == null) {
            return;
        }

        retractStack.push(pi);
    }

    /**
     * @return PieceInfo that should be redraw(for the red dot in it)
     */
    PieceInfo retract() throws Exception{
        if (retractStack.empty()) {
            throw new Exception("empty stack");
        }

        switch (Constants.getMode()) {
            case PvAI: {
                if (retractStack.peek().isAiMade()) {
                    if (retractStack.size() == 1) {
                        throw new Exception("Can not retract anymore.");
                    }
                    else {
                        PieceInfo tempPi = retractStack.pop();
                        resetPieceValue(tempPi.getX(), tempPi.getY());
                    }
                }

                if (retractStack.empty()) {
                    return null;
                }
                else {
                    PieceInfo tempPi = retractStack.pop();
                    resetPieceValue(tempPi.getX(), tempPi.getY());

                    if (retractStack.empty()) {
                        return null;
                    }
                    else {
                        return retractStack.peek();
                    }
                }
            }
            case PvP: {
                PieceInfo tempPi = retractStack.pop();
                resetPieceValue(tempPi.getX(), tempPi.getY());

                if (retractStack.empty()) {
                    return null;
                }
                else {
                    return retractStack.peek();
                }
            }
            default: {
                throw new Exception("Caught a bug in retract module.");
            }
        }
    }

    void getReplayData(StringBuilder sb) {
        if (retractStack.empty()) {
            return;
        }

        for(int i = 0; i < retractStack.size(); i++) {
            PieceInfo tempPi = retractStack.elementAt(i);
            sb.append(tempPi.getX()).append(" ").append(tempPi.getY()).append("\n");
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
        retractStack.clear();
        setWinningPieceInfo(null, null);
    }
}
