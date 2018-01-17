package AI;

import Gomoku.*;

import java.util.ArrayList;

// The AI_Herald naming follows Herald, Guardian, Crusader, Archon, Legend, Ancient, and Divine which are quoted from DotA2 Rank Medals.

public class AI_Guardian implements AiMove {
    private final int color;
    private QueryPieces pieces = null;
    private final int[][] p; // analog pieces
    private final int[][] pScore;

    private final int depth = 3; // include 0
    private final int breadth = 10;
    private ScoredPieceInfo bestMove = null;

    int lowestX = -1;
    int highestX = -1;
    int lowestY = -1;
    int highestY = -1;

    // searchZone's diameter = [(highestX + border) - (lowestX - border)] * [(highestY + border) - (lowestY - border)]
    private final int searchZoneBorder = 3;
    private int szLowestX = 0;
    private int szHighestX = Constants.getOrder() - 1;
    private int szLowestY = 0;
    private int szHighestY = Constants.getOrder() - 1;

    public AI_Guardian(int color, QueryPieces pieces) {
        this.color = color;
        this.pieces = pieces;
        this.p = new int[Constants.getOrder()][Constants.getOrder()];
        this.pScore = new int[Constants.getOrder()][Constants.getOrder()];
    }

    private void score_module_test() {
        makeOneMove(10, 2, color);
        makeOneMove(9, 3, color);
        makeOneMove(8, 4, color);

        makeOneMove(7, 6, color);
        makeOneMove(6, 7, color);
        makeOneMove(5, 8, color);
        makeOneMove(4, 9, color);
        updateSearchZone();
        System.out.println(checkBackDiagonally(color));
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public PieceInfo nextMove() {
        // this is a attempt move, will be checked before take effect

        updateAnalogPieces();
        updateSearchZone();

        clearPScore();

        //score_module_test();
        if(!checkIfPieceExist()) {
            return PieceInfo.createAiPieceInfo((Constants.getOrder() - 1) / 2, (Constants.getOrder() - 1) / 2, color);
        }

        alphaBeta(this.depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

        return PieceInfo.createAiPieceInfo(bestMove.x, bestMove.y, color);
    }

    private int alphaBeta(int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        if (depth == 0 || !checkIfBlankExist()) {
            return evaluate();
        }

        if (isMaximizingPlayer) {
            int v = Integer.MIN_VALUE;

            ArrayList<ScoredPieceInfo> moveList = generateLegalMoves(this.color);
            for (int i = 0; i < moveList.size(); i++) {
                ScoredPieceInfo spi = moveList.get(i);
                makeOneMove(spi.x, spi.y, spi.color);
                updateSearchZone();

                int result = alphaBeta(depth - 1, alpha, beta, false);
                if (depth == this.depth && v < result) {
                    bestMove = spi;
                }
                if (v < result) {
                    v = result;
                }

                undoOneMove(spi.x, spi.y);
                updateSearchZone();

                alpha = Integer.max(alpha, v);
                if (beta <= alpha) {
                    break;
                }
            }

            return v;
        }
        else {
            int v = Integer.MAX_VALUE;

            ArrayList<ScoredPieceInfo> moveList = generateLegalMoves(-this.color);
            for (int i = 0; i < moveList.size(); i++) {
                ScoredPieceInfo spi = moveList.get(i);
                makeOneMove(spi.x, spi.y, spi.color);
                updateSearchZone();

                v = Integer.min(v, alphaBeta(depth - 1, alpha, beta, true));

                undoOneMove(spi.x, spi.y);
                updateSearchZone();

                beta = Integer.min(beta, v);
                if (beta <= alpha) {
                    break;
                }
            }

            return v;
        }
    }

    private int evaluate() {
        return evaluateOneSide(true) / 2 - evaluateOneSide(false);
    }

    private int evaluateOneSide(boolean aiItself) {
        int score = 0;
        int color;
        if (aiItself) {
            color = this.color;
        }
        else {
            color = -this.color;
        }

        score += (checkHorizontally(color) + checkVertically(color) + checkDiagonally(color) + checkBackDiagonally(color));
        return score;
    }

    private ArrayList<ScoredPieceInfo> generateLegalMoves(int color) {
        ArrayList<ScoredPieceInfo> moveList = new ArrayList<>();
        for (int x = szLowestX; x <= szHighestX; x++) {
            for (int y = szLowestY; y <= szHighestY; y++) {
                if (p[x][y] == 0) {
                    //moveList.add(new ScoredPieceInfo(x, y, color, evaluateOneMove(x, y, color)));
                    moveList.add(new ScoredPieceInfo(x, y, color, 0));
                }
            }
        }
        return moveList;
    }

    private void makeOneMove(int x, int y, int color) {
        p[x][y] = color;
    }

    private void undoOneMove(int x, int y) {
        p[x][y] = 0;
    }

    private void updateAnalogPieces() {
        // make the analog pieces up-to-date

        for (int i = 0; i < Constants.getOrder(); i++) {
            for (int j = 0; j < Constants.getOrder(); j++) {
                if (pieces.getPieceValue(i, j) != p[i][j]) {
                    p[i][j] = pieces.getPieceValue(i, j);
                }
            }
        }
    }

    private void updateSearchZone() {
        if (checkIfPieceExist()) {
            boolean initialized = false;

            for (int x = 0; x < Constants.getOrder(); x++) {
                for (int y = 0; y < Constants.getOrder(); y++) {
                    if (p[x][y] != 0) {
                        if (initialized) {
                            if (x < lowestX) {
                                lowestX = x;
                            }
                            else if (x > highestX) {
                                highestX = x;
                            }

                            if (y < lowestY) {
                                lowestY = y;
                            }
                            else if (y > highestY) {
                                highestY = y;
                            }
                        }
                        else {
                            lowestX = x;
                            highestX = x;
                            lowestY = y;
                            highestY = y;
                            initialized = true;
                        }
                    }
                }
            }

            // using "analog pieces' limit X&Y" to update "search zone's limit X&Y"
            if (lowestX - searchZoneBorder < 0) {
                szLowestX = 0;
            }
            else {
                szLowestX = lowestX - searchZoneBorder;
            }

            if (highestX + searchZoneBorder > Constants.getOrder() - 1) {
                szHighestX = Constants.getOrder() - 1;
            }
            else {
                szHighestX = highestX + searchZoneBorder;
            }

            if (lowestY - searchZoneBorder < 0) {
                szLowestY = 0;
            }
            else {
                szLowestY = lowestY - searchZoneBorder;
            }

            if (highestY + searchZoneBorder > Constants.getOrder() - 1) {
                szHighestY = Constants.getOrder() - 1;
            }
            else {
                szHighestY = highestY + searchZoneBorder;
            }
        }
        else {
            szLowestX = 0;
            szHighestX = Constants.getOrder() - 1;
            szLowestY = 0;
            szHighestY = Constants.getOrder() - 1;
        }
    }

    private void clearPScore() {
        for (int i = 0; i < Constants.getOrder(); i++) {
            for (int j = 0; j < Constants.getOrder(); j++) {
                pScore[i][j] = 0;
            }
        }
    }

    private int scoreCombo(int length, int quality) {
        int score = 0;
        switch (length) {
            case 5: {
                score += 100000;
                break;
            }
            case 4: {
                switch (quality) {
                    case 2: {
                        score += 10000;
                        break;
                    }
                    case 1: {
                        score += 4000;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case 3: {
                switch (quality) {
                    case 2: {
                        score += 4000;
                        break;
                    }
                    case 1: {
                        score += 1000;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case 2: {
                switch (quality) {
                    case 2: {
                        score += 100;
                        break;
                    }
                    case 1: {
                        score += 10;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            default: {
                if (length > 5) {
                    score += 100000;
                }
                break;
            }
        }

        return score;
    }

    private int checkHorizontally(int color) {
        int score = 0;
        for (int y = lowestY; y <= highestY; y++) {
            for (int x = lowestX; x <= highestX; ) {
                if(p[x][y] != color) {
                    x++;
                }
                else {
                    int length = 0;
                    int quality = 0;
                    length++;
                    if (checkPieceValidity(x - 1, y)) {
                        quality++;
                    }
                    x++;

                    while (x <= highestX && y <= highestY && p[x][y] == color) {
                        length++;
                        x++;
                    }

                    if (checkPieceValidity(x, y)) {
                        quality++;
                    }

                    score += scoreCombo(length, quality);
                }
            }
        }

        return score;
    }

    private int checkVertically(int color) {
        int score = 0;
        for (int x = lowestX; x <= highestX; x++) {
            for (int y = lowestY; y <= highestY; ) {
                if (p[x][y] != color) {
                    y++;
                }
                else {
                    int length = 0;
                    int quality = 0;
                    length++;
                    if (checkPieceValidity(x, y - 1)) {
                        quality++;
                    }
                    y++;

                    while (x <= highestX && y <= highestY && p[x][y] == color) {
                        length++;
                        y++;
                    }

                    if (checkPieceValidity(x, y)) {
                        quality++;
                    }

                    score += scoreCombo(length, quality);
                }
            }
        }

        return score;
    }

    private int checkDiagonally(int color) {
        int score = 0;

        int x1 = lowestX;
        int y1 = Integer.max(0, highestY - 2);
        for ( ; y1 >= lowestY; y1--) {
            int x = x1;
            int y = y1;

            while (x <= highestX && y <= highestY) {
                if (p[x][y] != color) {
                    x++;
                    y++;
                }
                else {
                    int length = 0;
                    int quality = 0;
                    length++;
                    if (checkPieceValidity(x - 1, y - 1)) {
                        quality++;
                    }
                    x++;
                    y++;

                    while (x <= highestX && y <= highestY && p[x][y] == color) {
                        length++;
                        x++;
                        y++;
                    }

                    if (checkPieceValidity(x, y)) {
                        quality++;
                    }

                    score += scoreCombo(length, quality);
                }
            }
        }


        int y2 = lowestY;
        int x2 = Integer.max(0, highestX - 2);
        for ( ; x2 >= lowestX + 1; x2--) {
            int x = x2;
            int y = y2;

            while (x <= highestX && y <= highestY) {
                if (p[x][y] != color) {
                    x++;
                    y++;
                }
                else {
                    int length = 0;
                    int quality = 0;
                    length++;
                    if (checkPieceValidity(x - 1, y - 1)) {
                        quality++;
                    }
                    x++;
                    y++;

                    while (x <= highestX && y <= highestY && p[x][y] == color) {
                        length++;
                        x++;
                        y++;
                    }

                    if (checkPieceValidity(x, y)) {
                        quality++;
                    }

                    score += scoreCombo(length, quality);
                }
            }
        }

        return score;
    }

    private int checkBackDiagonally(int color) {
        int score = 0;

        int x1 = lowestX;
        int y1 = Integer.min(highestY, lowestY + 2);
        for ( ; y1 <= highestY; y1++) {
            int x = x1;
            int y = y1;

            while (x <= highestX && y >= lowestY) {
                if (p[x][y] != color) {
                    x++;
                    y--;
                }
                else {
                    int length = 0;
                    int quality = 0;
                    length++;
                    if (checkPieceValidity(x - 1, y + 1)) {
                        quality++;
                    }
                    x++;
                    y--;

                    while (x <= highestX && y >= lowestY && p[x][y] == color) {
                        length++;
                        x++;
                        y--;
                    }

                    if (checkPieceValidity(x, y)) {
                        quality++;
                    }

                    score += scoreCombo(length, quality);
                }
            }
        }


        int y2 = highestY;
        int x2 = Integer.max(0, highestX - 2);
        for ( ; x2 >= lowestX + 1; x2--) {
            int x = x2;
            int y = y2;

            while (x <= highestX && y >= lowestY) {
                if (p[x][y] != color) {
                    x++;
                    y--;
                }
                else {
                    int length = 0;
                    int quality = 0;
                    length++;
                    if (checkPieceValidity(x - 1, y + 1)) {
                        quality++;
                    }
                    x++;
                    y--;

                    while (x <= highestX && y >= lowestY && p[x][y] == color) {
                        length++;
                        x++;
                        y--;
                    }

                    if (checkPieceValidity(x, y)) {
                        quality++;
                    }

                    score += scoreCombo(length, quality);
                }
            }
        }

        return score;
    }

    private boolean checkIfBlankExist() {
        for (int x = 0; x < Constants.getOrder(); x++) {
            for (int y = 0; y < Constants.getOrder(); y++) {
                if (p[x][y] == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkIfPieceExist() {
        for (int x = 0; x < Constants.getOrder(); x++) {
            for (int y = 0; y < Constants.getOrder(); y++) {
                if (p[x][y] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkPieceValidity(int x, int y) {
        return (x >= 0 && x < Constants.getOrder() && y >= 0 && y < Constants.getOrder() && p[x][y] == 0);
    }

    class Combo {

        final int length;
        final int quality; // 0: no side open, 1: one side open, 2: both side open
        Combo(int length, int quality) {
            this.length = length;
            this.quality = quality;
        }

    }

    class ScoredPieceInfo {

        final int x;
        final int y;
        final int color;
        final int score;
        ScoredPieceInfo(int x, int y, int color, int score) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.score = score;
        }

    }







    /**
     * @param direction
     * * 1 for '|'
     * 2 for '-'
     * 3 for '/'
     * 4 for '\'
     *
     * @return Combo
     */
    private Combo checkCombo(int direction, int pX, int pY, int pC) {
        if (direction < 1 || direction > 4) {
            System.out.println("Wrong direction.");
            return null;
        }

        switch (direction) {
            case 1: {
                int lowY = pY;
                int highY = pY;

                for ( ; (lowY >= 1) && (p[pX][lowY - 1] == pC); lowY--) {
                }

                for ( ; (highY < Constants.getOrder() - 1) && (p[pX][highY + 1] == pC); highY++) {
                }

                int quality = 0;
                if (checkPieceValidity(pX, lowY - 1)) {
                    quality++;
                }
                if (checkPieceValidity(pX, highY + 1)) {
                    quality++;
                }

                return new Combo(highY - lowY + 1, quality);
            }
            case 2: {
                int lowX = pX;
                int highX = pX;

                for ( ; (lowX >= 1) && (p[lowX - 1][pY] == pC); lowX--) {
                }

                for ( ; (highX < Constants.getOrder() - 1) && (p[highX + 1][pY] == pC); highX++) {
                }

                int quality = 0;
                if (checkPieceValidity(lowX - 1, pY)) {
                    quality++;
                }
                if (checkPieceValidity(highX + 1, pY)) {
                    quality++;
                }

                return new Combo(highX - lowX + 1, quality);
            }
            case 3: {
                int lowX = pX;
                int highY = pY;

                int highX = pX;
                int lowY = pY;

                for ( ; (lowX >= 1) && (highY < Constants.getOrder() - 1) && (p[lowX - 1][highY + 1] == pC); lowX--, highY++) {
                }

                for ( ; (highX < Constants.getOrder() - 1) && (lowY >= 1) && (p[highX + 1][lowY - 1] == pC); highX++, lowY--) {
                }

                int quality = 0;
                if (checkPieceValidity(lowX - 1, highY + 1)) {
                    quality++;
                }
                if (checkPieceValidity(highX + 1, lowY - 1)) {
                    quality++;
                }

                return new Combo(highX - lowX + 1, quality);
            }
            case 4: {
                int lowX = pX;
                int lowY = pY;

                int highX = pX;
                int highY = pY;

                for ( ; (lowX >= 1) && (lowY >= 1) && (p[lowX - 1][lowY - 1] == pC); lowX--, lowY--) {
                }

                for ( ; (highX < Constants.getOrder() - 1) && (highY < Constants.getOrder() - 1) && (p[highX + 1][highY + 1] == pC); highX++, highY++) {
                }

                int quality = 0;
                if (checkPieceValidity(lowX - 1, lowY - 1)) {
                    quality++;
                }
                if (checkPieceValidity(highX + 1, highY + 1)) {
                    quality++;
                }

                return new Combo(highX - lowX + 1, quality);
            }
            default: {
                return null;
            }
        }
    }

    private void printAnalogPieces() {
        for (int j = 0; j < Constants.getOrder(); j++) {
            for (int i = 0; i < Constants.getOrder(); i++) {
                System.out.print(p[i][j] + " ");
            }
            System.out.print("\n");
        }
    }

    private boolean willThisMoveWin(PieceInfo pi) {
        return (checkHorizontallyAndVertically(pi) || checkDiagonal(pi));
    }

    private boolean checkHorizontallyAndVertically(PieceInfo pi) {
        int pX = pi.getX();
        int pY = pi.getY();
        int pC = pi.getColor();

        // check horizontally
        int lowX = pX;
        int highX = pX;

        for ( ; (lowX >= 1) && (p[lowX - 1][pY] == pC); lowX--) {
        }

        for ( ; (highX < Constants.getOrder() - 1) && (p[highX + 1][pY] == pC); highX++) {
        }
        if (highX - lowX >= 4) {
            return true;
        }



        // check Vertically
        int lowY = pY;
        int highY = pY;

        for ( ; (lowY >= 1) && (p[pX][lowY - 1] == pC); lowY--) {
        }

        for ( ; (highY < Constants.getOrder() - 1) && (p[pX][highY + 1] == pC); highY++) {
        }
        return highY - lowY >= 4;

        // otherwise
    }

    private boolean checkDiagonal(PieceInfo pi) {
        int pX = pi.getX();
        int pY = pi.getY();
        int pC = pi.getColor();

        // check '\' diagonal
        int lowX = pX;
        int lowY = pY;

        int highX = pX;
        int highY = pY;

        for ( ; (lowX >= 1) && (lowY >= 1) && (p[lowX - 1][lowY - 1] == pC); lowX--, lowY--) {
        }

        for (; (highX < Constants.getOrder() - 1) && (highY < Constants.getOrder() - 1) && (p[highX + 1][highY + 1] == pC); highX++, highY++) {
        }
        if (highX - lowX >= 4) {
            return true;
        }


        // check '/' diagonal
        lowX = pX;
        highY = pY;

        highX = pX;
        lowY = pY;

        for ( ; (lowX >= 1) && (highY < Constants.getOrder() - 1) && (p[lowX - 1][highY + 1] == pC); lowX--, highY++) {
        }

        for ( ; (highX < Constants.getOrder() - 1) && (lowY >= 1) && (p[highX + 1][lowY - 1] == pC); highX++, lowY--) {
        }
        return highX - lowX >= 4;

        // otherwise
    }

    private int evaluateOneMove(int x, int y, int color) {
        // TODO parameters of score algorithm are better to be stored in file, which makes it be able to learn like a truly AI.
        int score = 0;

        Combo combo1 = checkCombo(1, x, y, color);
        Combo combo2 = checkCombo(2, x, y, color);
        Combo combo3 = checkCombo(3, x, y, color);
        Combo combo4 = checkCombo(4, x, y, color);

        ArrayList<Combo> arr = new ArrayList<>();
        arr.add(combo1);
        arr.add(combo2);
        arr.add(combo3);
        arr.add(combo4);

        arr.sort((o1, o2) -> {
            if (o1.length > o2.length) {
                return -1;
            } else if (o1.length < o2.length) {
                return 1;
            } else {
                return Integer.compare(o2.quality, o1.quality);
            }
        });

        int highLength1 = arr.get(0).length;
        int quality1 = arr.get(0).quality;

        int highLength2 = arr.get(1).length;
        int quality2 = arr.get(1).quality;

        // TODO needs to be re-organized
        switch (highLength1) {
            case 5: {
                score += 10000;
                break;
            }
            case 4: {
                switch (quality1) {
                    case 2: {
                        score += 1000;
                        break;
                    }
                    case 1: {
                        score += 500;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case 3: {
                switch (quality1) {
                    case 2: {
                        score += 500;
                        break;
                    }
                    case 1: {
                        score += 100;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case 2: {
                switch (quality1) {
                    case 2: {
                        score += 20;
                        break;
                    }
                    case 1: {
                        score += 10;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            default: {
                if (highLength1 > 5) {
                    score += 10000;
                }
                break;
            }
        }

        switch (highLength2) {
            case 5: {
                score += 10000;
                break;
            }
            case 4: {
                switch (quality2) {
                    case 2: {
                        score += 1000;
                        break;
                    }
                    case 1: {
                        score += 500;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case 3: {
                switch (quality2) {
                    case 2: {
                        score += 500;
                        break;
                    }
                    case 1: {
                        score += 100;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case 2: {
                switch (quality2) {
                    case 2: {
                        score += 20;
                        break;
                    }
                    case 1: {
                        score += 10;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            default: {
                if (highLength2 > 5) {
                    score += 10000;
                }
                break;
            }
        }

        score += pScore[x][y];

        if (color == this.color) { // this AI's move (significant)
            return (score + 1) ;
        }
        else {
            return score;
        }
    }
}
