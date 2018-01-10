package Gomoku;

import AI.AI_Herald;
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

public class Gomoku extends Application{
    private FlowPane root = null;

    private Pane paneBoard = null;
    private Pane paneButton = null;

    private Button btnStart = null;
    private Button btnMode = null;
    private Slider sldSize = null;
    private Label lblSize = null;
    private Label lblTxt = null;

    private int paneWidth = 0;
    private int paneBoardHeight = 0;
    private int paneButtonHeight = 0;

    // In PvAI mode, human will always play with ai1
    private AiMove ai1 = null;
    private AiMove ai2 = null;

    // -1:black    1: white
    private int color = -1;

    // max number of failed attempts that AI can make
    private static final int maxAttempts = 10;


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

    private void createPane() {
        this.root = new FlowPane();
        paneBoard = new Pane();
        paneButton = new Pane();

        // paneWidth: |<-   minBorder  ->|<-   ((maxOrder - 1) * increment)   ->|<-   minBorder   ->|   (Y is the same)
        paneWidth = Constants.minBorder * 2 + (Constants.maxOrder - 1) * Constants.increment;
        paneBoardHeight = Constants.minBorder * 2 + (Constants.maxOrder - 1) * Constants.increment;
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
                    // TODO use timer instead of mouse click to trigger AI_Herald move
                    //return;
                    if (ai1.getColor() == color) {
                        letAiMove(ai1);
                    }
                    else {
                        letAiMove(ai2);
                    }

                    if (Constants.gameStarted) {
                        switchColor();
                    }
                }
                else if (Constants.getMode() == Constants.Mode.PvAI) {
                    letHumanMove(true, me);
                }
                else { // Constants.Mode.PvP
                    letHumanMove(false, me);
                }
            }
        });

        root.getChildren().add(paneBoard);
        root.getChildren().add(paneButton);
    }

    private void clearAndDrawBoard() {
        // clear board first
        Rectangle rectClear = new Rectangle(paneWidth, paneBoardHeight);
        rectClear.setFill(Color.WHITE);
        paneBoard.getChildren().add(rectClear);

        // draw lines
        for (int i = 0; i < Constants.getOrder(); i++) {
            Line lineX = new Line(Constants.getBorder(), calcPieceCoordinate(i), paneWidth - Constants.getBorder(), calcPieceCoordinate(i));
            Line lineY = new Line(calcPieceCoordinate(i), Constants.getBorder(), calcPieceCoordinate(i), paneBoardHeight - Constants.getBorder());
            lineX.setStrokeWidth(Constants.lineWidth);
            lineY.setStrokeWidth(Constants.lineWidth);
            paneBoard.getChildren().add(lineX);
            paneBoard.getChildren().add(lineY);
        }

        // draw five dots
        drawDot(new PieceInfo(3, 3, 0));
        drawDot(new PieceInfo(3, Constants.getOrder() - 4, 0));
        drawDot(new PieceInfo(Constants.getOrder() - 4, 3, 0));
        drawDot(new PieceInfo(Constants.getOrder() - 4, Constants.getOrder() - 4, 0));
        drawDot(new PieceInfo((Constants.getOrder() - 1) / 2, (Constants.getOrder() - 1) / 2, 0));

        // TODO draw number 1 ~ 15 and characters A ~ O (not necessary)
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

        lblSize = new Label("Size: " + Constants.getOrder());

        sldSize = new Slider(Constants.minOrder, Constants.maxOrder, Constants.getOrder());
        sldSize.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                if ((new_val.intValue() != Constants.getOrder()) && (new_val.intValue() % 2 != 0)) {
                    Constants.setOrder(new_val.intValue());
                    clearAndDrawBoard();
                    lblSize.setText("Size: " + new_val.intValue());
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

    private void drawDot(PieceInfo pi) {
        Circle dot = new Circle();
        dot.setCenterX(calcPieceCoordinate(pi.getX()));
        dot.setCenterY(calcPieceCoordinate(pi.getY()));
        dot.setRadius(Constants.dotRadius);
        dot.setFill(Color.BLACK);
        dot.setStroke(Color.BLACK);

        paneBoard.getChildren().add(dot);
    }

    private void drawPiece(PieceInfo pi) {
        Circle p = new Circle();
        p.setCenterX(calcPieceCoordinate(pi.getX()));
        p.setCenterY(calcPieceCoordinate(pi.getY()));
        p.setRadius(Constants.pieceRadius);
        if (pi.getColor() == 1) {
            p.setFill(Color.WHITE);
        }
        else {
            p.setFill(Color.BLACK);
        }
        p.setStrokeWidth(Constants.lineWidth);
        p.setStroke(Color.BLACK);

        paneBoard.getChildren().add(p);
    }

    private Parent startGame() {
        createPane();

        clearAndDrawBoard();

        addControlButton();

        return root;
    }

    /**
     * As for result
     * 1    -> White wins
     * 2    -> Black give up, White wins
     * -1   -> Black wins
     * -2   -> White give up, Black wins
     * -100 -> Draw game
     *
     * @param result
     */
    private void finishGame(int result) {
        playWinningAnimation(result);

        String playerWhite = null;
        String playerBlack = null;

        switch (Constants.getMode()) {
            case PvAI: {
                if (ai1.getColor() == 1) {
                    playerWhite = ai1.toString();
                    playerBlack = "Human";
                }
                else {
                    playerWhite = "Human";
                    playerBlack = ai1.toString();
                }
                break;
            }
            case PvP: {
                playerWhite = "Human";
                playerBlack = "Human";
                break;
            }
            case AIvAI: {
                if (ai1.getColor() == 1) {
                    playerWhite = ai1.toString();
                    playerBlack = ai2.toString();
                }
                else {
                    playerWhite = ai2.toString();
                    playerBlack = ai1.toString();
                }
                break;
            }
            default: {
                break;
            }
        }

        switch (result) {
            case 1: {
                lblTxt.setText("White(" + playerWhite + ") wins!");
                break;
            }
            case 2: {
                lblTxt.setText("Black( " + playerBlack + ") give up.\nWhite(" + playerWhite + ") wins!");
                break;
            }
            case -1: {
                lblTxt.setText("Black(" + playerBlack + ") wins!");
                break;
            }
            case -2: {
                lblTxt.setText("White(" + playerWhite + ") give up.\nBlack(" + playerBlack + ") wins!");
                break;
            }
            case -100: {
                lblTxt.setText("Oops, " + playerBlack + " and " + playerWhite + "\nended in a draw!");
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
            txt.setFont(new Font("Courier", 6 * Constants.pieceRadius));
            txt.setTextAlignment(TextAlignment.CENTER);
            paneBoard.getChildren().add(txt);
        }
        else {
            PieceInfo pi1 = Pieces.getInstance().getWinningPieceInfo(1);
            PieceInfo pi2 = Pieces.getInstance().getWinningPieceInfo(2);
            if (pi1 != null && pi2 != null) {
                Line winningLine = new Line(calcPieceCoordinate(pi1.getX()), calcPieceCoordinate(pi1.getY()), calcPieceCoordinate(pi2.getX()), calcPieceCoordinate(pi2.getY()));
                winningLine.setStroke(Color.RED);
                winningLine.setStrokeWidth(Constants.pieceRadius / 3);
                paneBoard.getChildren().add(winningLine);
            }
            else {
                //System.out.println("Caught a bug and failed to fetch winning PieceInfo");
                //System.exit(1);
            }
        }
    }

    private void btnStartFunc() {
        Pieces.getInstance().clearPieces();
        clearAndDrawBoard();
        Constants.gameStarted = true;
        sldSize.setDisable(true);
        btnMode.setDisable(true);
        lblTxt.setText("Black Move");
        btnStart.setText("End");

        switch (Constants.getMode()) {
            case PvAI: {
                Random ran = new Random();
                if (ran.nextInt(2) % 2 == 0) {
                    ai1 = new AI_Herald(-1, Pieces.getInstance());

                    // When AI_Herald first(white), switch Human's color and let AI_Herald make one move first
                    switchColor();

                    letAiMove(ai1);
                }
                else {
                    ai1 = new AI_Herald(1, Pieces.getInstance());
                }
                break;
            }
            case PvP: {
                break;
            }
            case AIvAI: {
                ai1 = new AI_Herald(-1, Pieces.getInstance());
                ai2 = new AI_Herald(1, Pieces.getInstance());
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

        this.color = -1;
        Constants.gameStarted = false;
        ai1 = null;
        ai2 = null;
        sldSize.setDisable(false);
        btnMode.setDisable(false);
        btnStart.setText("Start");
    }

    private static boolean checkMouseClick(double meX, double meY) {
        // A valid click should both satisfy (x, y coordinate close to gridPoint) and (the gridPoint has no piece on it)

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

    static int calcPieceSeq(double meC) { // x or y coordinate -> sequence number in Pieces.p[][]
        int c = (int)(meC + 0.5);
        if ((c - Constants.getBorder()) % Constants.increment < Constants.increment / 3) {
            return (c - Constants.getBorder()) / Constants.increment;
        }
        else {
            return ((c - Constants.getBorder()) / Constants.increment) + 1;
        }
    }

    static double calcPieceCoordinate(int seq) {
        return (double)(seq * Constants.increment + Constants.getBorder());
    }

    private void letAiMove(AiMove ai) {
        lblTxt.setText(ai.toString() + " (" + (ai.getColor() == 1 ? "White" : "Black") + ") is moving");

        PieceInfo aiMove = null;
        boolean isMoveValid = false;
        int attempt = 0;
        while (isMoveValid == false) {
            // too many failed attempts make failure indeed
            if (attempt < maxAttempts) {
                attempt++;
            }
            else {
                finishGame(-ai.getColor() * 2);
                return;
            }

            aiMove = ai.nextMove();
            if (Pieces.getInstance().checkPieceValidity(aiMove.getX(), aiMove.getY()) && aiMove.getColor() == ai.getColor()) {
                isMoveValid = true;
                Pieces.getInstance().setPieceValue(aiMove);
                drawPiece(aiMove);
            }
        }


        lblTxt.setText((ai.getColor() == 1 ? "Black" : "White") + " Move");
        int checkResult = Referee.checkWinningCondition(aiMove);
        if (checkResult != 0) {
            finishGame(checkResult);
        }
    }

    private void letHumanMove(boolean nextIsAi, MouseEvent me) {
        if (checkMouseClick(me.getX(), me.getY())) {
            int seqX = calcPieceSeq(me.getX());
            int seqY = calcPieceSeq(me.getY());
            PieceInfo tempPi = new PieceInfo(seqX, seqY, color);
            if (Pieces.getInstance().setPieceValue(tempPi)) {
                drawPiece(tempPi);

                int checkResult = Referee.checkWinningCondition(tempPi);
                if (checkResult != 0) {
                    finishGame(checkResult);
                }
                else {
                    if (nextIsAi) {
                        letAiMove(ai1);
                    }
                    else{
                        switchColor();
                    }
                }
            }
        }
        else {
            // do nothing
        }
    }

    private void switchColor() {
        // swap human player
        this.color = -this.color;

        if (this.color == 1) {
            lblTxt.setText("White Move");
        }
        else {
            lblTxt.setText("Black Move");
        }
    }
}