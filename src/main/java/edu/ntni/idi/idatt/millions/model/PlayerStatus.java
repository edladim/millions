package edu.ntni.idi.idatt.millions.model;

/**
 * Represents the progression status of a player.
 *
 * <p>A player's status is determined by how long the player has been active
 * in the market and how much the player's net worth has grown compared to
 * the starting capital.</p>
 *
 * <ul>
 *   <li>{@link #NOVICE} is the default starting status</li>
 *   <li>{@link #INVESTOR} requires at least 10 active trading weeks and
 *       at least 20% growth in net worth</li>
 *   <li>{@link #SPECULATOR} requires at least 20 active trading weeks and
 *       at least 100% growth in net worth</li>
 * </ul>
 */
public enum PlayerStatus {
  NOVICE,
  INVESTOR,
  SPECULATOR
}