package edu.ntni.idi.idatt.millions.fileHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;

public class ReadCSV {

  public static void readFile(String filePath) {
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
      String line;
      String delimiter = ",";
      while ((line = bufferedReader.readLine()) != null) {
        String[] fields = line.split(delimiter);
        for (String field : fields) {

        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
