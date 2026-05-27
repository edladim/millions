package edu.ntnu.idi.idatt.millions.persistence.stock;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt.millions.model.market.Stock;
import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link StockLoader}.
 *
 * <p>Verifies the two loading paths:
 *
 * <ul>
 *   <li>{@code file == null} — falls back to the bundled classpath resource
 *   <li>{@code file != null} — reads from the supplied filesystem path
 * </ul>
 *
 * <p>Also verifies that missing or invalid files produce a {@link PersistenceException} with a
 * descriptive message.
 */
class StockLoaderTest {

  @TempDir Path tempDir;

  // Classpath fallback

  /**
   * Verifies that passing {@code null} loads the bundled default stock data and returns a non-empty
   * list.
   */
  @Test
  void load_nullFile_loadsDefaultClasspathResource() throws PersistenceException {
    List<Stock> stocks = StockLoader.load(null);
    assertFalse(stocks.isEmpty(), "Default classpath resource should contain at least one stock");
  }

  /**
   * Verifies that the stocks loaded from the classpath resource are valid (non-null symbols with
   * positive prices).
   */
  @Test
  void load_nullFile_stocksHaveValidSymbolsAndPositivePrices() throws PersistenceException {
    List<Stock> stocks = StockLoader.load(null);
    for (Stock stock : stocks) {
      assertNotNull(stock.getSymbol(), "Symbol should not be null");
      assertTrue(stock.getSalesPrice().signum() > 0, "Price should be positive");
    }
  }

  // Filesystem path

  /** Verifies that a valid CSV file on disk is read and all entries returned. */
  @Test
  void load_validFile_returnsStocks() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "AAPL,Apple Inc.,150.00\nMSFT,Microsoft,300.00\n");

    List<Stock> stocks = StockLoader.load(csv.toFile());

    assertEquals(2, stocks.size());
  }

  /** Verifies that the symbol from a custom file is parsed correctly. */
  @Test
  void load_validFile_parsesSymbolCorrectly() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "NVDA,Nvidia,500.00\n");

    Stock stock = StockLoader.load(csv.toFile()).getFirst();

    assertEquals("NVDA", stock.getSymbol());
  }

  // Error cases

  /** Verifies that a non-existent file throws a {@link PersistenceException}. */
  @Test
  void load_nonExistentFile_throwsPersistenceException() {
    File missing = tempDir.resolve("no_such_file.csv").toFile();
    assertThrows(PersistenceException.class, () -> StockLoader.load(missing));
  }

  /** Verifies that a file with no valid stock entries throws a {@link PersistenceException}. */
  @Test
  void load_fileWithNoValidEntries_throwsPersistenceException() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "# only comments\n\n");

    assertThrows(PersistenceException.class, () -> StockLoader.load(csv.toFile()));
  }

  /** Verifies that a file containing only malformed lines throws a {@link PersistenceException}. */
  @Test
  void load_fileWithOnlyMalformedLines_throwsPersistenceException() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "NOT_ENOUGH_FIELDS\nALSO_BAD\n");

    assertThrows(PersistenceException.class, () -> StockLoader.load(csv.toFile()));
  }

  // Constant

  /** Verifies that the default stock file constant points to the expected classpath path. */
  @Test
  void defaultStockFile_constant_hasExpectedValue() {
    assertEquals("/data/StockData.csv", StockLoader.DEFAULT_STOCK_FILE);
  }

  /**
   * Invokes the private constructor via reflection to cover it for JaCoCo. The class is a static
   * utility holder and the constructor must never be used in production code.
   */
  @Test
  void privateConstructor_isInvocable_viaReflection() throws Exception {
    Constructor<StockLoader> constructor = StockLoader.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    assertNotNull(constructor.newInstance());
  }
}
