package Gomoku;

public class PieceInfo {
    private final int x;
    private final int y;
    private final int color;
    private final boolean isAiMade;

    PieceInfo(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.isAiMade = false;
    }

    PieceInfo(int x, int y, int color, boolean isAiMade) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.isAiMade = isAiMade;
    }

    public static PieceInfo createAiPieceInfo(int x, int y, int color) {
        return new PieceInfo(x, y, color, true);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getColor() {
        return color;
    }

    public boolean isAiMade() {
        return isAiMade;
    }
}
