package edu.ntnu.idi.idatt.millions.model;

import java.util.Objects;

/**
 * <p>Immutable record representing a single leaderboard entry.</p>
 *
 * <p>Each entry pairs a player name with their final Millions Score. Entries
 * are persisted between sessions so the all-time top scores survive
 * application restarts.</p>
 *
 * @param name the player's display name; must be non-null and non-blank
 * @param score the player's final Millions Score; must be {@code >= 0}
 */
public record LeaderboardEntry(String name, long score) {

  /**
   * <p>Compact constructor validating that the entry's fields are well-formed.</p>
   *
   * @throws NullPointerException if {@code name} is null
   * @throws IllegalArgumentException if {@code name} is blank or {@code score} is negative
   */
  public LeaderboardEntry {
    Objects.requireNonNull(name, "name cannot be null");
    if (name.isBlank()) {
      throw new IllegalArgumentException("name cannot be blank");
    }
    if (score < 0) {
      throw new IllegalArgumentException("score cannot be negative, got " + score);
    }
  }
}
