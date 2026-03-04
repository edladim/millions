package edu.ntni.idi.idatt.millions.calculator;

import edu.ntni.idi.idatt.millions.Share;
import edu.ntni.idi.idatt.millions.Stock;
import edu.ntni.idi.idatt.millions.calculator.PurchaseCalculator;
import edu.ntni.idi.idatt.millions.calculator.SaleCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class PurchaseCalculatorTest {

  private PurchaseCalculator purchaseCalculator;
  private Share share;

  @BeforeEach
  void setup() {
    Stock stock = new Stock("AAPL", "Apple Inc.", BigDecimal.valueOf(150));
    share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(100));
    purchaseCalculator = new PurchaseCalculator(share);
  }



}
