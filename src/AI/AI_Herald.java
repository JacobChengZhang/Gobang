package AI;

import Gomoku.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

// The AI_Herald naming follows Herald, Guardian, Crusader, Archon, Legend, Ancient, and Divine which are quoted from DotA2 Rank Medals.

public class AI_Herald implements AiMove { // TODO
    private int color;
    private QueryPieces pieces = null;
    private int[][] p; // analog pieces
    private int[][] pScore;

    // searchZone's diameter = [(highestX + border) - (lowestX - border)] * [(highestY + border) - (lowestY - border)]
    private int searchZoneBorder = 3;
    private int szLowestX = 0;
    private int szHighestX = Constants.getOrder() - 1;
    private int szLowestY = 0;
    private int szHighestY = Constants.getOrder() - 1;

    public AI_Herald(int color, QueryPieces pieces) {
        this.color = color;
        this.pieces = pieces;
        this.p = new int[Constants.getOrder()][Constants.getOrder()];
        this.pScore = new int[Constants.getOrder()][Constants.getOrder()];
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public PieceInfo nextMove() {
        // this is a attempt move, will be checked before take effect

        //test();
        updateAnalogPieces();
        updateSearchZone();
        evaluateAll(true);
        return makeDecision();
    }

    private void test() {
        // TODO test
        /*
        oiioo
        oiiii
        oioio
         */

        p[0][0] = 0;
        p[1][0] = 1;
        p[2][0] = 1;
        p[3][0] = 0;
        p[4][0] = 0;
        p[0][1] = 0;
        p[1][1] = 1;
        p[2][1] = 0;
        p[3][1] = 1;
        p[4][1] = 1;
        p[0][2] = 0;
        p[1][2] = 1;
        p[2][2] = 0;
        p[3][2] = 1;
        p[4][2] = 0;

        PieceInfo pi = new PieceInfo(2, 1, 1);
        Combo combo1 = checkCombo(1, pi);
        Combo combo2= checkCombo(2, pi);
        Combo combo3= checkCombo(3, pi);
        Combo combo4 = checkCombo(4, pi);

        System.out.println(combo1.length + " " + combo1.quality);
        System.out.println(combo2.length + " " + combo2.quality);
        System.out.println(combo3.length + " " + combo3.quality);
        System.out.println(combo4.length + " " + combo4.quality);
    }

    private void updateAnalogPieces() {
        // make the analog pieces up-to-date

        for (int i = 0; i < Constants.getOrder(); i++) {
            for (int j = 0; j < Constants.getOrder(); j++) {
                if (pieces.getPieceValue(i, j) != p[i][j]) {
                    p[i][j] = pieces.getPieceValue(i, j);
                    return; // since every time only one piece is changed
                }
            }
        }
    }

    private void updateAnalogPieces(int i, int j) {
        // update current move

        p[i][j] = color;
    }

    private void updateSearchZone() {
        if (checkIfPieceExist()) {
            int lowestX = -1;
            int highestX = -1;
            int lowestY = -1;
            int highestY = -1;
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

    private void addPotentialRing(int order) {
        for (int i = order; i <= Constants.getOrder() - 1 - order; i++) {
            pScore[i][order] = order;
            pScore[i][Constants.getOrder() - 1 - order] = order;
        }

        for (int i = order + 1; i <= Constants.getOrder() - 2 - order; i++) {
            pScore[order][i] = order;
            pScore[Constants.getOrder() - 1 - order][i] = order;
        }
    }

    private void addPotentialField() {
        for (int i = 0; i < (Constants.getOrder() + 1) / 2; i++) {
            addPotentialRing(i);
        }
    }

    private int evaluateOneMove(PieceInfo pi) {
        // TODO parameters of score algorithm are better to be stored in file, which makes it be able to learn like a truly AI.
        int score = 0;

        Combo combo1 = checkCombo(1, pi);
        Combo combo2 = checkCombo(2, pi);
        Combo combo3 = checkCombo(3, pi);
        Combo combo4 = checkCombo(4, pi);

        ArrayList<Combo> arr = new ArrayList<>();
        arr.add(combo1);
        arr.add(combo2);
        arr.add(combo3);
        arr.add(combo4);

        Collections.sort(arr, new Comparator<Combo>() {
            @Override
            public int compare(Combo o1, Combo o2) {
                if (o1.length > o2.length) {
                    return -1;
                }
                else if (o1.length < o2.length) {
                    return 1;
                }
                else {
                    if (o1.quality > o2.quality) {
                        return -1;
                    }
                    else if (o1.quality < o2.quality){
                        return 1;
                    }
                    else {
                        return 0;
                    }
                }
            }
        });

        int highLength1 = arr.get(0).length;
        int quality1 = arr.get(0).quality;

        int highLength2 = arr.get(1).length;
        int quality2 = arr.get(1).quality;

        switch (highLength1) {
            case 5: {
                score += 1000;
                break;
            }
            case 4: {
                switch (quality1) {
                    case 2: {
                        score += 90;
                        break;
                    }
                    case 1: {
                        score += 50;
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
                        score += 40;
                        break;
                    }
                    case 1: {
                        score += 30;
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
                break;
            }
        }

        switch (highLength2) {
            case 5: {
                score += 1000;
                break;
            }
            case 4: {
                switch (quality2) {
                    case 2: {
                        score += 90;
                        break;
                    }
                    case 1: {
                        score += 50;
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
                        score += 40;
                        break;
                    }
                    case 1: {
                        score += 30;
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
                break;
            }
        }

        if (pi.getColor() == color) { // this AI's move (significant)
            return score + 1;
        }
        else {
            return score;
        }
    }

    private void evaluateAll(boolean isForBothSide) {
        clearPScore();

        addPotentialField();

        for (int x = szLowestX; x <= szHighestX; x++) {
            for (int y = szLowestY; y <= szHighestY; y++) {
                if (p[x][y] == 0) {
                    pScore[x][y] += evaluateOneMove(new PieceInfo(x, y, color));

                    if (isForBothSide) {
                        pScore[x][y] += evaluateOneMove(new PieceInfo(x, y, -color));
                    }
                }
            }
        }
    }

    private PieceInfo makeDecision() {
        int resultX = -1;
        int resultY = -1;
        int highestScore = -1;
        for (int x = szLowestX; x <= szHighestX; x++) {
            for (int y = szLowestY; y <= szHighestY; y++) {
                if (pScore[x][y] > highestScore) {
                    highestScore = pScore[x][y];
                    resultX = x;
                    resultY = y;
                }
            }
        }

        // this step is necessary
        updateAnalogPieces(resultX, resultY);

        return new PieceInfo(resultX, resultY, color);
    }

    private boolean willThisMoveWin(PieceInfo pi) {
        return (checkHorizontallyAndVertically(pi) || checkDiagonal(pi));
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

    /**
     * As for direction:
     * 1 for '|'
     * 2 for '-'
     * 3 for '/'
     * 4 for '\'
     * @param direction
     * @return Combo
     */
    private Combo checkCombo(int direction, PieceInfo pi) {
        if (direction < 1 || direction > 4) {
            System.out.println("Wrong direction.");
            return null;
        }

        int pX = pi.getX();
        int pY = pi.getY();
        int pC = pi.getColor();

        switch (direction) {
            case 1: {
                int lowY = pY;
                int highY = pY;

                for ( ; (lowY >= 1) && (p[pX][lowY - 1] == pC); lowY--) {
                }

                for ( ; (highY < Constants.getOrder() - 1) && (p[pX][highY + 1] == pC); highY++) {
                }

                int quality = 0;
                if (pieces.checkPieceValidity(pX, lowY - 1)) {
                    quality++;
                }
                if (pieces.checkPieceValidity(pX, highY + 1)) {
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
                if (pieces.checkPieceValidity(lowX - 1, pY)) {
                    quality++;
                }
                if (pieces.checkPieceValidity(highX + 1, pY)) {
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
                if (pieces.checkPieceValidity(lowX - 1, highY + 1)) {
                    quality++;
                }
                if (pieces.checkPieceValidity(highX + 1, lowY - 1)) {
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

                for (; (highX < Constants.getOrder() - 1) && (highY < Constants.getOrder() - 1) && (p[highX + 1][highY + 1] == pC); highX++, highY++) {
                }

                int quality = 0;
                if (pieces.checkPieceValidity(lowX - 1, lowY - 1)) {
                    quality++;
                }
                if (pieces.checkPieceValidity(highX + 1, highY + 1)) {
                    quality++;
                }

                return new Combo(highX - lowX + 1, quality);
            }
            default: {
                return null;
            }
        }
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
        if (highY - lowY >= 4) {
            return true;
        }

        // otherwise
        return false;
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
        if (highX - lowX >= 4) {
            return true;
        }

        // otherwise
        return false;
    }

    private void printAnalogPieces() {
        for (int j = 0; j < Constants.getOrder(); j++) {
            for (int i = 0; i < Constants.getOrder(); i++) {
                System.out.print(p[i][j] + " ");
            }
            System.out.print("\n");
        }
    }

    class Combo {
        int length = 0;
        int quality = 0; // 0: no side open, 1: one side open, 2: both side open

        Combo(int length, int quality) {
            this.length = length;
            this.quality = quality;
        }
    }
}
