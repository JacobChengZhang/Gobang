package Gomoku;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

import static Gomoku.Utils.calcPieceCoordinate;

/**
 * control the boardUI elements with Gomoku class collaboratively
 */
class BoardUI {

  private static BoardUI boardUI = null;

  List<Line> lineListX = new ArrayList<>(30);
  List<Line> lineListY = new ArrayList<>(30);
  List<Circle> dotList = new ArrayList<>(10);

  List<Circle> pieceList = new ArrayList<>(300);
  Circle redDot = null;
  Object winAnimation = null;

  private int order;
  private Board board = null;
  private ObservableList<Node> paneBoardChildren = null;

  private BoardUI() {
  }

  static BoardUI getInstance(Board board, ObservableList<Node> paneBoardChildren) {
    if (boardUI == null) {
      boardUI = new BoardUI();
      boardUI.order = Gomoku.order;
      boardUI.board = board;
      boardUI.paneBoardChildren = paneBoardChildren;
    }

    return boardUI;
  }

  void drawDot(Piece pi) {
    Circle dot = new Circle();
    dot.setCenterX(calcPieceCoordinate(pi.getX()));
    dot.setCenterY(calcPieceCoordinate(pi.getY()));
    dot.setRadius(Configuration.dotRadius);
    dot.setFill(Color.BLACK);
    dot.setStroke(Color.BLACK);

    paneBoardChildren.add(dot);
    dotList.add(dot);
  }

  void drawPiece(Piece pi, boolean isNew) {
    final Circle p = new Circle();
    p.setCenterX(calcPieceCoordinate(pi.getX()));
    p.setCenterY(calcPieceCoordinate(pi.getY()));
    p.setRadius(Configuration.pieceRadius);
    if (pi.getColor() == 1) {
      p.setFill(Color.WHITE);
    } else {
      p.setFill(Color.BLACK);
    }
    p.setStrokeWidth(Configuration.lineWidth);
    p.setStroke(Color.BLACK);

    paneBoardChildren.add(p);
    pieceList.add(p);

    if (isNew) {
      if (redDot == null) {
        redDot = new Circle();
        redDot.setCenterX(calcPieceCoordinate(pi.getX()));
        redDot.setCenterY(calcPieceCoordinate(pi.getY()));

//            // red ring style
//            redDot.setRadius(Configuration.pieceRadius);
//            redDot.setFill(Color.TRANSPARENT);
//            redDot.setStrokeWidth(Configuration.lineWidth * 3);
//            redDot.setStroke(Color.RED);

        // red dot style
        redDot.setRadius(Configuration.pieceRadius / 4);
        redDot.setFill(Color.RED);
        redDot.setStrokeWidth(Configuration.lineWidth);
        redDot.setStroke(Color.RED);
        paneBoardChildren.add(redDot);
      } else {
        redDot.relocate(calcPieceCoordinate(pi.getX()) - redDot.getRadius(), calcPieceCoordinate(pi.getY()) - redDot.getRadius());
        redDot.toFront();
      }
    }
  }

  void clearAll() {
    if (paneBoardChildren != null) {
      paneBoardChildren.clear();
    }

    lineListX.clear();
    lineListY.clear();
    dotList.clear();
    pieceList.clear();
    redDot = null;
    winAnimation = null;
  }

  void clearPieces() {
    if (!pieceList.isEmpty()) {
      paneBoardChildren.removeAll(pieceList);
    }

    if (redDot != null) {
      paneBoardChildren.remove(redDot);
    }

    if (winAnimation != null) {
      paneBoardChildren.remove(winAnimation);
    }

    pieceList.clear();
    redDot = null;
    winAnimation = null;
  }

  void clearAndRedrawBoard() {
    // clearAll boardUI first
    // use getInstance() to update boardUI adapting order in Configuration
    boardUI.clearAll();
    board.clearPieces();

    order = Gomoku.order;

    // draw lines
    for (int i = 0; i < order; i++) {
      Line lineX = new Line(Gomoku.border, calcPieceCoordinate(i), Gomoku.paneWidth - Gomoku.border, calcPieceCoordinate(i));
      Line lineY = new Line(calcPieceCoordinate(i), Gomoku.border, calcPieceCoordinate(i), Gomoku.paneBoardHeight - Gomoku.border);
      lineX.setStrokeWidth(Configuration.lineWidth);
      lineY.setStrokeWidth(Configuration.lineWidth);
      paneBoardChildren.add(lineX);
      paneBoardChildren.add(lineY);

      lineListX.add(lineX);
      lineListY.add(lineY);
    }

    // draw five dots
    drawDot(new Piece(3, 3, 0));
    drawDot(new Piece(3, order - 4, 0));
    drawDot(new Piece(order - 4, 3, 0));
    drawDot(new Piece(order - 4, order - 4, 0));
    drawDot(new Piece((order - 1) / 2, (order - 1) / 2, 0));
  }

  void removeLastPiece() {
    if (winAnimation != null) {
      paneBoardChildren.remove(winAnimation);
    }
    winAnimation = null;

    int start = paneBoardChildren.size();
    paneBoardChildren.remove(pieceList.get(pieceList.size() - 1));
    if (paneBoardChildren.size() - start != -1) {
      System.out.println("Failed to remove element");
      System.out.println(paneBoardChildren.size() - start);
    }

    start = pieceList.size();
    pieceList.remove(pieceList.size() - 1);
    if (pieceList.size() - start != -1) {
      System.out.println("Failed to remove pieceList element");
      System.out.println(pieceList.size() - start);
    }
  }

}
