import com.oracle.tools.packager.JreUtils;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class Gobang extends Application {

    private MainFrame mainFrame;

    private boolean gameEnd = false;

    // 1: white   -1:black
    private int color = 1;

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    private Parent startGame() {
        Pane root = new Pane();
        mainFrame = new MainFrame(root);

        // paneWidth: |<-   border  ->|<-   ((order - 1) * increment)   ->|<-   border   ->|   (Y is the same)
        int paneWidth = Constants.border * 2 + (Constants.order - 1) * Constants.increment;
        int paneHeight = Constants.border * 2 + (Constants.order - 1) * Constants.increment;
        root.setPrefSize(paneWidth, paneHeight);
        root.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        root.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        drawLines(paneWidth, paneHeight);

        root.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (gameEnd) {
                    return;
                }
                if (checkMouseClick(me.getX(), me.getY())) {
                    int seqX = getPieceSeq(me.getX());
                    int seqY = getPieceSeq(me.getY());
                    if (Pieces.getInstance().checkAndDraw(seqX, seqY, color)) {
                        drawPiece(seqX, seqY, color);

                        // TODO add popup windows when game ends
                        switch (Rules.checkWinningCondition(seqX, seqY)) {
                            case 0: {
                                break;
                            }
                            case 1: {
                                System.out.println("White wins!");
                                gameEnd = true;
                                break;
                            }
                            case -1: {
                                System.out.println("Black wins!");
                                gameEnd = true;
                                break;
                            }
                            case -100: {
                                System.out.println("Oops, ended in a draw!");
                                gameEnd = true;
                                break;
                            }
                            default: {
                                System.out.println("Caught a bug in Rules.");
                                gameEnd = true;
                                break;
                            }
                        }

                        switchColor();
                    }
                }
                else {
                    // do nothing
                }
            }
        });

        return root;
    }

    private void drawLines(int paneWidth, int paneHeight) {
        for (int i = 0; i < Constants.order; i++) {
            Line lineX = new Line(Constants.border, Constants.border + i * Constants.increment, paneWidth - Constants.border, Constants.border + i * Constants.increment);
            Line lineY = new Line(Constants.border + i * Constants.increment, Constants.border, Constants.border + i * Constants.increment, paneHeight - Constants.border);
            mainFrame.root.getChildren().add(lineX);
            mainFrame.root.getChildren().add(lineY);
        }
    }

    private void drawPiece(int x, int y, int color) {
        Circle p = new Circle();
        p.setCenterX(Constants.border + x * Constants.increment);
        p.setCenterY(Constants.border + y * Constants.increment);
        p.setRadius(Constants.pieceRadius);
        if (this.color == 1) {
            p.setFill(Color.WHITE);
        }
        else {
            p.setFill(Color.BLACK);
        }
        p.setStroke(Color.BLACK);

        mainFrame.root.getChildren().add(p);
    }

    // A valid click should both satisfy (x, y coordinate close to gridPoint) and (the gridPoint has no piece on it)
    private boolean checkMouseClick(double meX, double meY) {
        boolean validX = false;
        boolean validY = false;
        int x = (int)(meX + 0.5);
        int y = (int)(meY + 0.5);

        if ((x - Constants.border) % Constants.increment < Constants.increment / 3 || (x - Constants.border) % Constants.increment > Constants.increment * 2 / 3) {
            validX = true;
        }
        if ((y - Constants.border) % Constants.increment < Constants.increment / 3 || (y - Constants.border) % Constants.increment > Constants.increment * 2 / 3) {
            validY = true;
        }

        return validX && validY;
    }

    // x or y coordinate -> sequence number in Pieces.p[][]
    private int getPieceSeq(double meC) {
        int c = (int)(meC + 0.5);
        if ((c - Constants.border) % Constants.increment < Constants.increment / 3) {
            return (c - Constants.border) / Constants.increment;
        }
        else {
            return ((c - Constants.border) / Constants.increment) + 1;
        }
    }

    // also means switch player
    private void switchColor() {
        this.color = -this.color;
    }

    @Override public void start(Stage primaryStage) {
        primaryStage.setTitle("Gobang");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(startGame()));
        primaryStage.show();
    }

    private class MainFrame {
        private Pane root;

        private MainFrame(Pane root) {
            this.root = root;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}