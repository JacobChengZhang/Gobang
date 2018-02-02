package Gomoku;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

/**
 * control the board elements with Gomoku class collaboratively
 */
class Board {
    List<Line> lineListX = new ArrayList<>();
    List<Line> lineListY = new ArrayList<>();
    List<Circle> dotList = new ArrayList<>();

    List<Circle> pieceList = new ArrayList<>();
    Circle redDot = null;
    Object winAnimation = null;


    static ObservableList<Node> paneBoardChildren = null;

    private static int order;
    private static Board board = null;

    private Board() {
        order = Constants.getOrder();
        clear();
    }

    static Board getInstance() {
        if (board == null || order != Constants.getOrder()) {
            board = new Board();
        }
        return board;
    }

    void clear() {
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
