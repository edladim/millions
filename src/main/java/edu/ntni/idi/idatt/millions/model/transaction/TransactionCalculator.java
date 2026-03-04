package edu.ntni.idi.idatt.millions.model.transaction;

import java.math.BigDecimal;

public interface TransactionCalculator {
  BigDecimal calculateGross();
  BigDecimal calculateComission();
  BigDecimal calculateTax();
  BigDecimal calculateTotal();
}
