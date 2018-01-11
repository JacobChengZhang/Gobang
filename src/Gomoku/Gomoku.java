package Gomoku;

import AI.AI_Herald;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Gomoku extends Application{
    private FlowPane root = null;

    private Pane paneBoard = null;
    private Pane paneButton = null;

    private Button btnStart = null;
    private Button btnMode = null;
    private Slider sldSize = null;
    private Label lblSize = null;
    private Button btnSave = null;
    private Button btnLoad = null;
    private Label lblTxt = null;

    private int paneWidth = 0;
    private int paneBoardHeight = 0;
    private int paneButtonWidth = 0;

    // In PvAI mode, human will always play with ai1
    private AiMove ai1 = null;
    private AiMove ai2 = null;
    private int ai1Color = 0;
    private int ai2Color = 0;

    // used for saving replay
    private StringBuilder sb = null;

    private Thread thread = null;
    private boolean endThread = false;

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
        this.root = new FlowPane(Orientation.HORIZONTAL);
        paneBoard = new Pane();
        paneButton = new Pane();

        // paneWidth: |<-   minBorder  ->|<-   ((maxOrder - 1) * increment)   ->|<-   minBorder   ->|   (Y is the same)
        paneWidth = Constants.minBorder * 2 + (Constants.maxOrder - 1) * Constants.increment;
        paneBoardHeight = Constants.minBorder * 2 + (Constants.maxOrder - 1) * Constants.increment;
        paneButtonWidth = Constants.btnPaneWidth;

        root.setPrefSize(paneWidth + paneButtonWidth, paneBoardHeight);
        paneBoard.setPrefSize(paneWidth, paneBoardHeight);
        paneButton.setPrefSize(paneButtonWidth, paneBoardHeight);

        paneBoard.setOnMouseClicked(me -> {
            if (!Constants.gameStarted) {
                return;
            }
            else if (Constants.getMode() == Constants.Mode.AIvAI) {
                return;
            }
            else if (Constants.getMode() == Constants.Mode.PvAI) {
                letHumanMove(true, me);
            }
            else { // Constants.Mode.PvP
                letHumanMove(false, me);
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
        btnStart.setPrefSize(Constants.btnPaneWidth * 2 / 3, Constants.btnPaneWidth / 4);
        btnStart.setOnMouseClicked(event -> {
            if (Constants.gameStarted) {
                btnEndFunc(true);
            }
            else {
                btnStartFunc();
            }
        });

        btnMode = new Button(Constants.getMode().toString());
        btnMode.setPrefSize(Constants.btnPaneWidth * 2 / 3, Constants.btnPaneWidth / 4);
        btnMode.setOnMouseClicked(event -> {
            btnModeFunc();
        });

        lblSize = new Label("Size: " + Constants.getOrder());
        lblSize.setWrapText(true);

        sldSize = new Slider(Constants.minOrder, Constants.maxOrder, Constants.getOrder());
        sldSize.valueProperty().addListener((ov, old_val, new_val) -> {
            if ((new_val.intValue() != Constants.getOrder()) && (new_val.intValue() % 2 != 0)) {
                Constants.setOrder(new_val.intValue());
                clearAndDrawBoard();
                lblSize.setText("Size: " + new_val.intValue());
            }
        });

        btnSave = new Button("Save");
        btnSave.setPrefSize(Constants.btnPaneWidth * 2 / 3, Constants.btnPaneWidth / 4);
        btnSave.setDisable(true);
        btnSave.setOnMouseClicked(event -> {
            btnSaveFunc();
        });

        btnLoad = new Button("Load");
        btnLoad.setPrefSize(Constants.btnPaneWidth * 2 / 3, Constants.btnPaneWidth / 4);
        btnLoad.setOnMouseClicked(event -> {
            if (thread == null || thread.getState() != Thread.State.RUNNABLE) {
                btnLoadFunc();
            }
            else {
                btnEndFunc(true);
                btnLoad.setText("Load");
            }

        });

        lblTxt = new Label("Gomoku " + Constants.version + ". \nHope you enjoy!\n(Developed by JacobChengZhang)");
        lblTxt.setWrapText(true);

        VBox vBox = new VBox();
        vBox.setPrefSize(paneButtonWidth, paneBoardHeight);
        vBox.setPadding(new Insets(20, 15, 20, 15));
        vBox.setSpacing(20);
        vBox.getChildren().addAll(btnStart, btnMode, sldSize, lblSize, btnSave, btnLoad, lblTxt);
        vBox.setAlignment(Pos.TOP_CENTER);

        paneButton.getChildren().add(vBox);
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

        Platform.runLater(() -> {
            paneBoard.getChildren().add(p);
        });
    }

    private Parent startGame() {
        createPane();

        clearAndDrawBoard();

        addControlButton();

        return root;
    }

    /**
     * @param result
     * 1    -> White wins
     * 2    -> Black give up, White wins
     * -1   -> Black wins
     * -2   -> White give up, Black wins
     * -100 -> Draw game
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
        sb = new StringBuilder();
        btnSave.setDisable(false);
        btnLoad.setDisable(true);
        lblTxt.setText("Black Move");
        btnStart.setText("End");

        switch (Constants.getMode()) {
            case PvAI: {
                Random ran = new Random();
                if (ran.nextInt(2) % 2 == 0) {
                    ai1 = new AI_Herald(-1, Pieces.getInstance());
                    ai1Color = -1;

                    sb.append(ai1.toString()).append("\n").append("Human\n");

                    // When AI_Herald first(white), switch Human's color and let AI_Herald make one move first
                    switchColor();

                    letAiMove(ai1);
                }
                else {
                    ai1 = new AI_Herald(1, Pieces.getInstance());
                    ai1Color = 1;

                    sb.append("Human\n").append(ai1.toString()).append("\n");
                }
                break;
            }
            case PvP: {
                sb.append("Human\n").append("Human\n");
                break;
            }
            case AIvAI: {
                Random ran = new Random();
                int first = -1;
                if (ran.nextInt(2) % 2 == 0) {
                    first = -first;
                }

                ai1 = new AI_Herald(first, Pieces.getInstance());
                ai1Color = first;

                ai2 = new AI_Herald(-first, Pieces.getInstance());
                ai2Color = -first;

                if (ran.nextInt(2) % 2 == 0) {
                    sb.append(ai2.toString()).append("\n").append(ai1.toString()).append("\n");
                }
                else {
                    sb.append(ai1.toString()).append("\n").append(ai2.toString()).append("\n");
                }

                thread = new Thread(() -> {
                    while (Constants.gameStarted && !endThread) {
                        if (ai1.getColor() == color) {
                            letAiMove(ai1);
                        }
                        else {
                            letAiMove(ai2);
                        }

                        if (Constants.gameStarted) {
                            switchColor();
                        }

                        try {
                            Thread.sleep(Constants.aiThreadCycle);
                        }
                        catch (InterruptedException ie) {
                            ie.printStackTrace();
                            Platform.runLater(() ->
                                lblTxt.setText("Something went wrong with AI thread."));
                        }
                    }
                });
                endThread = false;
                thread.start();
            }
            default: {
                break;
            }
        }
    }

    private void btnEndFunc(boolean clearPieces) {
        if (clearPieces) {
            while (thread != null && thread.getState() != Thread.State.TERMINATED) {
                endThread = true;
            }
            Pieces.getInstance().clearPieces();
            clearAndDrawBoard();
            lblTxt.setText("");
            sb = null;
            btnSave.setDisable(true);
        }

        this.color = -1;
        Constants.gameStarted = false;
        ai1 = null;
        ai2 = null;
        ai1Color = 0;
        ai2Color = 0;
        sldSize.setDisable(false);
        btnMode.setDisable(false);
        btnLoad.setDisable(false);
        btnStart.setText("Start");
    }

    private void btnModeFunc() {
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

    private void btnSaveFunc() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());

        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new StringBuilder().append("./replay/").append(date.replaceAll(":", "_")).append(".txt").toString()), "utf-8");
            writer.write(sb.toString());
            writer.close();
        }
        catch (Exception ex) {
            lblTxt.setText("Unknown error. Failed to save.");
            ex.printStackTrace();
        }
    }

    private void btnLoadFunc() {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        fc.setTitle("Load Replay");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) {
            clearAndDrawBoard();
            btnStart.setDisable(true);
            btnMode.setDisable(true);
            sldSize.setDisable(true);
            btnLoad.setText("Stop");

            thread = new Thread(() -> {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(selectedFile));
                    String tempStr = null;
                    int line = 1;
                    for ( ;(tempStr = reader.readLine()) != null && !endThread; line++) {
                        switch (line) {
                            case 1: {
                                final String txt1 = tempStr;
                                Platform.runLater(() ->
                                    lblTxt.setText("(Black)" + txt1));
                                break;
                            }
                            case 2: {
                                final String txt2 = tempStr;
                                Platform.runLater(() ->
                                    lblTxt.setText(lblTxt.getText() + "\n\n(White)" + txt2));
                                break;
                            }
                            default: {
                                String[] arr = tempStr.split(" ");

                                final int tempColor;
                                if (line % 2 == 0) {
                                    tempColor = 1;
                                }
                                else {
                                    tempColor = -1;
                                }
                                Platform.runLater(() -> {
                                    drawPiece(new PieceInfo(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), tempColor));
                                });
                                break;
                            }
                        }

                        Thread.sleep(1000);
                    }
                    reader.close();
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                    Platform.runLater(() ->
                            lblTxt.setText("Something goes wrong with the file. \nFailed to load replay."));
                }
                catch (InterruptedException ie) {
                    ie.printStackTrace();
                    Platform.runLater(() ->
                            lblTxt.setText("Something goes wrong with the file. \nFailed to load replay."));
                }
                finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

                Platform.runLater(() -> lblTxt.setText(lblTxt.getText() + "\n\nReplay finished."));
                btnStart.setDisable(false);
                btnMode.setDisable(false);
                sldSize.setDisable(false);
                btnLoad.setText("Load");
            });
            endThread = false;
            thread.start();

        }
        else {
            lblTxt.setText("Something goes wrong with the file. \nFailed to load replay.");
        }
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
        while (!isMoveValid) {
            // too many failed attempts make failure indeed
            if (attempt < maxAttempts) {
                attempt++;
            }
            else {
                finishGame(-ai.getColor() * 2);
                return;
            }

            aiMove = ai.nextMove();
            if (Pieces.getInstance().checkPieceValidity(aiMove.getX(), aiMove.getY()) && aiMove.getColor() == (ai == ai1 ? ai1Color : ai2Color)) {
                isMoveValid = true;
                Pieces.getInstance().setPieceValue(aiMove);
                drawPiece(aiMove);

                sb.append(aiMove.getX()).append(" ").append(aiMove.getY()).append("\n");
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

                sb.append(tempPi.getX()).append(" ").append(tempPi.getY()).append("\n");

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

// TODO add Repentance