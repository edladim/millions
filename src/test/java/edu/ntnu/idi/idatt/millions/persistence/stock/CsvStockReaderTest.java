package edu.ntnu.idi.idatt.millions.persistence.stock;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt.millions.model.market.Stock;
import edu.ntnu.idi.idatt.millions.persistence.PersistenceException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link CsvStockReader}.
 *
 * <p>Tests focus on CSV parsing correctness. A temporary directory is used to avoid coupling the
 * tests to any specific file on the developer's machine.
 */
class CsvStockReaderTest {

  @TempDir Path tempDir;

  // Positive tests

  @Test
  void readStockData_validLines_returnsAllStocks() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "AAPL,Apple Inc.,150.00\nMSFT,Microsoft,300.00\n");

    List<Stock> stocks = new CsvStockReader(csv).readStockData();

    assertEquals(2, stocks.size());
  }

  @Test
  void readStockData_commentsAndBlanks_areIgnored() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "# comment\n\nAAPL,Apple Inc.,150.00\n");

    List<Stock> stocks = new CsvStockReader(csv).readStockData();

    assertEquals(1, stocks.size());
  }

  @Test
  void readStockData_parsesSymbolCorrectly() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "NVDA,Nvidia,191.27\n");

    Stock stock = new CsvStockReader(csv).readStockData().getFirst();

    assertEquals("NVDA", stock.getSymbol());
  }

  @Test
  void readStockData_parsesCompanyCorrectly() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "NVDA,Nvidia Corporation,191.27\n");

    Stock stock = new CsvStockReader(csv).readStockData().getFirst();

    assertEquals("Nvidia Corporation", stock.getCompany());
  }

  @Test
  void readStockData_parsesPriceCorrectly() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "AAPL,Apple Inc.,276.43\n");

    Stock stock = new CsvStockReader(csv).readStockData().getFirst();

    assertEquals(0, new BigDecimal("276.43").compareTo(stock.getSalesPrice()));
  }

  @Test
  void readStockData_trimsWhitespace() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, " AAPL , Apple Inc. , 150.00 \n");

    Stock stock = new CsvStockReader(csv).readStockData().getFirst();

    assertEquals("AAPL", stock.getSymbol());
    assertEquals("Apple Inc.", stock.getCompany());
  }

  // Malformed lines are skipped

  @Test
  void readStockData_tooFewFields_skipsLine() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "AAPL,Apple Inc.\nMSFT,Microsoft,300.00\n");

    List<Stock> stocks = new CsvStockReader(csv).readStockData();

    assertEquals(1, stocks.size());
    assertEquals("MSFT", stocks.getFirst().getSymbol());
  }

  @Test
  void readStockData_invalidPrice_skipsLine() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "AAPL,Apple Inc.,not-a-number\nMSFT,Microsoft,300.00\n");

    List<Stock> stocks = new CsvStockReader(csv).readStockData();

    assertEquals(1, stocks.size());
  }

  @Test
  void readStockData_negativePice_skipsLine() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "AAPL,Apple Inc.,-50.00\nMSFT,Microsoft,300.00\n");

    List<Stock> stocks = new CsvStockReader(csv).readStockData();

    assertEquals(1, stocks.size());
  }

  @Test
  void readStockData_zeroPice_skipsLine() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "AAPL,Apple Inc.,0\nMSFT,Microsoft,300.00\n");

    List<Stock> stocks = new CsvStockReader(csv).readStockData();

    assertEquals(1, stocks.size());
  }

  @Test
  void readStockData_blankSymbol_skipsLine() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, " ,Apple Inc.,150.00\nMSFT,Microsoft,300.00\n");

    List<Stock> stocks = new CsvStockReader(csv).readStockData();

    assertEquals(1, stocks.size());
  }

  // Exception cases

  @Test
  void readStockData_noValidStocks_throwsException() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "# only comments\n\n");

    assertThrows(PersistenceException.class, () -> new CsvStockReader(csv).readStockData());
  }

  @Test
  void constructor_nonExistentFile_throwsPersistenceException() {
    Path missing = tempDir.resolve("does_not_exist.csv");

    assertThrows(PersistenceException.class, () -> new CsvStockReader(missing));
  }

  @Test
  void constructor_nullPath_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new CsvStockReader(null));
  }

  // Classpath factory

  @Test
  void fromClasspath_validResource_readsStocks() throws Exception {
    List<Stock> stocks = CsvStockReader.fromClasspath("/data/StockData.csv").readStockData();

    assertFalse(stocks.isEmpty());
  }

  @Test
  void fromClasspath_missingResource_throwsPersistenceException() {
    assertThrows(
        PersistenceException.class, () -> CsvStockReader.fromClasspath("/data/does_not_exist.csv"));
  }

  @Test
  void fromClasspath_nullResource_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> CsvStockReader.fromClasspath(null));
  }

  // Additional malformed-line branches

  @Test
  void readStockData_blankCompany_skipsLine() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "AAPL, ,150.00\nMSFT,Microsoft,300.00\n");

    List<Stock> stocks = new CsvStockReader(csv).readStockData();

    assertEquals(1, stocks.size());
    assertEquals("MSFT", stocks.getFirst().getSymbol());
  }

  /**
   * Covers the {@code IOException} catch block in {@code readStockData()} by feeding the reader an
   * {@link InputStream} that throws on read. The private (InputStream, String) constructor is
   * accessed via reflection.
   */
  @Test
  void readStockData_ioErrorOnRead_throwsPersistenceException() throws Exception {
    InputStream throwing =
        new InputStream() {
          @Override
          public int read() throws IOException {
            throw new IOException("simulated I/O failure");
          }
        };

    Constructor<CsvStockReader> ctor =
        CsvStockReader.class.getDeclaredConstructor(InputStream.class, String.class);
    ctor.setAccessible(true);
    CsvStockReader reader = ctor.newInstance(throwing, "test-source");

    assertThrows(PersistenceException.class, reader::readStockData);
  }

  @Test
  void readStockData_duplicateSymbol_skipsLine() throws Exception {
    Path csv = tempDir.resolve("stocks.csv");
    Files.writeString(csv, "AAPL,Apple Inc.,150.00\nAAPL,Apple Duplicate,200.00\n");

    List<Stock> stocks = new CsvStockReader(csv).readStockData();

    assertEquals(1, stocks.size());
    assertEquals(0, new BigDecimal("150.00").compareTo(stocks.getFirst().getSalesPrice()));
  }
}
