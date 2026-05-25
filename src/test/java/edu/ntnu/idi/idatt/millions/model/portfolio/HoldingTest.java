package edu.ntnu.idi.idatt.millions.model.portfolio;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt.millions.model.market.Stock;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class HoldingTest {

  @Test
  void aggregate_nullList_throwsException() {
    assertThrows(NullPointerException.class, () -> Holding.aggregate(null));
  }

  @Test
  void aggregate_emptyList_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> Holding.aggregate(List.of()));
  }

  @Test
  void aggregate_zeroTotalQuantity_returnsZeroWeightedPrice() throws Exception {
    Stock stock = new Stock("AAPL", "Apple", new BigDecimal("200"));
    Share share = new Share(stock, BigDecimal.ONE, new BigDecimal("150"));

    Field quantityField = Share.class.getDeclaredField("quantity");
    quantityField.setAccessible(true);
    quantityField.set(share, BigDecimal.ZERO);

    Holding holding = Holding.aggregate(List.of(share));

    assertEquals(BigDecimal.ZERO, holding.weightedBuyPrice());
    assertEquals(BigDecimal.ZERO, holding.totalQuantity());
    assertEquals(BigDecimal.ZERO, holding.totalInvestment());
  }

  @Test
  void getTotalValue_and_getGainOrLoss_calculateCorrectly() {
    Stock stock = new Stock("AAPL", "Apple", new BigDecimal("200"));
    Share share1 = new Share(stock, new BigDecimal("2"), new BigDecimal("150"));
    Share share2 = new Share(stock, new BigDecimal("1"), new BigDecimal("100"));

    Holding holding = Holding.aggregate(List.of(share1, share2));

    assertEquals(new BigDecimal("600"), holding.getTotalValue());
    assertEquals(new BigDecimal("200"), holding.getGainOrLoss());
  }
}
