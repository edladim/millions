package edu.ntnu.idi.idatt.millions.persistence.stock;

import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;

import edu.ntnu.idi.idatt.millions.model.market.Stock;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads stock data from a CSV file.
 *
 * <p>The CSV format expected by this reader:</p>
 * <ul>
 *   <li>Lines starting with {@code #} are treated as comments and ignored.</li>
 *   <li>Blank lines are ignored.</li>
 *   <li>Data lines must follow the format: {@code SYMBOL,Company Name,price}</li>
 *   <li>The decimal separator must be a period ({@code .}).</li>
 * </ul>
 *
 * <p>Malformed lines are skipped with a warning rather than failing the entire read.
 * An exception is only thrown if the source cannot be opened or yields no valid stocks.</p>
 *
 * <p>Supports both filesystem paths and classpath resources:</p>
 * <pre>{@code
 * // From a user-selected file:
 * StockReader reader = new CsvStockReader(Path.of("/home/user/stocks.csv"));
 *
 * // From a bundled classpath resource:
 * StockReader reader = CsvStockReader.fromClasspath("/data/StockData.csv");
 * }</pre>
 *
 * @see StockReader
 * @see CsvStockWriter
 */
public class CsvStockReader implements StockReader {

  private static final Logger LOGGER = LogManager.getLogger(CsvStockReader.class);
  private static final int EXPECTED_FIELD_COUNT = 3;

  private final InputStream inputStream;
  private final String sourceDescription;

  /**
   * Creates a reader for a CSV file on the filesystem.
   *
   * @param path the path to the CSV file, cannot be null
   * @throws PersistenceException   if the file cannot be opened
   * @throws NullPointerException if {@code path} is null
   */
  public CsvStockReader(Path path) throws PersistenceException {
    Objects.requireNonNull(path, "path cannot be null");
    try {
      this.inputStream = Files.newInputStream(path);
      this.sourceDescription = path.toString();
    } catch (IOException e) {
      throw new PersistenceException("Cannot open file: " + path, e);
    }
  }

  private CsvStockReader(InputStream inputStream, String sourceDescription) {
    this.inputStream = inputStream;
    this.sourceDescription = sourceDescription;
  }

  /**
   * Creates a reader for a CSV resource on the classpath.
   *
   * <p>Useful for loading the bundled default stock data at application startup.</p>
   *
   * @param resource classpath-relative path, e.g. {@code "/data/StockData.csv"}
   * @return a new {@code CsvStockReader} targeting the given resource
   * @throws PersistenceException   if the resource cannot be found on the classpath
   * @throws NullPointerException if {@code resource} is null
   */
  public static CsvStockReader fromClasspath(String resource) throws PersistenceException {
    Objects.requireNonNull(resource, "resource cannot be null");
    InputStream is = CsvStockReader.class.getResourceAsStream(resource);
    if (is == null) {
      throw new PersistenceException("Classpath resource not found: " + resource);
    }
    return new CsvStockReader(is, "classpath:" + resource);
  }

  /**
   * Reads and returns all valid stock entries from the CSV source.
   *
   * <p>Malformed and duplicate lines are skipped and logged as warnings. The
   * method throws only if the source cannot be read at all, or if no valid
   * stocks are found.</p>
   *
   * @return a non-null, non-empty list of {@link Stock} objects
   * @throws PersistenceException if an I/O error occurs, or no valid stocks are found
   */
  @Override
  public List<Stock> readStockData() throws PersistenceException {
    List<Stock> stocks = new ArrayList<>();
    Set<String> seen = new HashSet<>();

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

      String line;
      int lineNumber = 0;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        if (line.startsWith("#") || line.isBlank()) {
          continue;
        }
        parseLine(line, lineNumber, seen).ifPresent(stocks::add);
      }
    } catch (IOException e) {
      throw new PersistenceException(
          "Failed to read stock data from '" + sourceDescription + "'", e);
    }

    if (stocks.isEmpty()) {
      throw new PersistenceException(
          "No valid stock entries found in '" + sourceDescription + "'");
    }
    return stocks;
  }

  /**
   * Attempts to parse a single CSV line into a {@link Stock}.
   *
   * <p>Returns an empty {@link Optional} and logs a warning if the line is malformed,
   * so a single bad line does not abort the entire file read.</p>
   *
   * @param line       the raw CSV line
   * @param lineNumber the 1-based line number, used in warning messages
   * @param seen       set of symbols already accepted; lines with a duplicate
   *                   symbol are skipped so the first occurrence wins
   * @return an {@link Optional} containing the parsed stock, or empty if the line is invalid
   */
  private Optional<Stock> parseLine(String line, int lineNumber, Set<String> seen) {
    String[] fields = line.split(",");

    if (fields.length != EXPECTED_FIELD_COUNT) {
      LOGGER.warn("Skipping line {} in '{}': expected {} fields but found {} — \"{}\"",
          lineNumber, sourceDescription, EXPECTED_FIELD_COUNT, fields.length, line);
      return Optional.empty();
    }

    String symbol = fields[0].trim();
    String company = fields[1].trim();
    String priceRaw = fields[2].trim();

    if (symbol.isBlank()) {
      LOGGER.warn("Skipping line {} in '{}': symbol is blank", lineNumber, sourceDescription);
      return Optional.empty();
    }
    if (company.isBlank()) {
      LOGGER.warn("Skipping line {} in '{}': company name is blank",
          lineNumber, sourceDescription);
      return Optional.empty();
    }
    if (!seen.add(symbol)) {
      LOGGER.warn("Skipping duplicate symbol '{}' on line {} in '{}'",
          symbol, lineNumber, sourceDescription);
      return Optional.empty();
    }

    BigDecimal price;
    try {
      price = new BigDecimal(priceRaw);
    } catch (NumberFormatException e) {
      LOGGER.warn("Skipping line {} in '{}': invalid price value '{}'",
          lineNumber, sourceDescription, priceRaw);
      return Optional.empty();
    }

    if (price.compareTo(BigDecimal.ZERO) <= 0) {
      LOGGER.warn("Skipping line {} in '{}': price must be positive, got {}",
          lineNumber, sourceDescription, price);
      return Optional.empty();
    }

    return Optional.of(new Stock(symbol, company, price));
  }
}