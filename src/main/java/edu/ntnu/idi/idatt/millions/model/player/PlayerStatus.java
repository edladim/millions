package edu.ntnu.idi.idatt.millions.model.player;

/**
 * Represents the progression status of a player.
 *
 * <p>A player's status is determined by how long the player has been active in the market and how
 * much the player's net worth has grown compared to the starting capital.
 *
 * <ul>
 *   <li>{@link #INVESTOR} requires at least 10 active trading weeks and at least 20% growth in net
 *       worth
 * </ul>
 */
public enum PlayerStatus {
  BUY_HIGH_BJORN,
  MAX_MINUS,
  AVERAGE_JOE,
  INVESTOR,
  RAY_DALIO,
  BERNARD_MADOFF;

  /**
   * Returns a human-readable form of this status with underscores replaced by spaces and only the
   * first letter capitalised (e.g. {@code BUY_HIGH_BJORN} becomes {@code "Buy high bjorn"}).
   *
   * @return the display name for this status
   */
  public String displayName() {
    String raw = name().replace('_', ' ').toLowerCase();
    return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
  }
}
