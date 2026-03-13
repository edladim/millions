package edu.ntni.idi.idatt.millions.model;

import edu.ntni.idi.idatt.millions.model.transaction.SaleCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a portfolio containing a collection of {@link Share} objects.
 *
 * <p>A portfolio manages shares owned by an investor and provides operations
 * for adding, removing, retrieving, and querying shares.</p>
 *
 * <p>The class guarantees that:</p>
 * <ul>
 *   <li>No {@code null} shares can be added</li>
 *   <li>The internal collection cannot be modified externally</li>
 * </ul>
 */
public final class Portfolio {

  private final List<Share> shares = new ArrayList<>();

  /**
   * Constructs an empty portfolio.
   */
  public Portfolio() {
  }

  /**
   * Constructs a portfolio initialized with a list of shares.
   *
   * <p>Each share is validated and added using {@link #addShare(Share)}
   * to ensure consistent validation rules.</p>
   *
   * @param shares the initial shares to include in the portfolio
   * @throws NullPointerException if {@code shares} or any share in the list is null
   */
  public Portfolio(List<Share> shares) {
    Objects.requireNonNull(shares, "Shares list cannot be null");

    for (Share share : shares) {
      addShare(share);
    }
  }

  /**
   * Adds a share to the portfolio.
   *
   * @param share the share to add, cannot be null
   * @return {@code true} if the share was added successfully,
   *         {@code false} if the share already exists in the portfolio
   * @throws NullPointerException if {@code share} is null
   */
  public boolean addShare(Share share) {
    Objects.requireNonNull(share, "Share cannot be null");
    return shares.add(share);
  }

  /**
   * Removes a share from the portfolio.
   *
   * @param share the share to remove, cannot be null
   * @return {@code true} if the share existed and was removed,
   *         {@code false} otherwise
   * @throws NullPointerException if {@code share} is null
   */
  public boolean removeShare(Share share) {
    Objects.requireNonNull(share, "Share cannot be null");
    return shares.remove(share);
  }

  /**
   * Returns all shares currently stored in the portfolio.
   *
   * <p>The returned list is an unmodifiable copy to prevent
   * external modification of the internal state.</p>
   *
   * @return an unmodifiable list of shares, never null
   */
  public List<Share> getShares() {
    return List.copyOf(shares);
  }

  /**
   * Returns all shares associated with a specific stock symbol.
   *
   * @param symbol the ticker symbol to search for, cannot be null or blank
   * @return an unmodifiable list of matching shares, possibly empty but never null
   * @throws NullPointerException if {@code symbol} is null
   * @throws IllegalArgumentException if {@code symbol} is blank
   */
  public List<Share> getShares(String symbol) {
    final String validatedSymbol = validateSymbol(symbol);

    return shares.stream()
        .filter(share -> share.getStock().getSymbol().equals(validatedSymbol))
        .toList();
  }

  /**
   * Checks whether a given share exists in the portfolio.
   *
   * @param share the share to check for, cannot be null
   * @return {@code true} if the portfolio contains the share
   * @throws NullPointerException if {@code share} is null
   */
  public boolean contains(Share share) {
    Objects.requireNonNull(share, "Share cannot be null");
    return shares.contains(share);
  }

  /**
   * Returns the number of shares currently stored in the portfolio.
   *
   * @return the number of shares
   */
  public int size() {
    return shares.size();
  }

  /**
   * Calculates the current total market value of the portfolio.
   *
   * <p>The value is calculated as the sum of the current value of each
   * {@link Share} in the portfolio.</p>
   *
   * @return the total market value of the portfolio, never null
   */
  public BigDecimal getTotalValue() {
    return shares.stream()
        .map(Share::getCurrentValue)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Calculates the total amount originally invested in the portfolio.
   *
   * @return the total invested capital, never null
   */
  public BigDecimal getTotalInvestment() {
    return shares.stream()
        .map(Share::getTotalInvestment)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Calculates the total unrealized gain or loss of the portfolio.
   *
   * <p>This is the difference between the current market value and the
   * original invested capital.</p>
   *
   * @return the total gain or loss, never null
   */
  public BigDecimal getTotalGainOrLoss() {
    return getTotalValue().subtract(getTotalInvestment());
  }

  /**
   * Validates that a stock symbol is non-null and non-blank.
   *
   * @param symbol the symbol to validate
   * @return the trimmed symbol
   * @throws NullPointerException if {@code symbol} is null
   * @throws IllegalArgumentException if the symbol is blank
   */
  private static String validateSymbol(String symbol) {
    symbol = Objects.requireNonNull(symbol, "Symbol cannot be null").trim();

    if (symbol.isEmpty()) {
      throw new IllegalArgumentException("Symbol cannot be blank");
    }

    return symbol;
  }

  /**
   * Calculates the net worth from selling all shares in the portfolio.
   *
   * <p>The net worth is calculated by determining what amount would be
   * received after selling each share, accounting for broker commissions
   * and taxes on profit.</p>
   *
   * @return the total net worth after all deductions, never null
   */
  public BigDecimal getNetWorth() {
    return shares.stream()
            .map(SaleCalculator::new)
            .map(SaleCalculator::calculateTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

}