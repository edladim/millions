package edu.ntni.idi.idatt.millions.fileHandler;

import edu.ntni.idi.idatt.millions.model.Stock;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Responsible for reading and parsing data from a CSV resource file
 * on the classpath.
 *
 * <p>This class acts as the read-side of the file handler layer, translating
 * raw CSV content into domain objects that the rest of the application can work with.
 * It is the counterpart to {@link WriteCSV}, which handles persistence.</p>
 *
 * <p>The resource file must be located on the classpath, typically under
 * {@code src/main/resources}. The file path is provided at construction time.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * ReadCSV reader = new ReadCSV("/StockData.csv");
 * List<Stock> stocks = reader.readStockData();
 * }</pre>
 *
 * @see WriteCSV
 * @see Stock
 */
public class ReadCSV {

  private String fileName;

  /**
   * Constructs a new {@code WriteCSV} instance targeting the given classpath resource.
   *
   * @param fileName the classpath-relative path to the CSV file (e.g. {@code "/StockData.csv"}),
   *                 cannot be null
   * @throws NullPointerException if {@code fileName} is null
   */
  public ReadCSV(String fileName) {
    this.fileName = Objects.requireNonNull(fileName, "fileName cannot be null");
  }

  /**
   * Reads stock data from the CSV resource file and returns a list of {@link Stock} objects.
   *
   * <p>The CSV file is expected to follow this format:</p>
   * <pre>{@code
   * # Comment lines starting with '#' are ignored
   * AAPL,Apple Inc.,276.43
   * MSFT,Microsoft,404.68
   * }</pre>
   *
   * <p>Each valid line must contain exactly three comma-separated fields:
   * ticker symbol, company name, and sale price. Lines that are blank
   * or start with {@code #} are skipped.</p>
   *
   * @return a list of {@link Stock} objects parsed from the CSV file,
   *         never null but may be empty if the file contains no valid entries
   */
  public List<Stock> readStockData() {
    List<Stock> stocks = new ArrayList<>();

    try (InputStream is = getClass().getResourceAsStream(fileName);
          BufferedReader bf = new BufferedReader(new InputStreamReader(is))) {

      String line;
      while ((line = bf.readLine()) != null) {
        if (line.startsWith("#") || line.isBlank()) continue;

        String[] fields = line.split(",");
        if (fields.length == 3) {
          String symbol = fields[0].trim();
          String company = fields[1].trim();
          BigDecimal salesPrice = new BigDecimal(fields[2].trim());
          stocks.add(new Stock(symbol, company, salesPrice));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return stocks;
  }

}
