package Gomoku;

import javafx.application.Platform;

import java.lang.reflect.Constructor;
import java.util.concurrent.CountDownLatch;

import Gomoku.Configuration.*;


public class Utils {

  static void runAndWait(Runnable action) {
    if (action == null)
      throw new NullPointerException("action");

    // run synchronously on JavaFX thread
    if (Platform.isFxApplicationThread()) {
      action.run();
      return;
    }

    // queue on JavaFX thread and wait for completion
    final CountDownLatch doneLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        action.run();
      } finally {
        doneLatch.countDown();
      }
    });

    try {
      doneLatch.await();
    } catch (InterruptedException e) {
      // ignore exception
    }
  }

  static int calcPieceSeq(double meC) { // x or y coordinate -> sequence number in Board.p[][]
    int c = (int) (meC + 0.5);
    if ((c - Gomoku.border) % Configuration.increment < Configuration.increment / 3) {
      return (c - Gomoku.border) / Configuration.increment;
    } else {
      return ((c - Gomoku.border) / Configuration.increment) + 1;
    }
  }

  static double calcPieceCoordinate(int seq) {
    return (double) (seq * Configuration.increment + Gomoku.border);
  }

  static boolean checkMouseClick(double meX, double meY) {
    // A valid click should both satisfy (x, y coordinate close to gridPoint) and (the gridPoint has no piece on it)

    boolean validX = false;
    boolean validY = false;
    int x = (int) (meX + 0.5);
    int y = (int) (meY + 0.5);

    if ((x - Gomoku.border) % Configuration.increment < Configuration.increment / 3 || (x - Gomoku.border) % Configuration.increment > Configuration.increment * 2 / 3) {
      validX = true;
    }
    if ((y - Gomoku.border) % Configuration.increment < Configuration.increment / 3 || (y - Gomoku.border) % Configuration.increment > Configuration.increment * 2 / 3) {
      validY = true;
    }

    return validX && validY;
  }

  static AiMove createAI(boolean black, PieceQuery board) {
    try {
      if (black) {
        Class<?> clsB = Class.forName(Configuration.aiBlack);
        Constructor<?> consB = clsB.getConstructor();
        AiMove aiBlack = (AiMove)consB.newInstance();
        aiBlack.setColor(-1);
        aiBlack.setPieceQuery(board);
        return aiBlack;
      } else {
        Class<?> clsW = Class.forName(Configuration.aiWhite);
        Constructor<?> consW = clsW.getConstructor();
        AiMove aiWhite = (AiMove)consW.newInstance();
        aiWhite.setColor(1);
        aiWhite.setPieceQuery(board);
        return aiWhite;
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
      return null;
    }
  }

  public static Mode getMode() {
    return Gomoku.mode;
  }

  static void setMode(Mode m) {
    if (!Gomoku.gameStarted) {
      Gomoku.mode = m;
    }
  }

  public static int getOrder() {
    return Gomoku.order;
  }

  static void setOrder(int i) {
    if (!Gomoku.gameStarted) {
      if (i >= Configuration.minOrder && i <= Configuration.maxOrder && i % 2 != 0) {
        // first, adjust border correspondingly
        Gomoku.border += ((Gomoku.order - i) * Configuration.increment / 2);
        // change border
        Gomoku.order = i;

        Platform.runLater(() -> {
          BoardUI.getInstance(null, null).clearAndRedrawBoard();
        });
      }
    }
  }

}
