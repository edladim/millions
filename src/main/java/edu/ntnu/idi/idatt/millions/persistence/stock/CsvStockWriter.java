package edu.ntnu.idi.idatt.millions.persistence.stock;

import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;

import edu.ntnu.idi.idatt.millions.model.market.Stock;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Writes stock data to a CSV file in append mode.
 *
 * <p>Each call to {@link #writeStockData(Stock)} appends one line to the target file
 * in the format: {@code SYMBOL,Company Name,currentSalesPrice}</p>
 *
 * <p>The current sales price ({@link Stock#getSalesPrice()}) is written, reflecting
 * the stock's latest market value rather than its original starting price.</p>
 *
 * <p>The target file is created if it does not yet exist.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * StockWriter writer = new CsvStockWriter(Path.of("/data/export.csv"));
 * writer.writeStockData(stock);
 * }</pre>
 *
 * @see StockWriter
 * @see CsvStockReader
 */
public class CsvStockWriter implements StockWriter {

  private static final Logger LOGGER = LogManager.getLogger(CsvStockWriter.class);

  private final Path path;

  /**
   * Creates a writer targeting the given file path.
   *
   * <p>The file is created automatically on first write if it does not exist.</p>
   *
   * @param path the destination file path, cannot be null
   * @throws NullPointerException if {@code path} is null
   */
  public CsvStockWriter(Path path) {
    this.path = Objects.requireNonNull(path, "path cannot be null");
  }

  /**
   * Appends a single stock entry to the CSV file.
   *
   * <p>The entry is written as: {@code SYMBOL,Company Name,salesPrice}</p>
   *
   * @param stock the stock to persist, cannot be null
   * @throws PersistenceException   if an I/O error occurs while writing
   * @throws NullPointerException if {@code stock} is null
   */
  @Override
  public void writeStockData(Stock stock) throws PersistenceException {
    Objects.requireNonNull(stock, "stock cannot be null");

    String line = stock.getSymbol() + ","
        + stock.getCompany() + ","
        + stock.getSalesPrice().toPlainString();

    try (BufferedWriter writer = Files.newBufferedWriter(
        path, StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      writer.write(line);
      writer.newLine();
    } catch (IOException e) {
      LOGGER.error("Failed to write stock to '{}': {}", path, e.getMessage());
      throw new PersistenceException("Failed to write stock data to '" + path + "'", e);
    }
  }
}