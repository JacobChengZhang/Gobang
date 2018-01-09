public class AI{ // TODO
    private int color;

    public PieceInfo nextMove() {
        for (int i = 0; i < Constants.getOrder(); i++) {
            for (int j = 0; j < Constants.getOrder(); j++) {
                if (Pieces.getInstance().getPieceValue(i, j) == 0) {
                    PieceInfo pi = new PieceInfo(i, j, color);
                    Pieces.getInstance().checkAndSet(pi);
                    return pi;
                }
            }
        }
        return new PieceInfo(0, 0, color);
    }

    AI(int color) {
        this.color = color;
    }

}
