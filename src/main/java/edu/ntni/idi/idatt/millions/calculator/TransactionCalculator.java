package edu.ntni.idi.idatt.millions.calculator;

import java.math.BigDecimal;

public interface TransactionCalculator {
  BigDecimal calculateGross();
  BigDecimal calculateComission();
  BigDecimal calculateTax();
  BigDecimal calculateTotal();
}
