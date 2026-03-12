package edu.ntni.idi.idatt.millions.fileHandler;

import edu.ntni.idi.idatt.millions.model.Stock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteCSV {

  public void writeStockData(Stock stock) {
    String fileName = "src/main/resources/StockData.csv";
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
