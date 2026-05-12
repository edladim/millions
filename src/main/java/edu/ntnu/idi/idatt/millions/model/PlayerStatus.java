package edu.ntnu.idi.idatt.millions.model;

/**
 * Represents the progression status of a player.
 *
 * <p>A player's status is determined by how long the player has been active
 * in the market and how much the player's net worth has grown compared to
 * the starting capital.</p>
 *
 * <ul>
 *   <li>{@link #INVESTOR} requires at least 10 active trading weeks and
 *       at least 20% growth in net worth</li>
 * </ul>
 */
public enum PlayerStatus {
  BUY_HIGH_BJORN,
  MAX_MINUS,
  AVERAGE_JOE,
  INVESTOR,
  RAY_DALIO,
  BERNARD_MADOFF
}