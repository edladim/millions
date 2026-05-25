package edu.ntnu.idi.idatt.millions.persistence.stock;

import edu.ntnu.idi.idatt.millions.model.market.Stock;
import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;
import java.io.File;
import java.util.List;

/**
 * Service that loads stock data either from a user-selected file or from the bundled default
 * classpath resource.
 *
 * <p>This class isolates the "where do my stocks come from" decision from the UI layer. On any
 * failure it throws a {@link PersistenceException} with a user-friendly message that callers can
 * show directly.
 *
 * <p>This class cannot be instantiated; use the static method directly.
 */
public final class StockLoader {

  /** Classpath location of the bundled default stock file. */
  public static final String DEFAULT_STOCK_FILE = "/data/StockData.csv";

  private StockLoader() {}

  /**
   * Loads stock data from {@code file} if provided, falling back to the bundled classpath resource
   * if {@code file} is {@code null}.
   *
   * @param file the user-selected file, or {@code null} to use the bundled default
   * @return a non-empty list of stocks parsed from the source
   * @throws PersistenceException if the source cannot be read or contains no valid stock entries;
   *     the message is suitable for display to the end user
   */
  public static List<Stock> load(File file) throws PersistenceException {
    if (file != null) {
      try {
        return new CsvStockReader(file.toPath()).readStockData();
      } catch (PersistenceException e) {
        throw new PersistenceException("Could not read file: " + e.getMessage(), e);
      }
    }

    if (StockLoader.class.getResource(DEFAULT_STOCK_FILE) == null) {
      throw new PersistenceException(
          "Default StockData.csv not found. Please select a file manually.");
    }
    try {
      return CsvStockReader.fromClasspath(DEFAULT_STOCK_FILE).readStockData();
    } catch (PersistenceException e) {
      throw new PersistenceException("Could not load default stock data: " + e.getMessage(), e);
    }
  }
}
