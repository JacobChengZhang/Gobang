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
import javafx.stage.Stage;

public class Gobang extends Application {

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
                if (checkMouseClick(me.getX(), me.getY())) {
                    int seqX = calcPieceSeq(me.getX());
                    int seqY = calcPieceSeq(me.getY());
                    if (Pieces.getInstance().checkAndDraw(seqX, seqY, color)) {
                        drawPiece(seqX, seqY, color);

                        int checkResult = Rules.checkWinningCondition(seqX, seqY);
                        if (checkResult != 0) {
                            closeout(checkResult);
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

    private void addControlButton() {
        btnStart = new Button("Start");
        btnStart.setPrefSize(Constants.btnPaneHeight * 2, Constants.btnPaneHeight * 2 / 3);
        btnStart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (Constants.gameStarted) {
                    Pieces.getInstance().clearPieces();
                    clearAndDrawBoard();
                    Constants.gameStarted = false;
                    sldSize.setDisable(false);
                    btnMode.setDisable(false);
                    lblTxt.setText("");
                    btnStart.setText("Start");
                }
                else {
                    Constants.gameStarted = true;
                    sldSize.setDisable(true);
                    btnMode.setDisable(true);
                    lblTxt.setText("White Move");
                    btnStart.setText("End");
                }
            }
        });

        btnMode = new Button("PvAI");
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

        lblTxt = new Label("Gobang " + Constants.version + ". Hope you enjoy!\n(Developed by JacobChengZhang)");

        HBox hBox = new HBox();
        hBox.setPrefSize(paneWidth, paneButtonHeight);
        hBox.setPadding(new Insets(0, 30, 0, 30));
        hBox.setSpacing(20);
        hBox.getChildren().addAll(btnStart, btnMode, lblSize, sldSize, lblTxt);
        hBox.setAlignment(Pos.CENTER_LEFT);

        paneButton.getChildren().add(hBox);
    }

    private void drawPiece(int x, int y, int color) {
        Circle p = new Circle();
        p.setCenterX(Constants.getBorder() + x * Constants.increment);
        p.setCenterY(Constants.getBorder() + y * Constants.increment);
        p.setRadius(Constants.pieceRadius);
        if (this.color == 1) {
            p.setFill(Color.WHITE);
        }
        else {
            p.setFill(Color.BLACK);
        }
        p.setStroke(Color.BLACK);

        paneBoard.getChildren().add(p);
    }

    private void playWinningAnimation() {
        //TODO
    }

    private void closeout(int result) {
        playWinningAnimation();

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
                lblTxt.setText("Caught a bug in Rules.");
                break;
            }
        }
        Constants.gameStarted = false;
        btnStart.setText("Start");
        sldSize.setDisable(false);
    }

    private Parent startGame() {
        createPane();

        clearAndDrawBoard();

        addControlButton();

        return root;
    }

    // A valid click should both satisfy (x, y coordinate close to gridPoint) and (the gridPoint has no piece on it)
    private boolean checkMouseClick(double meX, double meY) {
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
    private int calcPieceSeq(double meC) {
        int c = (int)(meC + 0.5);
        if ((c - Constants.getBorder()) % Constants.increment < Constants.increment / 3) {
            return (c - Constants.getBorder()) / Constants.increment;
        }
        else {
            return ((c - Constants.getBorder()) / Constants.increment) + 1;
        }
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

    @Override public void start(Stage primaryStage) {
        primaryStage.setTitle("Gobang");
        //primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(startGame()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}