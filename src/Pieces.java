public class Pieces {
    private static Pieces pieces = null;

    // store pieces in this two dimension array
    // 1: white   0:nil    -1:black
    private int[][] p;

    private Pieces() {
        this.p = new int[Constants.getOrder()][Constants.getOrder()];
    }

    public static Pieces getInstance() {
        if (pieces == null) {
            pieces = new Pieces();
        }
        return pieces;
    }

    public boolean checkAndDraw(int x, int y, int color) {
        if (p[x][y] == 0) {
            p[x][y] = color;
            return true;
        }
        else {
            return false;
        }
    }

    public int getPieceValue(int x, int y) {
        return p[x][y];
    }

    public void clearPieces() {
        this.p = new int[Constants.getOrder()][Constants.getOrder()];
    }
}
