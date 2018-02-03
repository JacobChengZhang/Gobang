package AI;

import Gomoku.*;

import java.util.ArrayList;

// The AI_Herald naming follows Herald, Guardian, Crusader, Archon, Legend, Ancient, and Divine which are quoted from DotA2 Rank Medals.

public class AI_Guardian implements AiMove {
    private final int color;
    private QueryPieces pieces;
    private final int[][] p; // analog pieces
    private final int order = Constants.getOrder();

    private final int depth = 3; // include 0
    private _PieceInfo bestMove = null;

    private int lowestX = 0;
    private int highestX = order - 1;
    private int lowestY = 0;
    private int highestY = order - 1;

    // searchZone's diameter = [(highestX + border) - (lowestX - border)] * [(highestY + border) - (lowestY - border)]
    private final int searchZoneBorder = 3;
    private int szLowestX = 0;
    private int szHighestX = order - 1;
    private int szLowestY = 0;
    private int szHighestY = order - 1;


    public AI_Guardian(int color, QueryPieces pieces) {
        this.color = color;
        this.pieces = pieces;
        this.p = new int[order][order];
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
        bestMove = null;

        //score_module_test();

        if(!checkIfPieceExist()) {
            return PieceInfo.createAiPieceInfo((order - 1) / 2, (order - 1) / 2, color);
        }

        alphaBeta(this.depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, 0, 0, 0);

        if (bestMove != null) {
            return PieceInfo.createAiPieceInfo(bestMove.x, bestMove.y, color);
        }
        else {
            return null;
        }
    }

    private void updateAnalogPieces() {
        // make the analog pieces up-to-date

        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                p[i][j] = pieces.getPieceValue(i, j);
            }
        }
    }

    private void updateSearchZone() {
        if (checkIfPieceExist()) {
            boolean initialized = false;

            for (int x = 0; x < order; x++) {
                for (int y = 0; y < order; y++) {
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
            szLowestX = Integer.max(0, lowestX - searchZoneBorder);

            szHighestX = Integer.min(order - 1, highestX + searchZoneBorder);

            szLowestY = Integer.max(0, lowestY - searchZoneBorder);

            szHighestY = Integer.min(order - 1, highestY + searchZoneBorder);
        }
        else {
            lowestX = 0;
            highestX = order - 1;
            lowestY = 0;
            highestY = order - 1;
            szLowestX = 0;
            szHighestX = order - 1;
            szLowestY = 0;
            szHighestY = order - 1;
        }
    }

    /**
     *
     * @param depth depth of search
     * @param alpha alpha in "alpha-beta pruning"
     * @param beta beta in "alpha-beta pruning"
     * @param isMaximizingPlayer the same to isAI
     * @param x x of the last move
     * @param y y of the last move
     * @param color color of the last move
     * @return
     */
    private int alphaBeta(int depth, int alpha, int beta, boolean isMaximizingPlayer, int x, int y, int color) {
        if (depth == 0 || willThisMoveWin(x, y ,color)) {
            return evaluate();
        }

        if (isMaximizingPlayer) {
            int v = Integer.MIN_VALUE;

            ArrayList<_PieceInfo> moveList = generateLegalMoves(this.color);
            for (_PieceInfo _pi : moveList) {
                makeOneMove(_pi.x, _pi.y, _pi.color);

                // backup search zone
                int _lowestX = lowestX;
                int _highestX = highestX;
                int _lowestY = lowestY;
                int _highestY = highestY;

                int _szLowestX = szLowestX;
                int _szHighestX = szHighestX;
                int _szLowestY = szLowestY;
                int _szHighestY = szHighestY;

                adjustSearchZone(_pi.x, _pi.y);

                int result = alphaBeta(depth - 1, alpha, beta, false, _pi.x, _pi.y, _pi.color);
                if (v < result) {

                    v = result;

                    if (depth == this.depth) {
                        bestMove = _pi;
                    }
                }

                undoOneMove(_pi.x, _pi.y);

                // recover search zone
                lowestX = _lowestX;
                highestX = _highestX;
                lowestY = _lowestY;
                highestY = _highestY;

                szLowestX = _szLowestX;
                szHighestX = _szHighestX;
                szLowestY = _szLowestY;
                szHighestY = _szHighestY;

                alpha = Integer.max(alpha, v);
                if (beta <= alpha) {
                    break;
                }
            }

            return v;
        }
        else {
            int v = Integer.MAX_VALUE;

            ArrayList<_PieceInfo> moveList = generateLegalMoves(-this.color);
            for (_PieceInfo _pi : moveList) {
                makeOneMove(_pi.x, _pi.y, _pi.color);

                // backup search zone
                int _lowestX = lowestX;
                int _highestX = highestX;
                int _lowestY = lowestY;
                int _highestY = highestY;

                int _szLowestX = szLowestX;
                int _szHighestX = szHighestX;
                int _szLowestY = szLowestY;
                int _szHighestY = szHighestY;

                adjustSearchZone(_pi.x, _pi.y);

                v = Integer.min(v, alphaBeta(depth - 1, alpha, beta, true, _pi.x, _pi.y, _pi.color));

                undoOneMove(_pi.x, _pi.y);

                // recover search zone
                lowestX = _lowestX;
                highestX = _highestX;
                lowestY = _lowestY;
                highestY = _highestY;

                szLowestX = _szLowestX;
                szHighestX = _szHighestX;
                szLowestY = _szLowestY;
                szHighestY = _szHighestY;

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

    private int evaluateOneSide(boolean isItself) {
        int score = 0;
        int color;
        if (isItself) {
            color = this.color;
        }
        else {
            color = -this.color;
        }

        score += (scoreHorizontally(color) + scoreVertically(color) + scoreDiagonally(color) + scoreBackDiagonally(color));
        return score;
    }

    private ArrayList<_PieceInfo> generateLegalMoves(int color) {
        ArrayList<_PieceInfo> moveList = new ArrayList<>();
        for (int x = szLowestX; x <= szHighestX; x++) {
            for (int y = szLowestY; y <= szHighestY; y++) {
                if (p[x][y] == 0) {
                    //moveList.add(new _PieceInfo(x, y, color, evaluateOneMove(x, y, color)));
                    moveList.add(new _PieceInfo(x, y, color));
                }
            }
        }
        return moveList;
    }

    private void makeOneMove(int x, int y, int color) {
        p[x][y] = color;
    }

    private void adjustSearchZone(int x, int y) {
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

        szLowestX = Integer.max(0, lowestX - searchZoneBorder);

        szHighestX = Integer.min(order - 1, highestX + searchZoneBorder);

        szLowestY = Integer.max(0, lowestY - searchZoneBorder);

        szHighestY = Integer.min(order - 1, highestY + searchZoneBorder);
    }

    private void undoOneMove(int x, int y) {
        p[x][y] = 0;
    }

    private int scoreHorizontally(int color) {
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

    private int scoreVertically(int color) {
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

    private int scoreDiagonally(int color) {
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

    private int scoreBackDiagonally(int color) {
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

    private boolean checkIfPieceExist() {
        for (int x = lowestX; x <= highestX; x++) {
            for (int y = lowestY; y <= highestY; y++) {
                if (p[x][y] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkIfBlankExist() {
        for (int x = 0; x < order; x++) {
            for (int y = 0; y < order; y++) {
                if (p[x][y] == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean willThisMoveWin(int pX, int pY, int pC) {
        if (pC == 0) {
            return false;
        }
        return (checkHorizontallyAndVertically(pX, pY, pC) || checkDiagonallyAndBackDiagonally(pX, pY, pC));
    }

    private boolean checkHorizontallyAndVertically(int pX, int pY, int pC) {
        // check horizontally
        int lowX = pX;
        int highX = pX;

        for ( ; (lowX >= 1) && (p[lowX - 1][pY] == pC); lowX--) {
        }

        for ( ; (highX < order - 1) && (p[highX + 1][pY] == pC); highX++) {
        }
        if (highX - lowX >= 4) {
            return true;
        }



        // check Vertically
        int lowY = pY;
        int highY = pY;

        for ( ; (lowY >= 1) && (p[pX][lowY - 1] == pC); lowY--) {
        }

        for ( ; (highY < order - 1) && (p[pX][highY + 1] == pC); highY++) {
        }
        return highY - lowY >= 4;

        // otherwise
    }

    private boolean checkDiagonallyAndBackDiagonally(int pX, int pY, int pC) {
        // check '\' diagonal
        int lowX = pX;
        int lowY = pY;

        int highX = pX;
        int highY = pY;

        for ( ; (lowX >= 1) && (lowY >= 1) && (p[lowX - 1][lowY - 1] == pC); lowX--, lowY--) {
        }

        for (; (highX < order - 1) && (highY < order - 1) && (p[highX + 1][highY + 1] == pC); highX++, highY++) {
        }
        if (highX - lowX >= 4) {
            return true;
        }


        // check '/' diagonal
        lowX = pX;
        highY = pY;

        highX = pX;
        lowY = pY;

        for ( ; (lowX >= 1) && (highY < order - 1) && (p[lowX - 1][highY + 1] == pC); lowX--, highY++) {
        }

        for ( ; (highX < order - 1) && (lowY >= 1) && (p[highX + 1][lowY - 1] == pC); highX++, lowY--) {
        }
        return highX - lowX >= 4;

        // otherwise
    }

    private boolean checkPieceValidity(int x, int y) {
        return (x >= 0 && x < order && y >= 0 && y < order && p[x][y] == 0);
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
        System.out.println(14000 == scoreBackDiagonally(color));
    }

    class _PieceInfo {
        final int x;
        final int y;
        final int color;
        _PieceInfo(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }



    @Deprecated
    class Combo {
        final int length;
        final int quality; // 0: no side open, 1: one side open, 2: both side open

        Combo(int length, int quality) {
            this.length = length;
            this.quality = quality;
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
    @Deprecated
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

                for ( ; (highY < order - 1) && (p[pX][highY + 1] == pC); highY++) {
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

                for ( ; (highX < order - 1) && (p[highX + 1][pY] == pC); highX++) {
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

                for ( ; (lowX >= 1) && (highY < order - 1) && (p[lowX - 1][highY + 1] == pC); lowX--, highY++) {
                }

                for ( ; (highX < order - 1) && (lowY >= 1) && (p[highX + 1][lowY - 1] == pC); highX++, lowY--) {
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

                for ( ; (highX < order - 1) && (highY < order - 1) && (p[highX + 1][highY + 1] == pC); highX++, highY++) {
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

    @Deprecated
    private void printAnalogPieces() {
        for (int j = 0; j < order; j++) {
            for (int i = 0; i < order; i++) {
                System.out.print(p[i][j] + " ");
            }
            System.out.print("\n");
        }
    }

    @Deprecated
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

        if (color == this.color) { // this AI's move (significant)
            return (score + 1) ;
        }
        else {
            return score;
        }
    }
}
