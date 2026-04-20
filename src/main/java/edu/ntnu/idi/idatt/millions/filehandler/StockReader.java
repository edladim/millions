package edu.ntnu.idi.idatt.millions.filehandler;

import edu.ntnu.idi.idatt.millions.model.Stock;
import java.util.List;

/**
 * Defines the contract for reading stock data from a data source.
 *
 * <p>Implementations may read from different formats (CSV, JSON, XML) or sources
 * (filesystem, classpath, network). This interface allows the rest of the application
 * to remain independent of the specific format or source used.</p>
 *
 * @see CsvStockReader
 */
public interface StockReader {

  /**
   * Reads and returns all valid stocks from the data source.
   *
   * @return a non-null, non-empty list of stocks
   * @throws StockFileException if the source cannot be read or contains no valid stock entries
   */
  List<Stock> readStockData() throws StockFileException;
}
