package edu.ntni.idi.idatt.millions.fileHandler;

import edu.ntni.idi.idatt.millions.model.Stock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * Responsible for writing and persisting data to a CSV file on disk.
 *
 * <p>This class acts as the write-side of the file handler layer, translating
 * domain objects into raw CSV content that can be stored for later retrieval.
 * It is the counterpart to {@link ReadCSV}, which handles parsing.</p>
 *
 * <p>The target file is {@code /StockData.csv}, located on the classpath
 * under {@code src/main/resources}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * WriteCSV writer = new WriteCSV();
 * writer.writeStockData(new Stock("AAPL", "Apple Inc.", new BigDecimal("276.43")));
 * }</pre>
 *
 * @see ReadCSV
 * @see Stock
 */
public class WriteCSV {

  /**
   * Appends a single {@link Stock} entry to the CSV file.
   *
   * <p>The stock is written as one comma-separated line in the format:</p>
   * <pre>{@code
   * AAPL,Apple Inc.,276.43
   * }</pre>
   *
   * <p>Each entry is appended on a new line without overwriting existing data.</p>
   *
   * @param stock the stock to persist, cannot be null
   * @throws RuntimeException if an I/O error occurs while writing to the file
   */
  public void writeStockData(Stock stock) {
    String fileName = Objects.requireNonNull(getClass().getResource("/StockData.csv")).getPath();
    try (FileWriter fileWriter = new FileWriter(fileName, true);
         BufferedWriter writer = new BufferedWriter(fileWriter)) {
      String line = stock.getSymbol() + "," +
              stock.getCompany() + "," +
              stock.getPrices().getFirst();
      writer.newLine();
      writer.write(line);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
