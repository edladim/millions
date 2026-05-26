package edu.ntnu.idi.idatt.millions.persistence.leaderboard;

import edu.ntnu.idi.idatt.millions.model.player.LeaderboardEntry;
import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads leaderboard entries from a CSV file.
 *
 * <p>The CSV format expected by this reader:
 *
 * <ul>
 *   <li>Lines starting with {@code #} are treated as comments and ignored.
 *   <li>Blank lines are ignored.
 *   <li>Data lines must follow the format: {@code name,score}
 *   <li>{@code score} must be a non-negative integer.
 * </ul>
 *
 * <p>Malformed lines are skipped with a warning so a single bad line does not abort the entire
 * read. A missing file is treated as an empty leaderboard.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * LeaderboardReader reader =
 *     new CsvLeaderboardReader(Path.of("/home/user/.millions/leaderboard.csv"));
 * List<LeaderboardEntry> entries = reader.readLeaderboard();
 * }</pre>
 *
 * @see LeaderboardReader
 * @see CsvLeaderboardWriter
 */
public class CsvLeaderboardReader implements LeaderboardReader {

  private static final Logger LOGGER = LogManager.getLogger(CsvLeaderboardReader.class);
  private static final int EXPECTED_FIELD_COUNT = 2;

  private final Path path;

  /**
   * Creates a reader for a CSV leaderboard file at the given path.
   *
   * <p>The file does not need to exist when this reader is constructed; a missing file is handled
   * gracefully by {@link #readLeaderboard()}.
   *
   * @param path the path to the leaderboard CSV file, cannot be null
   * @throws NullPointerException if {@code path} is null
   */
  public CsvLeaderboardReader(Path path) {
    this.path = Objects.requireNonNull(path, "path cannot be null");
  }

  /**
   * Reads and returns all valid leaderboard entries from the CSV source.
   *
   * <p>If the file does not exist, an empty list is returned. Malformed lines are skipped and
   * logged as warnings. An exception is only thrown if an unexpected I/O error occurs while reading
   * an existing file.
   *
   * @return a non-null list of {@link LeaderboardEntry} objects (possibly empty)
   * @throws PersistenceException if an I/O error occurs while reading the file
   */
  @Override
  public List<LeaderboardEntry> readLeaderboard() throws PersistenceException {
    if (!Files.exists(path)) {
      return Collections.emptyList();
    }

    List<LeaderboardEntry> entries = new ArrayList<>();
    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      String line;
      int lineNumber = 0;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        if (line.startsWith("#") || line.isBlank()) {
          continue;
        }
        parseLine(line, lineNumber).ifPresent(entries::add);
      }
    } catch (IOException e) {
      throw new PersistenceException("Failed to read leaderboard from '" + path + "'", e);
    }
    return entries;
  }

  /**
   * Attempts to parse a single CSV line into a {@link LeaderboardEntry}.
   *
   * <p>Returns an empty {@link Optional} and logs a warning if the line is malformed, so a single
   * bad line does not abort the entire file read.
   *
   * @param line the raw CSV line
   * @param lineNumber the 1-based line number, used in warning messages
   * @return an {@link Optional} containing the parsed entry, or empty if the line is invalid
   */
  private Optional<LeaderboardEntry> parseLine(String line, int lineNumber) {
    String[] fields = line.split(",");
    if (fields.length != EXPECTED_FIELD_COUNT) {
      LOGGER.warn(
          "Skipping leaderboard line {} in '{}': expected {} fields but found {} — \"{}\"",
          lineNumber,
          path,
          EXPECTED_FIELD_COUNT,
          fields.length,
          line);
      return Optional.empty();
    }

    String name = fields[0].trim();
    String scoreRaw = fields[1].trim();

    if (name.isBlank()) {
      LOGGER.warn("Skipping leaderboard line {} in '{}': name is blank", lineNumber, path);
      return Optional.empty();
    }

    long score;
    try {
      score = Long.parseLong(scoreRaw);
    } catch (NumberFormatException e) {
      LOGGER.warn(
          "Skipping leaderboard line {} in '{}': invalid score '{}'", lineNumber, path, scoreRaw);
      return Optional.empty();
    }

    if (score < 0) {
      LOGGER.warn(
          "Skipping leaderboard line {} in '{}': score must be non-negative, got {}",
          lineNumber,
          path,
          score);
      return Optional.empty();
    }

    return Optional.of(new LeaderboardEntry(name, score));
  }
}
