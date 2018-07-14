package Gomoku;

public class Piece {

  private final int x;
  private final int y;
  private final int color;
  private final boolean madeByAI;


  Piece(int x, int y, int color) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.madeByAI = false;
  }

  private Piece(int x, int y, int color, boolean madeByAI) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.madeByAI = true;
  }

  // prevent AI from pretending to be human (set madeByAI=false)
  public static Piece createPieceByAI(int x, int y, int color) {
    return new Piece(x, y, color, true);
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

  public boolean isMadeByAI() {
    return madeByAI;
  }

}
