package edu.ntni.idi.idatt.millions.fileHandler;

import edu.ntni.idi.idatt.millions.model.Stock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteCSV {
  private final String fileName = "src/main/resources/StockData.csv";

  public void writeStockData(Stock stock) {
    try (FileWriter fileWriter = new FileWriter(fileName, true);
         BufferedWriter writer = new BufferedWriter(fileWriter)) {
      String line = stock.getSymbol() + "," +
              stock.getCompany() + "," +
              stock.getPrices().getFirst();
      writer.write(line);
      writer.newLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
