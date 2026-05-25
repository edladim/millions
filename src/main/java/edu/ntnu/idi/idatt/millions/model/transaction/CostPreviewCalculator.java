package edu.ntnu.idi.idatt.millions.model.transaction;

import java.math.BigDecimal;

/**
 * Computes the gross, commission, and total cost of a prospective transaction before it is
 * executed.
 *
 * <p>The commission is a flat percentage of the gross, computed at full {@link BigDecimal}
 * precision so the affordability check is exact. For buy orders the commission is added to the
 * gross; for sell orders it is subtracted, since commissions reduce the player's proceeds in both
 * cases.
 */
public final class CostPreviewCalculator {

  /** Flat commission rate applied to the gross amount of every order. */
  public static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005");

  private final BigDecimal gross;
  private final BigDecimal commission;
  private final BigDecimal total;

  /**
   * Computes the cost breakdown for the given price, quantity, and order direction.
   *
   * @param price the unit price of the asset
   * @param quantity the share quantity to trade
   * @param isBuy {@code true} for buy orders (commission added), {@code false} for sell orders
   *     (commission subtracted)
   */
  public CostPreviewCalculator(BigDecimal price, BigDecimal quantity, boolean isBuy) {
    this.gross = price.multiply(quantity);
    this.commission = gross.multiply(COMMISSION_RATE);
    this.total = isBuy ? gross.add(commission) : gross.subtract(commission);
  }

  /**
   * Returns the gross amount: {@code price × quantity}, before commission.
   *
   * @return the gross amount
   */
  public BigDecimal getGross() {
    return gross;
  }

  /**
   * Returns the commission charged on the gross amount.
   *
   * @return the commission amount
   */
  public BigDecimal getCommission() {
    return commission;
  }

  /**
   * Returns the total cost of the order: gross ± commission depending on order direction.
   *
   * @return the total amount
   */
  public BigDecimal getTotal() {
    return total;
  }
}
