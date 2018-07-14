package Gomoku;

import java.util.Stack;

public class Board implements PieceQuery {

  // store pieces in this two dimension array
  // 1: white   0:nil    -1:black
  private int[][] pieces;

  // piece  stack for retract in both PvAI and PvP mode
  private Stack<Piece> pieceStack = new Stack<>();

  // represent the two ends of the winning five-in-a-row pieces
  private Piece winningPiece1 = null;
  private Piece winningPiece2 = null;


  Board() {
    this.pieces = new int[Gomoku.order][Gomoku.order];
  }

  @Override
  public boolean checkPieceValidity(int x, int y) {
    return x >= 0 && x < Gomoku.order && y >= 0 && y < Gomoku.order && pieces[x][y] == 0;
  }

  @Override
  public int getPieceColor(int x, int y) {
    return pieces[x][y];
  }

  boolean setPieceColor(Piece pi) {
    if (checkPieceValidity(pi.getX(), pi.getY())) {
      pieces[pi.getX()][pi.getY()] = pi.getColor();
      return true;
    } else {
      return false;
    }
  }

  private void retractLastPiece(int x, int y) {
    if (x >= 0 && x < Gomoku.order && y >= 0 && y < Gomoku.order) {
      pieces[x][y] = 0;
      //also clearAll the two winning pieces
      setWinningPiece(null, null);

      // call boardUI to make changes relatively
      BoardUI.getInstance(null, null).removeLastPiece();
    }
  }

  /**
   * @return Piece that should be redraw(for the red dot in it)
   */
  Piece retract() throws Exception {
    if (pieceStack.empty()) {
      throw new Exception("empty stack");
    }

    switch (Gomoku.mode) {
      case PvAI: {
        if (pieceStack.peek().isMadeByAI()) {
          if (pieceStack.size() == 1) {
            throw new Exception("Can not retract anymore.");
          } else {
            Piece tempPi = pieceStack.pop();
            retractLastPiece(tempPi.getX(), tempPi.getY());
          }
        }

        if (pieceStack.empty()) {
          return null;
        } else {
          Piece tempPi = pieceStack.pop();
          retractLastPiece(tempPi.getX(), tempPi.getY());

          if (pieceStack.empty()) {
            return null;
          } else {
            return pieceStack.peek();
          }
        }
      }
      case PvP: {
        Piece tempPi = pieceStack.pop();
        retractLastPiece(tempPi.getX(), tempPi.getY());

        if (pieceStack.empty()) {
          return null;
        } else {
          return pieceStack.peek();
        }
      }
      default: {
        throw new Exception("Caught a bug in retract module.");
      }
    }
  }

  void pushPieceStack(Piece pi) {
    if (pi == null) {
      return;
    }

    pieceStack.push(pi);
  }

  void getReplayData(StringBuilder sb) {
    if (pieceStack.empty()) {
      return;
    }

    for (int i = 0; i < pieceStack.size(); i++) {
      Piece tempPi = pieceStack.elementAt(i);
      sb.append(tempPi.getX()).append(" ").append(tempPi.getY()).append("\n");
    }
  }

  void setWinningPiece(Piece pi1, Piece pi2) {
    winningPiece1 = pi1;
    winningPiece2 = pi2;
  }

  Piece getWinningPiece(int index) {
    if (index == 1) {
      return winningPiece1;
    } else {
      return winningPiece2;
    }
  }

  void clearPieces() {
    if (pieces.length == Gomoku.order) {
      for (int i = 0; i < Gomoku.order; i++) {
        for (int j = 0; j < Gomoku.order; j++) {
          pieces[i][j] = 0;
        }
      }
    } else {
      pieces = new int[Gomoku.order][Gomoku.order];
    }

    pieceStack.clear();
    setWinningPiece(null, null);
  }

}
