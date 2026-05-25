package edu.ntnu.idi.idatt.millions.persistence.stock;

import edu.ntnu.idi.idatt.millions.model.market.Stock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvStockWriter}.
 */
class CsvStockWriterTest {

  @TempDir
  Path tempDir;

  @Test
  void writeStockData_createsFileIfAbsent() throws Exception {
    Path csv = tempDir.resolve("output.csv");
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00"));

    new CsvStockWriter(csv).writeStockData(stock);

    assertTrue(Files.exists(csv));
  }

  @Test
  void writeStockData_writesCorrectFormat() throws Exception {
    Path csv = tempDir.resolve("output.csv");
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00"));

    new CsvStockWriter(csv).writeStockData(stock);

    String content = Files.readString(csv).trim();
    assertEquals("AAPL,Apple Inc.,150.00", content);
  }

  @Test
  void writeStockData_appendsMultipleStocks() throws Exception {
    Path csv = tempDir.resolve("output.csv");
    Stock apple = new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00"));
    Stock microsoft = new Stock("MSFT", "Microsoft", new BigDecimal("300.00"));
    CsvStockWriter writer = new CsvStockWriter(csv);

    writer.writeStockData(apple);
    writer.writeStockData(microsoft);

    List<String> lines = Files.readAllLines(csv);
    assertEquals(2, lines.size());
    assertTrue(lines.get(0).startsWith("AAPL"));
    assertTrue(lines.get(1).startsWith("MSFT"));
  }

  @Test
  void writeStockData_usesSalesPrice() throws Exception {
    Path csv = tempDir.resolve("output.csv");
    Stock stock = new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00"));
    stock.addNewSalesPrice(new BigDecimal("175.50"));

    new CsvStockWriter(csv).writeStockData(stock);

    String content = Files.readString(csv).trim();
    assertTrue(content.endsWith("175.50"));
  }

  @Test
  void writeStockData_nullStock_throwsNullPointerException() {
    Path csv = tempDir.resolve("output.csv");
    CsvStockWriter writer = new CsvStockWriter(csv);

    assertThrows(NullPointerException.class, () -> writer.writeStockData(null));
  }

  @Test
  void constructor_nullPath_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new CsvStockWriter(null));
  }
}