package edu.ntnu.idi.idatt.millions.persistence.stock;

import edu.ntnu.idi.idatt.millions.model.market.Stock;
import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;
import java.util.List;

/**
 * Defines the contract for reading stock data from a data source.
 *
 * <p>Implementations may read from different formats (CSV, JSON, XML) or sources (filesystem,
 * classpath, network). This interface allows the rest of the application to remain independent of
 * the specific format or source used.
 *
 * @see CsvStockReader
 */
public interface StockReader {

  /**
   * Reads and returns all valid stocks from the data source.
   *
   * @return a non-null, non-empty list of stocks
   * @throws PersistenceException if the source cannot be read or contains no valid stock entries
   */
  List<Stock> readStockData() throws PersistenceException;
}
