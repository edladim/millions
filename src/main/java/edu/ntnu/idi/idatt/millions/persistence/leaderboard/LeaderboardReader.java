package edu.ntnu.idi.idatt.millions.persistence.leaderboard;

import edu.ntnu.idi.idatt.millions.model.player.LeaderboardEntry;
import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;
import java.util.List;

/**
 * Defines the contract for reading leaderboard entries from a data source.
 *
 * <p>Implementations may read from different formats (CSV, JSON) or sources (filesystem,
 * classpath). This interface keeps the rest of the application independent of the specific format
 * or location.
 *
 * @see CsvLeaderboardReader
 * @see LeaderboardWriter
 */
public interface LeaderboardReader {

  /**
   * Reads and returns all valid leaderboard entries from the data source.
   *
   * <p>Unlike {@link edu.ntnu.idi.idatt.millions.persistence.stock.StockLoader}, an empty result is
   * valid, a fresh installation has no recorded scores. Implementations should also treat a missing
   * file as an empty list rather than as an error.
   *
   * @return a non-null list of entries (possibly empty)
   * @throws PersistenceException if the source cannot be read due to an I/O error
   */
  List<LeaderboardEntry> readLeaderboard() throws PersistenceException;
}
