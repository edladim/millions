package edu.ntnu.idi.idatt.millions.filehandler;

import edu.ntnu.idi.idatt.millions.model.Stock;
import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * <p>Service that loads stock data either from a user-selected file or from
 * the bundled default classpath resource.</p>
 *
 * <p>This class isolates the "where do my stocks come from" decision from the
 * UI layer. On any failure it throws a {@link StockFileException} with a
 * user-friendly message that callers can show directly.</p>
 *
 * <p>This class cannot be instantiated; use the static method directly.</p>
 */
public final class StockLoader {

  /** Classpath location of the bundled default stock file. */
  public static final String DEFAULT_STOCK_FILE = "/StockData.csv";

  private StockLoader() {}

  /**
   * <p>Loads stock data from {@code file} if provided, falling back to the
   * bundled classpath resource if {@code file} is {@code null}.</p>
   *
   * @param file the user-selected file, or {@code null} to use the bundled default
   * @return a non-empty list of stocks parsed from the source
   * @throws StockFileException if the source cannot be read or contains no
   *                            valid stock entries; the message is suitable
   *                            for display to the end user
   */
  public static List<Stock> load(File file) throws StockFileException {
    if (file != null) {
      try {
        return new CsvStockReader(file.toPath()).readStockData();
      } catch (StockFileException e) {
        throw new StockFileException("Could not read file: " + e.getMessage(), e);
      }
    }

    InputStream stream = StockLoader.class.getResourceAsStream(DEFAULT_STOCK_FILE);
    if (stream == null) {
      throw new StockFileException(
          "Default StockData.csv not found. Please select a file manually.");
    }
    try {
      return CsvStockReader.fromClasspath(DEFAULT_STOCK_FILE).readStockData();
    } catch (StockFileException e) {
      throw new StockFileException("Could not load default stock data: " + e.getMessage(), e);
    }
  }
}
