package edu.ntnu.idi.idatt.millions.model.portfolio;

import edu.ntnu.idi.idatt.millions.model.market.ReadOnlyStock;
import edu.ntnu.idi.idatt.millions.model.market.Stock;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Aggregated, immutable view of all {@link Share} lots that belong to the same underlying {@link
 * Stock}. A {@code Holding} sums the quantities and totals of the lots and computes the
 * quantity-weighted average purchase price, so views can render a single row per stock without
 * recomputing the aggregation on each cell access.
 *
 * @param stock the underlying stock, never null
 * @param totalQuantity the summed quantity across all lots, never negative
 * @param weightedBuyPrice the quantity-weighted average purchase price, never negative
 * @param totalInvestment the summed original investment across all lots, never negative
 */
public record Holding(
    ReadOnlyStock stock,
    BigDecimal totalQuantity,
    BigDecimal weightedBuyPrice,
    BigDecimal totalInvestment) {

  /** Scale used when dividing total investment by total quantity to derive the weighted price. */
  private static final int WEIGHTED_PRICE_SCALE = 4;

  /**
   * Builds a {@code Holding} from a non-empty list of {@link Share} lots that all reference the
   * same stock. The weighted buy price is {@code totalInvestment / totalQuantity}; if the total
   * quantity is zero, the weighted price falls back to {@link BigDecimal#ZERO} to avoid division by
   * zero.
   *
   * @param lots one or more share lots referencing the same stock
   * @return the aggregated holding
   * @throws NullPointerException if {@code lots} is null
   * @throws IllegalArgumentException if {@code lots} is empty
   */
  public static Holding aggregate(List<Share> lots) {
    Objects.requireNonNull(lots, "Lots cannot be null");
    if (lots.isEmpty()) {
      throw new IllegalArgumentException("Cannot aggregate an empty list of lots");
    }
    ReadOnlyStock stock = lots.getFirst().getStock();
    BigDecimal totalQuantity =
        lots.stream().map(Share::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalInvestment =
        lots.stream().map(Share::getTotalInvestment).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal weightedBuyPrice =
        totalQuantity.signum() == 0
            ? BigDecimal.ZERO
            : totalInvestment.divide(totalQuantity, WEIGHTED_PRICE_SCALE, RoundingMode.HALF_UP);
    return new Holding(stock, totalQuantity, weightedBuyPrice, totalInvestment);
  }

  /**
   * Returns the current market value of this holding, {@code currentPrice × totalQuantity}.
   *
   * @return the total value at current market price
   */
  public BigDecimal getTotalValue() {
    return stock.getSalesPrice().multiply(totalQuantity);
  }

  /**
   * Returns the unrealised gain or loss for this holding, {@code totalValue − totalInvestment}.
   *
   * @return the unrealised gain (positive) or loss (negative)
   */
  public BigDecimal getGainOrLoss() {
    return getTotalValue().subtract(totalInvestment);
  }
}
