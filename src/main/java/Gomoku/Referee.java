package Gomoku;

public class Referee {
    /**
     * @param pi piece info
     * @return result
     * 1    -> White wins
     * 0    -> Continue to play
     * -1   -> Black wins
     * -100 -> Draw.
     */
    static int checkWinningCondition(Pieces pieces, PieceInfo pi) {
        if (checkHorizontallyAndVertically(pieces, pi) || checkDiagonal(pieces, pi)) {
            return pi.getColor();
        }
        else if (!checkIfBlankExist(pieces)) {
            return -100;
        }
        else {
            return 0;
        }
    }

    private static boolean checkIfBlankExist(Pieces pieces) {
        for (int x = 0; x < Constants.getOrder(); x++) {
            for (int y = 0; y < Constants.getOrder(); y++) {
                if (pieces.getPieceValue(x, y) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkHorizontallyAndVertically(Pieces pieces, PieceInfo pi) {
        int pX = pi.getX();
        int pY = pi.getY();
        int pC = pi.getColor();

        // check horizontally
        int lowX = pX;
        int highX = pX;

        for ( ; (lowX >= 1) && (pieces.getPieceValue(lowX - 1, pY) == pC); lowX--) {
        }

        for ( ; (highX < Constants.getOrder() - 1) && (pieces.getPieceValue(highX + 1, pY) == pC); highX++) {
        }
        if (highX - lowX >= 4) {
            pieces.setWinningPieceInfo(new PieceInfo(lowX, pY, pC), new PieceInfo(highX, pY, pC));
            return true;
        }



        // check Vertically
        int lowY = pY;
        int highY = pY;

        for ( ; (lowY >= 1) && (pieces.getPieceValue(pX, lowY - 1) == pC); lowY--) {
        }

        for ( ; (highY < Constants.getOrder() - 1) && (pieces.getPieceValue(pX, highY + 1) == pC); highY++) {
        }
        if (highY - lowY >= 4) {
            pieces.setWinningPieceInfo(new PieceInfo(pX, lowY, pC), new PieceInfo(pX, highY, pC));
            return true;
        }

        // otherwise
        return false;
    }

    private static boolean checkDiagonal(Pieces pieces, PieceInfo pi) {
        int pX = pi.getX();
        int pY = pi.getY();
        int pC = pi.getColor();

        // check '\' diagonal
        int lowX = pX;
        int lowY = pY;

        int highX = pX;
        int highY = pY;

        for ( ; (lowX >= 1) && (lowY >= 1) && (pieces.getPieceValue(lowX - 1, lowY - 1) == pC); lowX--, lowY--) {
        }

        for (; (highX < Constants.getOrder() - 1) && (highY < Constants.getOrder() - 1) && (pieces.getPieceValue(highX + 1, highY + 1) == pC); highX++, highY++) {
        }
        if (highX - lowX >= 4) {
            pieces.setWinningPieceInfo(new PieceInfo(lowX, lowY, pC), new PieceInfo(highX, highY, pC));
            return true;
        }


        // check '/' diagonal
        lowX = pX;
        highY = pY;

        highX = pX;
        lowY = pY;

        for ( ; (lowX >= 1) && (highY < Constants.getOrder() - 1) && (pieces.getPieceValue(lowX - 1, highY + 1) == pC); lowX--, highY++) {
        }

        for ( ; (highX < Constants.getOrder() - 1) && (lowY >= 1) && (pieces.getPieceValue(highX + 1, lowY - 1) == pC); highX++, lowY--) {
        }
        if (highX - lowX >= 4) {
            pieces.setWinningPieceInfo(new PieceInfo(lowX, highY, pC), new PieceInfo(highX, lowY, pC));
            return true;
        }

        // otherwise
        return false;
    }
}

// TODO add ban rules support