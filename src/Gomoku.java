import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Random;

import static com.sun.javafx.font.LogicalFont.STYLE_BOLD;

public class Gomoku extends Application{
    private FlowPane root = null;

    private Pane paneBoard = null;
    private Pane paneButton = null;

    private Button btnStart = null;
    private Button btnMode = null;
    private Slider sldSize = null;
    private Label lblTxt = null;

    private int paneWidth = 0;
    private int paneBoardHeight = 0;
    private int paneButtonHeight = 0;

    private AI ai1 = null;
    private AI ai2 = null;

    // 1: white   -1:black
    private int color = 1;


    private void createPane() {
        this.root = new FlowPane();
        paneBoard = new Pane();
        paneButton = new Pane();

        // paneWidth: |<-   border  ->|<-   ((order - 1) * increment)   ->|<-   border   ->|   (Y is the same)
        paneWidth = Constants.getBorder() * 2 + (Constants.getOrder() - 1) * Constants.increment;
        paneBoardHeight = Constants.getBorder() * 2 + (Constants.getOrder() - 1) * Constants.increment;
        paneButtonHeight = Constants.btnPaneHeight;

        root.setPrefSize(paneWidth, paneBoardHeight + paneButtonHeight);
        paneBoard.setPrefSize(paneWidth, paneBoardHeight);
        paneButton.setPrefSize(paneWidth, paneButtonHeight);

        paneBoard.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (!Constants.gameStarted) {
                    return;
                }
                else if (Constants.getMode() == Constants.Mode.AIvAI) {
                    return;
                }
                else if (Constants.getMode() == Constants.Mode.PvAI) {
                    if (checkMouseClick(me.getX(), me.getY())) {
                        int seqX = calcPieceSeq(me.getX());
                        int seqY = calcPieceSeq(me.getY());
                        PieceInfo tempPi = new PieceInfo(seqX, seqY, color);
                        if (Pieces.getInstance().checkAndSet(tempPi)) {
                            drawPiece(tempPi);

                            int checkResult = Referee.checkWinningCondition(tempPi);
                            if (checkResult != 0) {
                                finishGame(checkResult);
                            }
                            else {
                                PieceInfo aiMove = ai1.nextMove();
                                drawPiece(aiMove);
                                checkResult = Referee.checkWinningCondition(aiMove);
                                if (checkResult != 0) {
                                    finishGame(checkResult);
                                }
                            }
                        }
                    }
                    else {
                        // do nothing
                    }
                }
                else { // Constants.Mode.PvP
                    if (checkMouseClick(me.getX(), me.getY())) {
                        int seqX = calcPieceSeq(me.getX());
                        int seqY = calcPieceSeq(me.getY());
                        PieceInfo tempPi = new PieceInfo(seqX, seqY, color);
                        if (Pieces.getInstance().checkAndSet(tempPi)) {
                            drawPiece(tempPi);

                            int checkResult = Referee.checkWinningCondition(tempPi);
                            if (checkResult != 0) {
                                finishGame(checkResult);
                            }
                            else {
                                switchColor();
                            }
                        }
                    }
                    else {
                        // do nothing
                    }
                }
            }
        });

        root.getChildren().add(paneBoard);
        root.getChildren().add(paneButton);
    }

    private void clearAndDrawBoard() {
        Rectangle rectClear = new Rectangle(paneWidth, paneBoardHeight);
        rectClear.setFill(Color.WHITE);
        paneBoard.getChildren().add(rectClear);

        for (int i = 0; i < Constants.getOrder(); i++) {
            Line lineX = new Line(Constants.getBorder(), Constants.getBorder() + i * Constants.increment, paneWidth - Constants.getBorder(), Constants.getBorder() + i * Constants.increment);
            Line lineY = new Line(Constants.getBorder() + i * Constants.increment, Constants.getBorder(), Constants.getBorder() + i * Constants.increment, paneBoardHeight - Constants.getBorder());
            paneBoard.getChildren().add(lineX);
            paneBoard.getChildren().add(lineY);
        }
    }

    private void swapAI(AI ai1, AI ai2) {
        AI temp = ai1;
        ai1 = ai2;
        ai2 = temp;
    }

    private void btnStartFunc() {
        Pieces.getInstance().clearPieces();
        clearAndDrawBoard();
        Constants.gameStarted = true;
        sldSize.setDisable(true);
        btnMode.setDisable(true);
        lblTxt.setText("White Move");
        btnStart.setText("End");

        switch (Constants.getMode()) {
            case PvAI: {
                Random ran = new Random();
                if (ran.nextInt(2) % 2 == 0) {
                    ai1 = new AI(1);

                    // When AI first(white), switch Human's color and let AI make one move first
                    switchColor();
                    PieceInfo aiMove = ai1.nextMove();
                    drawPiece(aiMove);
                    int checkResult = Referee.checkWinningCondition(aiMove);
                    if (checkResult != 0) {
                        finishGame(checkResult);
                    }
                }
                else {
                    ai1 = new AI(-1);
                }
                break;
            }
            case PvP: {
                break;
            }
            case AIvAI: {
                // TODO under construction
//                ai1 = new AI(1);
//                ai2 = new AI(0);
//
//                while (true) {
//                    PieceInfo tempPi;
//                    do {
//                        tempPi = ai1.nextMove();
//                    } while (!Pieces.getInstance().checkAndSet(tempPi));
//
//                    System.out.println(tempPi.getX() + " " + tempPi.getY() +  " " + tempPi.getColor());
//
//
//                    drawPiece(tempPi);
//
//
//                    System.out.println("here");
//
//                    int checkResult = Referee.checkWinningCondition(tempPi);
//                    if (checkResult != 0) {
//                        finishGame(checkResult);
//                    }
//
//                    try {
//                        Thread.sleep(1000);
//                    }
//                    catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    swapAI(ai1, ai2);
//                }
            }
            default: {
                break;
            }
        }
    }

    private void btnEndFunc(boolean clearPieces) {
        if (clearPieces) {
            Pieces.getInstance().clearPieces();
            clearAndDrawBoard();
            lblTxt.setText("");
        }

        this.color = 1;
        Constants.gameStarted = false;
        sldSize.setDisable(false);
        btnMode.setDisable(false);
        btnStart.setText("Start");
    }

    private void addControlButton() {
        btnStart = new Button("Start");
        btnStart.setPrefSize(Constants.btnPaneHeight * 2, Constants.btnPaneHeight * 2 / 3);
        btnStart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (Constants.gameStarted) {
                    btnEndFunc(true);
                }
                else {
                    btnStartFunc();
                }
            }
        });

        btnMode = new Button(Constants.getMode().toString());
        btnMode.setPrefSize(Constants.btnPaneHeight * 2, Constants.btnPaneHeight * 2 / 3);
        btnMode.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                switch(Constants.getMode()) {
                    case PvAI: {
                        Constants.setMode(Constants.Mode.PvP);
                        btnMode.setText("PvP");
                        break;
                    }
                    case PvP: {
                        Constants.setMode(Constants.Mode.AIvAI);
                        btnMode.setText("AIvAI");
                        break;
                    }
                    case AIvAI: {
                        Constants.setMode(Constants.Mode.PvAI);
                        btnMode.setText("PvAI");
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        });

        final Label lblSize = new Label("Size:");

        sldSize = new Slider(11, 19, 19);
        sldSize.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                if (new_val.intValue() != Constants.getOrder()) {
                    Constants.setOrder(new_val.intValue());
                    clearAndDrawBoard();
                    lblTxt.setText("Size: " + new_val.intValue());
                }
            }
        });

        lblTxt = new Label("Gomoku " + Constants.version + ". Hope you enjoy!\n(Developed by JacobChengZhang)");

        HBox hBox = new HBox();
        hBox.setPrefSize(paneWidth, paneButtonHeight);
        hBox.setPadding(new Insets(0, 30, 0, 30));
        hBox.setSpacing(20);
        hBox.getChildren().addAll(btnStart, btnMode, lblSize, sldSize, lblTxt);
        hBox.setAlignment(Pos.CENTER_LEFT);

        paneButton.getChildren().add(hBox);
    }

    private void drawPiece(PieceInfo pi) {
        Circle p = new Circle();
        p.setCenterX(Constants.getBorder() + pi.getX() * Constants.increment);
        p.setCenterY(Constants.getBorder() + pi.getY() * Constants.increment);
        p.setRadius(Constants.pieceRadius);
        if (pi.getColor() == 1) {
            p.setFill(Color.WHITE);
        }
        else {
            p.setFill(Color.BLACK);
        }
        p.setStroke(Color.BLACK);

        paneBoard.getChildren().add(p);
    }

    private Parent startGame() {
        createPane();

        clearAndDrawBoard();

        addControlButton();

        return root;
    }

    private void finishGame(int result) {
        playWinningAnimation(result);

        switch (result) {
            case 1: {
                lblTxt.setText("White wins!");
                break;
            }
            case -1: {
                lblTxt.setText("Black wins!");
                break;
            }
            case -100: {
                lblTxt.setText("Oops, ended in a draw!");
                break;
            }
            default: {
                lblTxt.setText("Caught a bug in Referee.");
                System.out.println("Caught a bug in Referee.");
                System.exit(1);
                break;
            }
        }

        btnEndFunc(false);
    }

    private void playWinningAnimation(int result) {
        //TODO turn static Text into really animation...
        if (result == -100) { // draw
            Text txt = new Text(paneWidth / 3, paneBoardHeight / 2, "Draw!");
            txt.setFill(Color.RED);
            txt.setFont(new Font("Courier", 100));
            txt.setTextAlignment(TextAlignment.CENTER);
            paneBoard.getChildren().add(txt);
        }
        else {
            PieceInfo pi1 = Pieces.getInstance().getWinningPieceInfo(1);
            PieceInfo pi2 = Pieces.getInstance().getWinningPieceInfo(2);
            if (pi1 != null && pi2 != null) {
                Line winningLine = new Line(calcPieceCoordinate(pi1.getX()), calcPieceCoordinate(pi1.getY()), calcPieceCoordinate(pi2.getX()), calcPieceCoordinate(pi2.getY()));
                winningLine.setStroke(Color.RED);
                winningLine.setStrokeWidth(5);
                paneBoard.getChildren().add(winningLine);
            }
            else { // due to unknown bugs, failed to fetch winning PieceInfo
                System.out.println("Caught a bug and failed to fetch winning PieceInfo");
                System.exit(1);
            }
        }
    }

    // A valid click should both satisfy (x, y coordinate close to gridPoint) and (the gridPoint has no piece on it)
    public static boolean checkMouseClick(double meX, double meY) {
        boolean validX = false;
        boolean validY = false;
        int x = (int)(meX + 0.5);
        int y = (int)(meY + 0.5);

        if ((x - Constants.getBorder()) % Constants.increment < Constants.increment / 3 || (x - Constants.getBorder()) % Constants.increment > Constants.increment * 2 / 3) {
            validX = true;
        }
        if ((y - Constants.getBorder()) % Constants.increment < Constants.increment / 3 || (y - Constants.getBorder()) % Constants.increment > Constants.increment * 2 / 3) {
            validY = true;
        }

        return validX && validY;
    }

    // x or y coordinate -> sequence number in Pieces.p[][]
    public static int calcPieceSeq(double meC) {
        int c = (int)(meC + 0.5);
        if ((c - Constants.getBorder()) % Constants.increment < Constants.increment / 3) {
            return (c - Constants.getBorder()) / Constants.increment;
        }
        else {
            return ((c - Constants.getBorder()) / Constants.increment) + 1;
        }
    }

    public static double calcPieceCoordinate(int seq) {
        return (double)(seq * Constants.increment + Constants.getBorder());
    }

    // also means switch player
    private void switchColor() {
        this.color = -this.color;

        if (this.color == 1) {
            lblTxt.setText("White Move");
        }
        else {
            lblTxt.setText("Black Move");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gomoku");
        //primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(startGame()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}