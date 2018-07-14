package Gomoku;

public class Referee {

  public enum GameState {
    BLACK_WIN, WHITE_WIN, DRAW, NOT_END, BLACK_GIVE_UP, WHITE_GIVE_UP,
  }

  static GameState checkIfGameEnds(Board board, Piece pi) {
    if (checkHorizontallyAndVertically(board, pi) || checkDiagonal(board, pi)) {
      if (pi.getColor() == 1) {
        return GameState.WHITE_WIN;
      } else {
        return GameState.BLACK_WIN;
      }
    } else if (!checkIfBlankExist(board)) {
      return GameState.DRAW;
    } else {
      return GameState.NOT_END;
    }
  }

  private static boolean checkIfBlankExist(Board board) {
    for (int x = 0; x < Gomoku.order; x++) {
      for (int y = 0; y < Gomoku.order; y++) {
        if (board.getPieceColor(x, y) == 0) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean checkHorizontallyAndVertically(Board board, Piece pi) {
    int pX = pi.getX();
    int pY = pi.getY();
    int pC = pi.getColor();

    // check horizontally
    int lowX = pX;
    int highX = pX;

    while ((lowX >= 1) && (board.getPieceColor(lowX - 1, pY) == pC)) {
      lowX--;
    }

    while ((highX < Gomoku.order - 1) && (board.getPieceColor(highX + 1, pY) == pC)) {
      highX++;
    }

    if (highX - lowX >= 4) {
      board.setWinningPiece(new Piece(lowX, pY, pC), new Piece(highX, pY, pC));
      return true;
    }


    // check Vertically
    int lowY = pY;
    int highY = pY;

    while ((lowY >= 1) && (board.getPieceColor(pX, lowY - 1) == pC)) {
      lowY--;
    }

    while ((highY < Gomoku.order - 1) && (board.getPieceColor(pX, highY + 1) == pC)) {
      highY++;
    }
    if (highY - lowY >= 4) {
      board.setWinningPiece(new Piece(pX, lowY, pC), new Piece(pX, highY, pC));
      return true;
    }

    // otherwise
    return false;
  }

  private static boolean checkDiagonal(Board board, Piece pi) {
    int pX = pi.getX();
    int pY = pi.getY();
    int pC = pi.getColor();

    // check '\' diagonal
    int lowX = pX;
    int lowY = pY;

    int highX = pX;
    int highY = pY;

    while ((lowX >= 1) && (lowY >= 1) && (board.getPieceColor(lowX - 1, lowY - 1) == pC)) {
      lowX--;
      lowY--;
    }

    while ((highX < Gomoku.order - 1) && (highY < Gomoku.order - 1) && (board.getPieceColor(highX + 1, highY + 1) == pC)) {
      highX++;
      highY++;
    }

    if (highX - lowX >= 4) {
      board.setWinningPiece(new Piece(lowX, lowY, pC), new Piece(highX, highY, pC));
      return true;
    }


    // check '/' diagonal
    lowX = pX;
    highY = pY;

    highX = pX;
    lowY = pY;

    while ((lowX >= 1) && (highY < Gomoku.order - 1) && (board.getPieceColor(lowX - 1, highY + 1) == pC)) {
      lowX--;
      highY++;
    }

    while ((highX < Gomoku.order - 1) && (lowY >= 1) && (board.getPieceColor(highX + 1, lowY - 1) == pC)) {
      highX++;
      lowY--;
    }

    if (highX - lowX >= 4) {
      board.setWinningPiece(new Piece(lowX, highY, pC), new Piece(highX, lowY, pC));
      return true;
    }

    // otherwise
    return false;
  }

}
