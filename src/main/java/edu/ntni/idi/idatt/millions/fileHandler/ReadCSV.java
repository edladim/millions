package edu.ntni.idi.idatt.millions.fileHandler;

import edu.ntni.idi.idatt.millions.model.Stock;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReadCSV {
  private String stockDataFilePath = "src/main/resources/StockData.csv";

  public List<Stock> readStockData() {
    List<Stock> stocks = new ArrayList<>();

    try (InputStream is = getClass().getResourceAsStream("/StockData.csv");
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
