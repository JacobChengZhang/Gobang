package Gomoku;

public class Configuration {

  public static final String version = "1.8";
  enum Mode {
    PvP, PvAI, AIvAI,
  }

  static final int threadPoolLimit = 3;
  static final int aiThreadCycle = 300;
  static final int loadThreadCycle = 500;

  static final String aiBlack = "AI.AI_Guardian";
  static final String aiWhite = "AI.AI_Guardian";

  // max number of failed attempts that AI can make
  static final int maxAttempts = 10;

  // TODO add a calculating time limit for AI

  static final boolean isAIvAISilently = false;
  static final boolean isManualLoad = true;

  static final int minOrder = 11;
  static final int maxOrder = 19;
  static final int minBorder = 25;
  static final int increment = 38;
  static final int btnPaneWidth = 180;
  static final double pieceRadius = 16f;
  static final double lineWidth = 1f;
  static final double dotRadius = 4f;

  static boolean bans = false; // haven't and may not be implemented

}
