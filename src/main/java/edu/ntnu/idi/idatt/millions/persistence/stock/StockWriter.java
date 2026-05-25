package edu.ntnu.idi.idatt.millions.persistence.stock;

import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;

import edu.ntnu.idi.idatt.millions.model.market.Stock;

/**
 * Defines the contract for writing stock data to a data source.
 *
 * <p>Implementations may write to different formats (CSV, JSON, XML) or destinations
 * (filesystem, database). This interface allows the rest of the application
 * to remain independent of the specific format or destination used.</p>
 *
 * @see CsvStockWriter
 */
public interface StockWriter {

  /**
   * Appends a single stock entry to the data source.
   *
   * @param stock the stock to persist, cannot be null
   * @throws PersistenceException if the stock cannot be written
   * @throws NullPointerException if {@code stock} is null
   */
  void writeStockData(Stock stock) throws PersistenceException;
}
