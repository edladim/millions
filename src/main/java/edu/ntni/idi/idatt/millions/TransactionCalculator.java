package edu.ntni.idi.idatt.millions;

import java.math.BigDecimal;

public interface TransactionCalculator {
  BigDecimal calucaleGross();
  BigDecimal calucaleComission();
  BigDecimal calculateTax();
  BigDecimal calculateTotal();
}
