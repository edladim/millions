package calculator;

import edu.ntni.idi.idatt.millions.Share;
import edu.ntni.idi.idatt.millions.Stock;
import edu.ntni.idi.idatt.millions.calculator.PurchaseCalculator;
import edu.ntni.idi.idatt.millions.calculator.SaleCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class PurchaseCalculatorTest {
  @BeforeEach
  public void setup() {
    List<BigDecimal> prices = List.of(BigDecimal.valueOf(263), BigDecimal.valueOf(253), BigDecimal.valueOf(273));
    Stock stock = new Stock("AAPL", "Apple", prices);
    Share share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(263));

    PurchaseCalculator purchaseCalculator = new PurchaseCalculator(share);
    SaleCalculator saleCalculator = new SaleCalculator(share);
  }

  @Test
  public void calculateGrossTest() {

  }
}
