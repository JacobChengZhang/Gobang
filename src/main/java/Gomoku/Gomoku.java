package Gomoku;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Gomoku.Configuration.*;
import static Gomoku.Configuration.Mode.*;
import Gomoku.Referee.*;
import static Gomoku.Referee.GameState.*;


public class Gomoku extends Application {

  static Mode mode = PvAI;
  static boolean gameStarted = false;
  static int order = 15; // better between 11~19
  static int border = 101; // border when order = 15

  // UI elements
  private FlowPane root = null;
  private Pane paneBoard = null;
  private Pane paneButton = null;
  private ObservableList<Node> paneBoardChildren = null;

  private BoardUI boardUI = null;

  private Button btnStart = null;
  private Button btnMode = null;
  private Slider sldSize = null;
  private Label lblSize = null;
  private Button btnSave = null;
  private Button btnLoad = null;
  private Button btnRetract = null;
  private Label lblTxt = null;

  static int paneWidth = 0;
  static int paneBoardHeight = 0;
  private static int paneButtonWidth = 0;

  private Board board = null;

  // In PvAI mode, human will always play with ai1
  private AiMove ai1 = null;
  private AiMove ai2 = null;
  private int ai1Color = 0;
  private int ai2Color = 0;

  // used for asynchronous tasks like AI training
  private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Configuration.threadPoolLimit);

  // thread for AI trigger or Replay loading
  private Thread thread = null;
  private boolean endThread = false;

  // used for manually load replay
  private boolean clicked = false;

  // -1:black    1: white   (Black first)
  private int color = -1;

  // names of players
  private String playerWhite = null;
  private String playerBlack = null;


  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Gomoku");
    primaryStage.setResizable(false);
    primaryStage.setScene(new Scene(startGame()));
    primaryStage.show();
  }

  private Parent startGame() {
    createPane();
    boardUI.clearAndRedrawBoard();
    addControlButton();
    return root;
  }

  private void createPane() {
    this.root = new FlowPane(Orientation.HORIZONTAL);
    paneBoard = new Pane();
    paneButton = new Pane();

    // paneWidth: |<-   minBorder  ->|<-   ((maxOrder - 1) * increment)   ->|<-   minBorder   ->|   (Y is the same)
    paneWidth = Configuration.minBorder * 2 + (Configuration.maxOrder - 1) * Configuration.increment;
    paneBoardHeight = Configuration.minBorder * 2 + (Configuration.maxOrder - 1) * Configuration.increment;
    paneButtonWidth = Configuration.btnPaneWidth;

    root.setPrefSize(paneWidth + paneButtonWidth, paneBoardHeight);
    paneBoard.setPrefSize(paneWidth, paneBoardHeight);
    paneButton.setPrefSize(paneButtonWidth, paneBoardHeight);

    paneBoard.setOnMouseClicked(me -> {
      clicked = true;

      if (!gameStarted) {
        return;
      } else if (mode == Configuration.Mode.AIvAI) {
        return;
      } else if (mode == PvAI) {
        if (thread == null || thread.getState() == Thread.State.TERMINATED)
          letHumanMove(true, me);
      } else { // Configuration.Mode.PvP
        letHumanMove(false, me);
      }
    });

    board = new Board();
    paneBoardChildren = paneBoard.getChildren();
    boardUI = BoardUI.getInstance(board, paneBoardChildren);

    root.getChildren().add(paneBoard);
    root.getChildren().add(paneButton);
  }

  private void addControlButton() {
    btnStart = new Button("Start");
    btnStart.setPrefSize(Configuration.btnPaneWidth / 2, Configuration.btnPaneWidth / 4);
    btnStart.setOnMouseClicked(event -> {
      if (gameStarted) {
        btnEndFunc(true);
      } else {
        btnStartFunc();
      }
    });

    Separator sp1 = new Separator(Orientation.HORIZONTAL);

    btnMode = new Button(mode.toString());
    btnMode.setPrefSize(Configuration.btnPaneWidth / 2, Configuration.btnPaneWidth / 4);
    btnMode.setOnMouseClicked(event ->
            btnModeFunc());

    lblSize = new Label("Size: " + order);
    lblSize.setWrapText(true);

    sldSize = new Slider(Configuration.minOrder, Configuration.maxOrder, order);
    sldSize.valueProperty().addListener((ov, old_val, new_val) -> {
      if ((new_val.intValue() != order) && (new_val.intValue() % 2 != 0)) {
        Utils.setOrder(new_val.intValue());
        lblSize.setText("Size: " + new_val.intValue());
      }
    });

    Separator sp2 = new Separator(Orientation.HORIZONTAL);

    btnSave = new Button("Save");
    btnSave.setPrefSize(Configuration.btnPaneWidth / 2, Configuration.btnPaneWidth / 4);
    btnSave.setDisable(true);
    btnSave.setOnMouseClicked(event ->
            btnSaveFunc());

    btnLoad = new Button("Load");
    btnLoad.setPrefSize(Configuration.btnPaneWidth / 2, Configuration.btnPaneWidth / 4);
    btnLoad.setOnMouseClicked(event -> {
      if (thread == null || thread.getState() != Thread.State.TIMED_WAITING) {
        btnLoadFunc();
      } else {
        btnEndFunc(true);
        btnLoad.setText("Load");
      }
    });

    Separator sp3 = new Separator(Orientation.HORIZONTAL);

    btnRetract = new Button("Retract");
    btnRetract.setPrefSize(Configuration.btnPaneWidth / 2, Configuration.btnPaneWidth / 4);
    btnRetract.setDisable(true);
    btnRetract.setOnMouseClicked(event ->
            btnRetractFunc());

    Separator sp4 = new Separator(Orientation.HORIZONTAL);

    lblTxt = new Label("Gomoku " + Configuration.version + ". \nHope you enjoy!\n(Developed by JacobChengZhang)");
    lblTxt.setWrapText(true);

    VBox vBox = new VBox();
    vBox.setPrefSize(paneButtonWidth, paneBoardHeight);
    vBox.setPadding(new Insets(20, 15, 20, 15));
    vBox.setSpacing(20);
    vBox.getChildren().addAll(btnStart, sp1, btnMode, sldSize, lblSize, sp2, btnSave, btnLoad, sp3, btnRetract, sp4, lblTxt);
    vBox.setAlignment(Pos.TOP_CENTER);

    paneButton.getChildren().add(vBox);
  }

  private void terminateThread() {
    while (thread != null && thread.getState() != Thread.State.TERMINATED) {
      endThread = true;
      thread.interrupt();
    }
  }

  private void finishGame(GameState ending) {
    terminateThread();

    switch (mode) {
      case PvAI: {
        fixedThreadPool.execute(() ->
                ai1.gameEnds(ending));
        break;
      }
      case AIvAI: {
        if (Configuration.aiTrainingMode) {

        } else {
          fixedThreadPool.execute(() ->
                  ai1.gameEnds(ending));
          fixedThreadPool.execute(() ->
                  ai2.gameEnds(ending));
        }
        break;
      }
      default: {
        break;
      }
    }

    boardUI.playWinningAnimation(ending);

    switch (ending) {
      case WHITE_WIN: {
        lblTxt.setText("White(" + playerWhite + ") wins!");
        break;
      }
      case BLACK_GIVE_UP: {
        lblTxt.setText("Black( " + playerBlack + ") give up.\nWhite(" + playerWhite + ") wins!");
        break;
      }
      case BLACK_WIN: {
        lblTxt.setText("Black(" + playerBlack + ") wins!");
        break;
      }
      case WHITE_GIVE_UP: {
        lblTxt.setText("White(" + playerWhite + ") give up.\nBlack(" + playerBlack + ") wins!");
        break;
      }
      case DRAW: {
        lblTxt.setText("Oops, " + playerBlack + " and " + playerWhite + "\nended in a draw!");
        break;
      }
      default: {
        lblTxt.setText("Caught a bug in Referee.");
        System.err.println("Caught a bug in Referee.");
        System.exit(1);
        break;
      }
    }

    btnEndFunc(false);
  }

  // TODO refactor btnStartFunc and btnLoadFunc
  private void btnStartFunc() {
    boardUI.clearAndRedrawBoard();
    gameStarted = true;
    sldSize.setDisable(true);
    btnMode.setDisable(true);
    btnSave.setDisable(false);
    btnLoad.setDisable(true);
    lblTxt.setText("Black Move");
    btnStart.setText("End");
    color = -1;

    AiMove aiBlack = Utils.createAI(true, board);
    AiMove aiWhite = Utils.createAI(false, board);

    switch (mode) {
      case PvAI: {
        btnRetract.setDisable(false);

        if (Math.random() >= 0.5) {
          ai1 = aiBlack;
          ai1Color = -1;
          playerBlack = ai1.getName();
          playerWhite = "Human";

          // When AI_Herald first(white), switch Human's color and let AI_Herald make one move first
          switchColor();

          letAiMove(ai1, ai1Color);
        } else {
          ai1 = aiWhite;
          ai1Color = 1;
          playerBlack = "Human";
          playerWhite = ai1.getName();
        }
        break;
      }
      case PvP: {
        btnRetract.setDisable(false);
        playerBlack = "Human";
        playerWhite = "Human";
        break;
      }
      case AIvAI: {
        if (Configuration.aiTrainingMode) {
          btnSave.setDisable(true);
          lblTxt.setText("AIvAI silently in background...");

          for (int i = 0; i < Configuration.threadPoolLimit; i++) {
            Board tempBoard = new Board();
            AiMove tempAiBlack = Utils.createAI(true, board);
            AiMove tempAiWhite = Utils.createAI(false, board);

            endThread = false;
            fixedThreadPool.execute(() -> {
              int tempColor = -1;
              while (!endThread) {
                AiMove ai;
                int aiColor;
                if (tempColor == -1) {
                  ai = tempAiBlack;
                  aiColor = -1;
                } else {
                  ai = tempAiWhite;
                  aiColor = 1;
                }

                Piece aiMove = null;
                boolean isMoveValid = false;
                int attempt = 0;
                while (!isMoveValid && !endThread) {
                  // too many failed attempts make failure indeed
                  if (attempt < Configuration.maxAttempts) {
                    attempt++;
                  } else {
                    System.out.println((aiColor == -1 ? "Black" : "White") + " gives up!");
                    return;
                  }

                  try {
                    aiMove = ai.nextMove();
                  } catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                  }

                  if (tempBoard.checkPieceValidity(aiMove.getX(), aiMove.getY()) && aiMove.getColor() == aiColor) {
                    isMoveValid = true;
                    tempBoard.setPieceColor(aiMove);
                    tempBoard.pushPieceStack(aiMove);
                  }
                }

                GameState checkRusult = Referee.checkIfGameEnds(tempBoard, aiMove);
                if (checkRusult != NOT_END) {
                  fixedThreadPool.execute(() ->
                          tempAiBlack.gameEnds(checkRusult));
                  fixedThreadPool.execute(() ->
                          tempAiWhite.gameEnds(checkRusult));
                  System.out.println(checkRusult);
                  return;
                }

                tempColor = -tempColor;
              }


            });
          }
        } else {
          if (Math.random() >= 0.5) {
            ai1 = aiWhite;
            ai1Color = 1;
            playerWhite = ai1.getName();

            ai2 = aiBlack;
            ai2Color = -1;
            playerBlack = ai2.getName();

          } else {
            ai1 = aiBlack;
            ai1Color = -1;
            playerBlack = ai1.getName();

            ai2 = aiWhite;
            ai2Color = 1;
            playerWhite = ai2.getName();
          }

          thread = new Thread(() -> {
            while (gameStarted && !endThread) {
              AiMove ai;
              if (color == ai1Color) {
                letAiMoveInOtherThread(ai1, ai1Color);
              } else {
                letAiMoveInOtherThread(ai2, ai2Color);
              }



              if (gameStarted && !endThread) {
                Utils.runAndWait(() ->
                        switchColor());
              }
//                        try {
//                            Thread.sleep(Configuration.aiThreadCycle);
//                        }
//                        catch (InterruptedException ie) {
//                            ie.printStackTrace();
//                            Platform.runLater(() ->
//                                    lblTxt.setText("Something went wrong with AI thread."));
//                        }
            }
          });
          endThread = false;
          thread.start();
        }
      }
      default: {
        break;
      }
    }
  }

  private void btnEndFunc(boolean clearPieces) {
    terminateThread();

    if (clearPieces) {
      boardUI.clearPieces();
      lblTxt.setText("");
      btnSave.setDisable(true);
      btnRetract.setDisable(true);
      ai1 = null;
      ai2 = null;
      ai1Color = 0;
      ai2Color = 0;
    }

    this.color = -1;
    gameStarted = false;
    sldSize.setDisable(false);
    btnMode.setDisable(false);
    btnLoad.setDisable(false);
    btnStart.setText("Start");
  }

  private void btnModeFunc() {
    switch (mode) {
      case PvAI: {
        Utils.setMode(Configuration.Mode.PvP);
        btnMode.setText("PvP");
        break;
      }
      case PvP: {
        Utils.setMode(Configuration.Mode.AIvAI);
        btnMode.setText("AIvAI");
        break;
      }
      case AIvAI: {
        Utils.setMode(PvAI);
        btnMode.setText("PvAI");
        break;
      }
      default: {
        break;
      }
    }
  }

  private void btnSaveFunc() { //TODO use serialization
    StringBuilder sb = new StringBuilder();
    if (!gameStarted) {
      Piece tempPi = board.getWinningPiece(1);
      if (tempPi != null) {
        if (tempPi.getColor() == -1) {
          sb.insert(0, "// Black wins\n\n");
        } else {
          sb.insert(0, "// White wins\n\n");
        }
      }
    }

    sb.append(order).append("\n").append("(Black) ").append(playerBlack).append("\n").append("(White) ").append(playerWhite).append("\n");

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = df.format(new Date());
    sb.insert(0, "// " + date + "\n");

    board.getReplayData(sb);

    try {
      File folder = new File("./replay/");
      folder.mkdirs();

      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("./replay/" + date.replaceAll(":", "_") + ".txt"), "utf-8");
      writer.write(sb.toString());
      writer.close();
      lblTxt.setText("Replay saved!\nNamed with time.");
    } catch (Exception ex) {
      lblTxt.setText("Unknown error. Failed to save.");
      ex.printStackTrace();
    }
  }

  /**
   * Comments that start with '//' and Blank line in replay files are supported which should not change the order of raw content.
   */
  private void btnLoadFunc() {
    //TODO may add a feature "load and play" and if so, must execute pushPieceStack

    FileChooser fc = new FileChooser();
    //fc.setInitialDirectory(new File(System.getProperty("user.dir")));
    fc.setTitle("Load Replay");
    fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
    File selectedFile = fc.showOpenDialog(null);
    if (selectedFile != null) {
      //Configuration.gameStarted = true;
      board.clearPieces();
      btnStart.setDisable(true);
      btnMode.setDisable(true);
      sldSize.setDisable(true);
      btnLoad.setText("Stop");
      btnRetract.setDisable(true);

      thread = new Thread(() -> {
        BufferedReader reader = null;
        try {
          reader = new BufferedReader(new FileReader(selectedFile));
          String tempStr = null;
          int line = 1;
          for (; (tempStr = reader.readLine()) != null && !endThread; ) {
            if (tempStr.startsWith("//") || tempStr.equals("")) {
              continue;
            }

            switch (line) {
              case 1: {
                final int replayOrder = Integer.parseInt(tempStr);
                Utils.setOrder(replayOrder);

                Platform.runLater(() -> {
                  boardUI.clearAndRedrawBoard();
                  sldSize.setValue(replayOrder);
                  lblSize.setText("Size: " + replayOrder);
                  if (Configuration.isManualLoad) {
                    lblTxt.setText("Click to replay step by step.");
                  }
                });
                break;
              }
              case 2: {
                final String txt1 = tempStr;
                Platform.runLater(() ->
                        lblTxt.setText(txt1));
                break;
              }
              case 3: {
                final String txt2 = tempStr;
                Platform.runLater(() ->
                        lblTxt.setText(lblTxt.getText() + "\n\n" + txt2));
                break;
              }
              default: {
                String[] arr = tempStr.split(" ");

                final int tempColor;
                if (line % 2 == 0) {
                  tempColor = -1;
                } else {
                  tempColor = 1;
                }

                final Piece tempPi = new Piece(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), tempColor);
                if (board.setPieceColor(tempPi)) {

                  Platform.runLater(() -> {
                    boardUI.drawPiece(tempPi, true);
                    GameState checkResult = Referee.checkIfGameEnds(board, tempPi);
                    if (checkResult != NOT_END) {
                      boardUI.playWinningAnimation(checkResult);
                    }
                  });
                } else {
                  Platform.runLater(() ->
                          lblTxt.setText("Replay has been damaged."));
                  endThread = true;
                }
                break;
              }
            }

            line++;

            if (Configuration.isManualLoad) {
              while (!clicked) {
                Thread.sleep(100);
              }
              clicked = false;
            } else {
              Thread.sleep(Configuration.loadThreadCycle);
            }
          }
          reader.close();
        } catch (Exception ex1) {
          ex1.printStackTrace();
          Platform.runLater(() ->
                  lblTxt.setText("Something goes wrong with the file. \nFailed to load replay."));
        } finally {
          if (reader != null) {
            try {
              reader.close();
            } catch (IOException ex2) {
              ex2.printStackTrace();
            }
          }
        }

        Platform.runLater(() ->
                lblTxt.setText(lblTxt.getText() + "\n\nReplay finished."));
        btnStart.setDisable(false);
        btnMode.setDisable(false);
        sldSize.setDisable(false);
        Platform.runLater(() ->
                btnLoad.setText("Load"));
      });
      endThread = false;
      thread.start();

    } else {
      lblTxt.setText("Something goes wrong with the file. \nFailed to load replay.");
    }
  }

  private void btnRetractFunc() {
    if (thread != null && thread.getState() != Thread.State.TERMINATED) {
      lblTxt.setText("No response.\nTry retract later.");
      return;
    }

    Piece redrawPi;
    try {
      redrawPi = board.retract();
    } catch (Exception ex) {
      lblTxt.setText(ex.getMessage());
      return;
    }

    if (redrawPi != null) {
      switch (mode) {
        case PvAI: {
          color = -redrawPi.getColor();
          if (color == -1) {
            lblTxt.setText("Black Move");
          } else {
            lblTxt.setText("White Move");
          }
          break;
        }
        case PvP: {
          color = -redrawPi.getColor();
          if (color == -1) {
            lblTxt.setText("Black Move");
          } else {
            lblTxt.setText("White Move");
          }
          break;
        }
        default: {
          lblTxt.setText("Caught a bug in btnRetractFunc.");
          break;
        }
      }

      boardUI.redDot.relocate(Utils.calcPieceCoordinate(redrawPi.getX()) - boardUI.redDot.getRadius(), Utils.calcPieceCoordinate(redrawPi.getY()) - boardUI.redDot.getRadius());
      boardUI.redDot.toFront();

      gameStarted = true;
      sldSize.setDisable(true);
      btnMode.setDisable(true);
      btnLoad.setDisable(true);
      btnStart.setText("End");
    } else {
      switch (mode) {
        case PvAI: {
          color = -1;
          break;
        }
        case PvP: {
          color = -1;
          lblTxt.setText("Black Move");
          break;
        }
        default: {
          lblTxt.setText("Caught a bug in btnRetractFunc.");
          break;
        }
      }

      boardUI.clearPieces();
      gameStarted = true;
      sldSize.setDisable(true);
      btnMode.setDisable(true);
      btnLoad.setDisable(true);
      btnStart.setText("End");
    }
  }

  private void letAiMove(AiMove ai, int aiColor) {
    lblTxt.setText(ai.getName() + " (" + (aiColor == 1 ? "White" : "Black") + ") is moving");

    Piece aiMove = null;
    boolean isMoveValid = false;
    int attempt = 0;
    while (!isMoveValid) {
      // too many failed attempts make failure indeed
      if (attempt < Configuration.maxAttempts) {
        attempt++;
      } else {
        if (aiColor == 1) {
          finishGame(WHITE_GIVE_UP);
        } else {
          finishGame(BLACK_GIVE_UP);
        }

        return;
      }

      try {
        aiMove = ai.nextMove();
      } catch (Exception ex) {
        ex.printStackTrace();
        continue;
      }

      if (board.checkPieceValidity(aiMove.getX(), aiMove.getY()) && aiMove.getColor() == aiColor) {
        isMoveValid = true;
        board.setPieceColor(aiMove);
        board.pushPieceStack(aiMove);
        boardUI.drawPiece(aiMove, true);
      }
    }

    lblTxt.setText((aiColor == 1 ? "Black" : "White") + " Move");
    GameState checkResult = Referee.checkIfGameEnds(board, aiMove);
    if (checkResult != NOT_END) {
      finishGame(checkResult);
    }
  }

  //TODO try to combine the two function (in current thread & in other thread)
  private void letAiMoveInOtherThread(AiMove ai, int aiColor) {
    Utils.runAndWait(() ->
            lblTxt.setText(ai.getName() + " (" + (aiColor == 1 ? "White" : "Black") + ") is moving"));

    Piece aiMove = null;
    boolean isMoveValid = false;
    int attempt = 0;
    while (!isMoveValid && !endThread) {
      // too many failed attempts make failure indeed
      if (attempt < Configuration.maxAttempts) {
        attempt++;
      } else {
        final GameState ending;
        if (aiColor == 1) {
          ending = WHITE_GIVE_UP;
        } else {
          ending = BLACK_GIVE_UP;
        }

        Utils.runAndWait(() ->
                finishGame(ending));
        return;
      }

      try {
        aiMove = ai.nextMove();
      } catch (Exception ex) {
        ex.printStackTrace();
        continue;
      }

      if (board.checkPieceValidity(aiMove.getX(), aiMove.getY()) && aiMove.getColor() == aiColor) {
        isMoveValid = true;
        board.setPieceColor(aiMove);
        board.pushPieceStack(aiMove);

        final Piece _aiMove = Piece.createPieceByAI(aiMove.getX(), aiMove.getY(), aiMove.getColor());
        if (!endThread) {
          Utils.runAndWait(() ->
                  boardUI.drawPiece(_aiMove, true));
        }
      }
    }

    Utils.runAndWait(() ->
            lblTxt.setText((aiColor == 1 ? "Black" : "White") + " Move"));

    GameState checkResult = Referee.checkIfGameEnds(board, aiMove);
    if (checkResult != NOT_END) {
      Utils.runAndWait(() ->
              finishGame(checkResult));
    }
  }

  private void letHumanMove(boolean nextIsAi, MouseEvent me) {
    if (Utils.checkMouseClick(me.getX(), me.getY())) {
      int seqX = Utils.calcPieceSeq(me.getX());
      int seqY = Utils.calcPieceSeq(me.getY());
      Piece tempPi = new Piece(seqX, seqY, color);
      if (board.setPieceColor(tempPi)) {
        board.pushPieceStack(tempPi);
        boardUI.drawPiece(tempPi, true);

        GameState checkResult = Referee.checkIfGameEnds(board, tempPi);
        if (checkResult != NOT_END) {
          finishGame(checkResult);
        } else {
          if (nextIsAi) {
            // TODO use singleThreadExecutor to improve performance
            thread = new Thread(() -> {
              if (gameStarted && !endThread) {
                AiMove ai = ai1;

                letAiMoveInOtherThread(ai, ai1Color);
              }
            });
            endThread = false;
            thread.start();
          } else {
            switchColor();
          }
        }
      }
    } else {
      // do nothing
    }
  }

  private void switchColor() {
    if (!gameStarted) {
      return;
    }

    this.color = -this.color;

    if (this.color == 1) {
      lblTxt.setText("White Move");
    } else {
      lblTxt.setText("Black Move");
    }
  }

  // TODO add a game mode without GUI for AI to train themselves

}
