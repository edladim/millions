package edu.ntnu.idi.idatt.millions.model.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PlayerStatus}.
 *
 * <p>Covers the {@link PlayerStatus#displayName()} formatting method.</p>
 */
class PlayerStatusTest {

  /**
   * Verifies that BUY_HIGH_BJORN formats correctly.
   */
  @Test
  void displayName_buyHighBjorn_formatsCorrectly() {
    assertEquals("Buy high bjorn", PlayerStatus.BUY_HIGH_BJORN.displayName());
  }

  /**
   * Verifies that MAX_MINUS formats correctly.
   */
  @Test
  void displayName_maxMinus_formatsCorrectly() {
    assertEquals("Max minus", PlayerStatus.MAX_MINUS.displayName());
  }

  /**
   * Verifies that AVERAGE_JOE formats correctly.
   */
  @Test
  void displayName_averageJoe_formatsCorrectly() {
    assertEquals("Average joe", PlayerStatus.AVERAGE_JOE.displayName());
  }

  /**
   * Verifies that INVESTOR formats correctly (single word, no underscore).
   */
  @Test
  void displayName_investor_formatsCorrectly() {
    assertEquals("Investor", PlayerStatus.INVESTOR.displayName());
  }

  /**
   * Verifies that RAY_DALIO formats correctly.
   */
  @Test
  void displayName_rayDalio_formatsCorrectly() {
    assertEquals("Ray dalio", PlayerStatus.RAY_DALIO.displayName());
  }

  /**
   * Verifies that BERNARD_MADOFF formats correctly.
   */
  @Test
  void displayName_bernardMadoff_formatsCorrectly() {
    assertEquals("Bernard madoff", PlayerStatus.BERNARD_MADOFF.displayName());
  }

  /**
   * Verifies that displayName never returns null.
   */
  @Test
  void displayName_allValues_neverNull() {
    for (PlayerStatus status : PlayerStatus.values()) {
      assertNotNull(status.displayName());
    }
  }

  /**
   * Verifies that displayName always starts with an uppercase letter.
   */
  @Test
  void displayName_allValues_startsWithUpperCase() {
    for (PlayerStatus status : PlayerStatus.values()) {
      char first = status.displayName().charAt(0);
      assertTrue(Character.isUpperCase(first),
          "Expected uppercase first character for " + status);
    }
  }
}
