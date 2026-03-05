package edu.ntni.idi.idatt.millions.model.transaction;

import java.math.BigDecimal;

/**
 * Defines calculation methods for financial transactions.
 *
 * <p>Implementations compute the financial values associated with
 * buying or selling shares.</p>
 */
public interface TransactionCalculator {

  /**
   * Calculates the gross value of the transaction before fees and taxes.
   *
   * @return the gross value
   */
  BigDecimal calculateGross();

  /**
   * Calculates the commission fee paid to the broker.
   *
   * @return the commission fee
   */
  BigDecimal calculateCommission();

  /**
   * Calculates the tax associated with the transaction.
   *
   * @return the tax amount
   */
  BigDecimal calculateTax();

  /**
   * Calculates the final total value of the transaction.
   *
   * @return the total value
   */
  BigDecimal calculateTotal();
}
