package edu.ntnu.idi.idatt.millions.persistence.leaderboard;

import edu.ntnu.idi.idatt.millions.model.player.LeaderboardEntry;
import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Facade for persisting and retrieving the leaderboard.
 *
 * <p>Hides the underlying CSV reader and writer behind a small static API and owns the canonical
 * leaderboard file location at {@code ~/.millions/leaderboard.csv}. Mirrors the role of {@link
 * edu.ntnu.idi.idatt.millions.persistence.stock.StockLoader} for stock data.
 *
 * <p>UI code should call these methods directly, if the underlying file is corrupt or unreadable,
 * errors are logged and the leaderboard is treated as empty rather than propagating exceptions to
 * the view layer.
 */
public final class LeaderboardStore {

  private static final Logger LOGGER = LogManager.getLogger(LeaderboardStore.class);

  /** Maximum number of entries kept in the leaderboard. */
  public static final int MAX_ENTRIES = 5;

  /**
   * Canonical location of the leaderboard file on disk.
   *
   * <p>Not declared {@code final} so tests can redirect it to a temporary location via reflection;
   * in production this value is never reassigned.
   */
  private static Path DEFAULT_PATH =
      Paths.get(System.getProperty("user.home"), ".millions", "leaderboard.csv");

  private LeaderboardStore() {
    // Utility class — no instances.
  }

  /**
   * Loads the current top-{@value #MAX_ENTRIES} entries sorted by score descending.
   *
   * <p>Returns an empty list if no leaderboard file exists yet or if the file cannot be read. UI
   * code can rely on this method never throwing.
   *
   * @return a non-null list of at most {@value #MAX_ENTRIES} entries
   */
  public static List<LeaderboardEntry> loadTop() {
    try {
      List<LeaderboardEntry> entries = new CsvLeaderboardReader(DEFAULT_PATH).readLeaderboard();
      entries.sort(Comparator.comparingLong(LeaderboardEntry::score).reversed());
      if (entries.size() > MAX_ENTRIES) {
        return new ArrayList<>(entries.subList(0, MAX_ENTRIES));
      }
      return entries;
    } catch (PersistenceException e) {
      LOGGER.warn("Could not load leaderboard, treating as empty: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * Submits a new entry to the leaderboard and persists the updated top list.
   *
   * <p>The submitted entry is inserted into the existing list, the list is sorted by score
   * descending, trimmed to {@value #MAX_ENTRIES}, and written back to disk. If the submitted entry
   * survives the trim, its 1-based rank (1–{@value #MAX_ENTRIES}) is returned; otherwise {@code
   * null} is returned.
   *
   * <p>If writing fails the error is logged but the rank is still returned — UI code should not be
   * blocked from displaying the result of the game just because persistence failed.
   *
   * @param name the player's name; must be non-null and non-blank
   * @param score the player's final score; must be {@code >= 0}
   * @return the player's 1-based rank if they made the top {@value #MAX_ENTRIES}, else {@code null}
   * @throws NullPointerException if {@code name} is null
   * @throws IllegalArgumentException if {@code name} is blank or {@code score} is negative
   */
  public static Integer submit(String name, long score) {
    Objects.requireNonNull(name, "name cannot be null");
    LeaderboardEntry submitted = new LeaderboardEntry(name, score);

    List<LeaderboardEntry> entries;
    try {
      entries = new ArrayList<>(new CsvLeaderboardReader(DEFAULT_PATH).readLeaderboard());
    } catch (PersistenceException e) {
      LOGGER.warn("Could not read leaderboard before submit, starting fresh: {}", e.getMessage());
      entries = new ArrayList<>();
    }

    entries.add(submitted);
    entries.sort(Comparator.comparingLong(LeaderboardEntry::score).reversed());
    if (entries.size() > MAX_ENTRIES) {
      entries = new ArrayList<>(entries.subList(0, MAX_ENTRIES));
    }

    try {
      new CsvLeaderboardWriter(DEFAULT_PATH).writeLeaderboard(entries);
    } catch (PersistenceException e) {
      LOGGER.error("Failed to persist leaderboard: {}", e.getMessage());
    }

    int index = entries.indexOf(submitted);
    return index >= 0 ? index + 1 : null;
  }
}
