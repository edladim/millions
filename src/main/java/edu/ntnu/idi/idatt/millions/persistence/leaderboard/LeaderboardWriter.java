package edu.ntnu.idi.idatt.millions.persistence.leaderboard;

import edu.ntnu.idi.idatt.millions.model.player.LeaderboardEntry;
import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;
import java.util.List;

/**
 * Defines the contract for writing leaderboard entries to a data source.
 *
 * <p>Implementations persist the leaderboard as a single snapshot, each call replaces the previous
 * contents rather than appending — so the file always reflects the current top-N list.
 *
 * @see CsvLeaderboardWriter
 * @see LeaderboardReader
 */
public interface LeaderboardWriter {

  /**
   * Writes the given entries to the data source, replacing any existing content.
   *
   * @param entries the entries to persist; must be non-null
   * @throws PersistenceException if an I/O error occurs while writing
   * @throws NullPointerException if {@code entries} is null
   */
  void writeLeaderboard(List<LeaderboardEntry> entries) throws PersistenceException;
}
