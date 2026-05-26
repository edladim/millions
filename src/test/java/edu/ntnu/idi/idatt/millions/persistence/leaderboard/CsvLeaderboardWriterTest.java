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
 * Unit tests for {@link CsvLeaderboardWriter}.
 *
 * <p>Tests focus on correct serialisation of entries, overwrite semantics, and edge-case inputs. A
 * temporary directory is used to avoid coupling tests to any real file on the developer's machine.
 *
 * <p>Verifies the following behaviours:
 *
 * <ul>
 *   <li>Entries are written in {@code name,score} format, one per line
 *   <li>An existing file is fully replaced on each write
 *   <li>Missing parent directories are created automatically
 *   <li>A name containing a comma has its comma replaced with a space
 *   <li>Writing an empty list produces an empty file
 *   <li>A round-trip (write then read) returns the original entries
 *   <li>A null path or null entries list is rejected immediately
 * </ul>
 */
class CsvLeaderboardWriterTest {

  @TempDir Path tempDir;

  // Construction

  /** Verifies that a null path is rejected at construction time. */
  @Test
  void constructor_nullPath_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new CsvLeaderboardWriter(null));
  }

  // Null-guard on write

  /** Verifies that passing null as the entries list is rejected. */
  @Test
  void writeLeaderboard_nullEntries_throwsNullPointerException() {
    CsvLeaderboardWriter writer = new CsvLeaderboardWriter(tempDir.resolve("lb.csv"));
    assertThrows(NullPointerException.class, () -> writer.writeLeaderboard(null));
  }

  // Format correctness

  /** Verifies that each entry is written as {@code name,score} on its own line. */
  @Test
  void writeLeaderboard_validEntries_writesNameCommaScore() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    new CsvLeaderboardWriter(csv).writeLeaderboard(List.of(new LeaderboardEntry("Alice", 1500L)));

    String content = Files.readString(csv);
    assertTrue(content.contains("Alice,1500"), "Expected 'Alice,1500' in output");
  }

  /** Verifies that multiple entries each appear on a separate line. */
  @Test
  void writeLeaderboard_multipleEntries_eachOnOwnLine() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    new CsvLeaderboardWriter(csv)
        .writeLeaderboard(
            List.of(new LeaderboardEntry("Alice", 1500L), new LeaderboardEntry("Bob", 800L)));

    List<String> lines = Files.readAllLines(csv);
    assertEquals(2, lines.size());
  }

  /** Verifies that writing an empty list produces a file with no content. */
  @Test
  void writeLeaderboard_emptyList_producesEmptyFile() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    new CsvLeaderboardWriter(csv).writeLeaderboard(List.of());

    assertEquals("", Files.readString(csv));
  }

  // Overwrite semantics

  /** Verifies that a second write fully replaces the first write. */
  @Test
  void writeLeaderboard_secondWrite_truncatesExistingContent() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    CsvLeaderboardWriter writer = new CsvLeaderboardWriter(csv);

    writer.writeLeaderboard(List.of(new LeaderboardEntry("Alice", 1500L)));
    writer.writeLeaderboard(List.of(new LeaderboardEntry("Bob", 800L)));

    String content = Files.readString(csv);
    assertFalse(content.contains("Alice"), "Old entry should have been removed");
    assertTrue(content.contains("Bob"), "New entry should be present");
  }

  // Parent directory creation

  /** Verifies that missing parent directories are created automatically. */
  @Test
  void writeLeaderboard_missingParentDirs_createsThemAutomatically() throws Exception {
    Path csv = tempDir.resolve("a/b/c/leaderboard.csv");
    new CsvLeaderboardWriter(csv).writeLeaderboard(List.of(new LeaderboardEntry("Alice", 100L)));

    assertTrue(Files.exists(csv), "File should have been created including missing parents");
  }

  // Comma-sanitisation

  /** Verifies that a comma in a name is replaced with a space to keep the CSV valid. */
  @Test
  void writeLeaderboard_nameWithComma_replacesCommaWithSpace() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    new CsvLeaderboardWriter(csv)
        .writeLeaderboard(List.of(new LeaderboardEntry("Smith, John", 500L)));

    String content = Files.readString(csv);
    assertTrue(
        content.contains("Smith  John"), "Comma in name should have been replaced with a space");
  }

  // Round-trip

  /**
   * Verifies that a path with no parent component (a bare filename) is handled without an NPE,
   * exercising the {@code parent != null} branch.
   */
  @Test
  void writeLeaderboard_pathWithoutParent_writesSuccessfully() throws Exception {
    Path bare = Path.of("test_leaderboard_no_parent.csv");
    try {
      new CsvLeaderboardWriter(bare).writeLeaderboard(List.of(new LeaderboardEntry("Alice", 1L)));
      assertTrue(Files.exists(bare));
    } finally {
      Files.deleteIfExists(bare);
    }
  }

  /**
   * Verifies that an I/O failure while creating parent directories is surfaced as a {@link
   * PersistenceException}. Triggered by making the parent path point at an existing regular file.
   */
  @Test
  void writeLeaderboard_parentIsExistingFile_throwsPersistenceException() throws Exception {
    Path blocker = tempDir.resolve("blocker");
    Files.createFile(blocker);
    Path nested = blocker.resolve("leaderboard.csv");

    CsvLeaderboardWriter writer = new CsvLeaderboardWriter(nested);

    assertThrows(
        PersistenceException.class,
        () -> writer.writeLeaderboard(List.of(new LeaderboardEntry("Alice", 1L))));
  }

  /**
   * Verifies that an I/O failure while opening the target file for writing is surfaced as a {@link
   * PersistenceException}. Triggered by pointing the writer at an existing directory.
   */
  @Test
  void writeLeaderboard_pathIsDirectory_throwsPersistenceException() throws Exception {
    Path dir = tempDir.resolve("blocking_dir");
    Files.createDirectory(dir);

    CsvLeaderboardWriter writer = new CsvLeaderboardWriter(dir);

    assertThrows(
        PersistenceException.class,
        () -> writer.writeLeaderboard(List.of(new LeaderboardEntry("Alice", 1L))));
  }

  /** Verifies that entries written by the writer can be read back exactly by the reader. */
  @Test
  void writeLeaderboard_roundTrip_readBackProducesIdenticalEntries() throws Exception {
    Path csv = tempDir.resolve("leaderboard.csv");
    List<LeaderboardEntry> original =
        List.of(
            new LeaderboardEntry("Alice", 1500L),
            new LeaderboardEntry("Bob", 800L),
            new LeaderboardEntry("Carol", 300L));

    new CsvLeaderboardWriter(csv).writeLeaderboard(original);
    List<LeaderboardEntry> readBack = new CsvLeaderboardReader(csv).readLeaderboard();

    assertEquals(original, readBack);
  }
}
