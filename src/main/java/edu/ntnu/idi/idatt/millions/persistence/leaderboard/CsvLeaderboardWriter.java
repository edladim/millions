package edu.ntnu.idi.idatt.millions.persistence.leaderboard;

import edu.ntnu.idi.idatt.millions.model.player.LeaderboardEntry;
import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Writes leaderboard entries to a CSV file as a single snapshot.
 *
 * <p>Each call to {@link #writeLeaderboard(List)} truncates the target file and writes the full
 * list, so the file always reflects the current top-N leaderboard rather than a historical log.
 *
 * <p>The format written is: {@code name,score} (one entry per line).
 *
 * <p>Names containing a comma are sanitized by replacing the comma with a space — this writer does
 * not support CSV escaping. Application code is expected to keep names free of commas at the input
 * layer.
 *
 * <p>The target file (and any missing parent directories) is created on first write.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * LeaderboardWriter writer =
 *     new CsvLeaderboardWriter(Path.of("/home/user/.millions/leaderboard.csv"));
 * writer.writeLeaderboard(List.of(new LeaderboardEntry("Alice", 1234L)));
 * }</pre>
 *
 * @see LeaderboardWriter
 * @see CsvLeaderboardReader
 */
public class CsvLeaderboardWriter implements LeaderboardWriter {

  private static final Logger LOGGER = LogManager.getLogger(CsvLeaderboardWriter.class);

  private final Path path;

  /**
   * Creates a writer targeting the given file path.
   *
   * <p>The file and any missing parent directories are created automatically on first write.
   *
   * @param path the destination file path, cannot be null
   * @throws NullPointerException if {@code path} is null
   */
  public CsvLeaderboardWriter(Path path) {
    this.path = Objects.requireNonNull(path, "path cannot be null");
  }

  /**
   * Writes the given entries to the CSV file, replacing any existing content.
   *
   * @param entries the entries to persist, cannot be null
   * @throws PersistenceException if an I/O error occurs while writing
   * @throws NullPointerException if {@code entries} is null
   */
  @Override
  public void writeLeaderboard(List<LeaderboardEntry> entries) throws PersistenceException {
    Objects.requireNonNull(entries, "entries cannot be null");

    try {
      Path parent = path.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
    } catch (IOException e) {
      LOGGER.error("Failed to create parent directory for '{}': {}", path, e.getMessage());
      throw new PersistenceException("Failed to create parent directory for '" + path + "'", e);
    }

    try (BufferedWriter writer =
        Files.newBufferedWriter(
            path,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
      for (LeaderboardEntry entry : entries) {
        String safeName = entry.name().replace(',', ' ');
        writer.write(safeName + "," + entry.score());
        writer.newLine();
      }
    } catch (IOException e) {
      LOGGER.error("Failed to write leaderboard to '{}': {}", path, e.getMessage());
      throw new PersistenceException("Failed to write leaderboard to '" + path + "'", e);
    }
  }
}
