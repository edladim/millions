package edu.ntnu.idi.idatt.millions.persistence.leaderboard;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt.millions.model.player.LeaderboardEntry;
import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link CsvLeaderboardReader}.
 *
 * <p>Tests focus on CSV parsing correctness. A temporary directory is used to avoid coupling the
 * tests to any specific file on the developer's machine.
 *
 * <p>Verifies the following behaviours:
 *
 * <ul>
 *   <li>Valid entries are parsed correctly (name, score)
 *   <li>A missing file returns an empty list without throwing
 *   <li>Comment lines ({@code #}) and blank lines are ignored
 *   <li>Malformed lines are skipped (wrong field count, invalid score, negative score, blank name)
 *       while valid lines on the same file are still returned
 *   <li>Leading and trailing whitespace is trimmed from fields
 *   <li>A null path is rejected immediately
 * </ul>
 */
class CsvLeaderboardReaderTest {

  @TempDir Path tempDir;

  // Construction

  /** Verifies that a null path is rejected at construction time. */
  @Test
  void constructor_nullPath_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new CsvLeaderboardReader(null));
  }

  // Missing / empty file

  /** Verifies that a missing file returns an empty list instead of throwing. */
  @Test
  void readLeaderboard_missingFile_returnsEmptyList() throws PersistenceException {
    Path missing = tempDir.resolve("does_not_exist.csv");
    List<LeaderboardEntry> entries = new CsvLeaderboardReader(missing).readLeaderboard();
    assertTrue(entries.isEmpty());
  }

  /** Verifies that an empty file returns an empty list. */
  @Test
  void readLeaderboard_emptyFile_returnsEmptyList() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "");

    List<LeaderboardEntry> entries = new CsvLeaderboardReader(csv).readLeaderboard();

    assertTrue(entries.isEmpty());
  }

  // Positive cases

  /** Verifies that all valid lines in a file are returned. */
  @Test
  void readLeaderboard_validLines_returnsAllEntries() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "Alice,1500\nBob,800\nCarol,300\n");

    List<LeaderboardEntry> entries = new CsvLeaderboardReader(csv).readLeaderboard();

    assertEquals(3, entries.size());
  }

  /** Verifies that the name field is parsed correctly. */
  @Test
  void readLeaderboard_parsesNameCorrectly() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "Alice,1500\n");

    LeaderboardEntry entry = new CsvLeaderboardReader(csv).readLeaderboard().getFirst();

    assertEquals("Alice", entry.name());
  }

  /** Verifies that the score field is parsed correctly. */
  @Test
  void readLeaderboard_parsesScoreCorrectly() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "Alice,1500\n");

    LeaderboardEntry entry = new CsvLeaderboardReader(csv).readLeaderboard().getFirst();

    assertEquals(1500L, entry.score());
  }

  /** Verifies that a score of zero is accepted as a valid entry. */
  @Test
  void readLeaderboard_zeroScore_isValid() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "Alice,0\n");

    List<LeaderboardEntry> entries = new CsvLeaderboardReader(csv).readLeaderboard();

    assertEquals(1, entries.size());
    assertEquals(0L, entries.getFirst().score());
  }

  /** Verifies that comment lines and blank lines are ignored. */
  @Test
  void readLeaderboard_commentsAndBlanks_areIgnored() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "# Top scores\n\nAlice,1500\n\n# End\n");

    List<LeaderboardEntry> entries = new CsvLeaderboardReader(csv).readLeaderboard();

    assertEquals(1, entries.size());
  }

  /** Verifies that leading and trailing whitespace is trimmed from name and score. */
  @Test
  void readLeaderboard_trimsWhitespace() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "  Alice  ,  1500  \n");

    LeaderboardEntry entry = new CsvLeaderboardReader(csv).readLeaderboard().getFirst();

    assertEquals("Alice", entry.name());
    assertEquals(1500L, entry.score());
  }

  // Malformed lines are skipped, rest of file still parsed

  /** Verifies that a line with too few fields is skipped; other valid lines are returned. */
  @Test
  void readLeaderboard_tooFewFields_skipsLine() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "Alice\nBob,800\n");

    List<LeaderboardEntry> entries = new CsvLeaderboardReader(csv).readLeaderboard();

    assertEquals(1, entries.size());
    assertEquals("Bob", entries.getFirst().name());
  }

  /** Verifies that a line with too many fields is skipped; other valid lines are returned. */
  @Test
  void readLeaderboard_tooManyFields_skipsLine() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "Alice,1500,extra\nBob,800\n");

    List<LeaderboardEntry> entries = new CsvLeaderboardReader(csv).readLeaderboard();

    assertEquals(1, entries.size());
    assertEquals("Bob", entries.getFirst().name());
  }

  /** Verifies that a line with a non-numeric score is skipped. */
  @Test
  void readLeaderboard_invalidScore_skipsLine() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "Alice,not-a-number\nBob,800\n");

    List<LeaderboardEntry> entries = new CsvLeaderboardReader(csv).readLeaderboard();

    assertEquals(1, entries.size());
  }

  /** Verifies that a line with a negative score is skipped. */
  @Test
  void readLeaderboard_negativeScore_skipsLine() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "Alice,-100\nBob,800\n");

    List<LeaderboardEntry> entries = new CsvLeaderboardReader(csv).readLeaderboard();

    assertEquals(1, entries.size());
  }

  /** Verifies that a line with a blank name is skipped. */
  @Test
  void readLeaderboard_blankName_skipsLine() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    Files.writeString(csv, "   ,1500\nBob,800\n");

    List<LeaderboardEntry> entries = new CsvLeaderboardReader(csv).readLeaderboard();

    assertEquals(1, entries.size());
    assertEquals("Bob", entries.getFirst().name());
  }
}
