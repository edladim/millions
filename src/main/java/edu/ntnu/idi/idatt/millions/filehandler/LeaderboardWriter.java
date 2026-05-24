package edu.ntnu.idi.idatt.millions.filehandler;

import edu.ntnu.idi.idatt.millions.model.LeaderboardEntry;
import java.util.List;

/**
 * <p>Defines the contract for writing leaderboard entries to a data source.</p>
 *
 * <p>Implementations persist the leaderboard as a single snapshot, each call
 * replaces the previous contents rather than appending — so the file always
 * reflects the current top-N list.</p>
 *
 * @see CsvLeaderboardWriter
 * @see LeaderboardReader
 */
public interface LeaderboardWriter {

  /**
   * <p>Writes the given entries to the data source, replacing any existing content.</p>
   *
   * @param entries the entries to persist; must be non-null
   * @throws StockFileException if an I/O error occurs while writing
   * @throws NullPointerException if {@code entries} is null
   */
  void writeLeaderboard(List<LeaderboardEntry> entries) throws StockFileException;
}
