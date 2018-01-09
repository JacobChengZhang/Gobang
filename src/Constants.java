public class Constants {
    public static boolean gameStarted = false;

    public enum  Mode{
        PvP, PvAI, AIvAI,
    }
    private static Mode mode = Mode.PvAI;
    public static Mode getMode() {
        return mode;
    }
    public static void setMode(Mode m) {
        if (!gameStarted) {
            mode = m;
        }
    }

    // TODO add differents ban rules
    private static boolean bans;

    public static final int minOrder = 11;
    public static final int maxOrder = 19;
    private static int order = 15; // better between 11~19
    public static int getOrder() {
        return order;
    }
    public static void setOrder(int i) {
        if (!gameStarted) {
            if (i >= minOrder && i <= maxOrder && i % 2 != 0) {
                // first, adjust border correspondingly
                border += ((order - i) * increment / 2);
                // change border
                order = i;
            }
        }
    }

    public static final int minBorder = 25;
    private static int border = 101;
    public static int getBorder() {
        return border;
    }

    public final static int btnPaneHeight = 35;
    public final static int increment = 38;
    public final static double pieceRadius = 16f;

    public final static String version = "1.1";
}
