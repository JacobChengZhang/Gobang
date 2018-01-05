public class Pieces {
    private static Pieces pieces = null;

    // store pieces in this two dimension array
    // 1: white   0:nil    -1:black
    private int[][] p;

    private Pieces() {
        this.p = new int[Constants.order][Constants.order];
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

//    private void debugPrint() {
//        for (int i = 0; i < Constants.order; i++) {
//            for (int j = 0; j < Constants.order; j++) {
//                System.out.print(p[j][i] + " ");
//            }
//            System.out.print("\n");
//        }
//    }
}
