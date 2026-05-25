package edu.ntnu.idi.idatt.millions.model.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LeaderboardEntry}.
 *
 * <p>Covers valid construction and rejection of invalid arguments.</p>
 */
class LeaderboardEntryTest {

  /**
   * Verifies that a valid entry stores name and score correctly.
   */
  @Test
  void constructor_validEntry_storesNameAndScore() {
    LeaderboardEntry entry = new LeaderboardEntry("Alice", 1500L);
    assertEquals("Alice", entry.name());
    assertEquals(1500L, entry.score());
  }

  /**
   * Verifies that a score of zero is accepted (no-gain run is still valid).
   */
  @Test
  void constructor_zeroScore_isValid() {
    assertDoesNotThrow(() -> new LeaderboardEntry("Alice", 0L));
  }

  /**
   * Verifies that a null name is rejected.
   */
  @Test
  void constructor_nullName_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new LeaderboardEntry(null, 100L));
  }

  /**
   * Verifies that a blank name is rejected.
   */
  @Test
  void constructor_blankName_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new LeaderboardEntry("   ", 100L));
  }

  /**
   * Verifies that an empty name is rejected.
   */
  @Test
  void constructor_emptyName_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new LeaderboardEntry("", 100L));
  }

  /**
   * Verifies that a negative score is rejected.
   */
  @Test
  void constructor_negativeScore_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> new LeaderboardEntry("Alice", -1L));
  }

  /**
   * Verifies that two entries with identical fields are considered equal.
   */
  @Test
  void equals_identicalEntries_areEqual() {
    LeaderboardEntry a = new LeaderboardEntry("Bob", 200L);
    LeaderboardEntry b = new LeaderboardEntry("Bob", 200L);
    assertEquals(a, b);
  }

  /**
   * Verifies that entries with different scores are not equal.
   */
  @Test
  void equals_differentScores_areNotEqual() {
    LeaderboardEntry a = new LeaderboardEntry("Bob", 200L);
    LeaderboardEntry b = new LeaderboardEntry("Bob", 201L);
    assertNotEquals(a, b);
  }
}
