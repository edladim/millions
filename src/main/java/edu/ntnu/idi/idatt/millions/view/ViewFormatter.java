package edu.ntnu.idi.idatt.millions.view;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <p>
 * Utility class for formatting {@link BigDecimal} model values into display strings
 * suitable for use in view classes.
 * </p>
 *
 * <p>
 * All methods scale values to two decimal places using
 * {@link RoundingMode#HALF_UP} before formatting. The class cannot be
 * instantiated; use the static methods directly.
 * </p>
 */
public final class ViewFormatter {

  private ViewFormatter() {}

  /**
   * <p>Formats a value as a dollar price, e.g. {@code "$12.34"}.</p>
   *
   * @param value the monetary value to format
   * @return the formatted price string
   */
  public static String price(BigDecimal value) {
    return "$" + scale(value).toPlainString();
  }

  /**
   * <p>Formats a value as a signed dollar price, e.g. {@code "$+12.34"} or
   * {@code "$-12.34"}.</p>
   *
   * @param value the monetary value to format
   * @return the formatted signed price string
   */
  public static String signedPrice(BigDecimal value) {
    BigDecimal scaled = scale(value);
    return (scaled.signum() >= 0 ? "+$" : "-$") + scaled.abs().toPlainString();
  }

  /**
   * <p>Formats a value as a signed amount without a currency symbol,
   * e.g. {@code "+12.34"} or {@code "-12.34"}.</p>
   *
   * @param value the value to format
   * @return the formatted signed amount string
   */
  public static String signedAmount(BigDecimal value) {
    return (value.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + scale(value).toPlainString();
  }

  /**
   * <p>Formats a value as a signed percentage, e.g. {@code "+12.34%"} or
   * {@code "-12.34%"}.</p>
   *
   * @param value the percentage value to format (e.g. {@code 12.34} for 12.34%)
   * @return the formatted percentage string
   */
  public static String percent(BigDecimal value) {
    return signedAmount(value) + "%";
  }

  /**
   * <p>Formats a value as a directional percentage change with an arrow indicator,
   * e.g. {@code "↗ +12.34%"} or {@code "↘ -12.34%"}.</p>
   *
   * @param value the percentage change value to format
   * @return the formatted directional change string
   */
  public static String changeArrow(BigDecimal value) {
    boolean positive = value.compareTo(BigDecimal.ZERO) >= 0;
    return (positive ? "↗ +" : "↘ ") + scale(value).toPlainString() + "%";
  }

  /**
   * <p>Formats a value as a directional price change with an arrow indicator but
   * without a percentage sign, e.g. {@code "↗ +12.34"} or {@code "↘ -12.34"}.</p>
   *
   * @param value the price change value to format
   * @return the formatted directional price change string
   */
  public static String priceChangeArrow(BigDecimal value) {
    boolean positive = value.compareTo(BigDecimal.ZERO) >= 0;
    return (positive ? "↗ " : "↘ ") + signedAmount(value);
  }

  /**
   * <p>Computes the percentage change from {@code currentPrice} and formats it
   * with a directional arrow, e.g. {@code "↗ +5.42%"} or {@code "↘ -3.17%"}.</p>
   *
   * <p>Uses {@code change / (currentPrice - change) × 100} to derive the rate
   * from the absolute delta and the current price.</p>
   *
   * @param change       the absolute price change (latest minus previous)
   * @param currentPrice the current (latest) price
   * @return the formatted percentage-change string with directional arrow
   */
  public static String changeArrowPercent(BigDecimal change, BigDecimal currentPrice) {
    BigDecimal prev = currentPrice.subtract(change);
    BigDecimal pct = prev.signum() == 0 ? BigDecimal.ZERO
        : change.divide(prev, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    return changeArrow(pct);
  }

  /**
   * <p>Converts a decimal return rate to a signed percentage string,
   * e.g. {@code 0.20} becomes {@code "+20.00%"}.</p>
   *
   * @param rate the decimal rate (e.g. {@code 0.20} for 20%)
   * @return the formatted percentage string
   */
  public static String rateAsPercent(BigDecimal rate) {
    return percent(rate.multiply(new BigDecimal("100")));
  }

  /**
   * <p>Formats a share quantity as a plain decimal string scaled to two
   * decimal places, e.g. {@code "10.00"}.</p>
   *
   * @param quantity the share quantity to format
   * @return the formatted quantity string
   */
  public static String quantity(BigDecimal quantity) {
    return scale(quantity).toPlainString();
  }

  /**
   * <p>Formats a value as a whole-dollar price with no decimals,
   * e.g. {@code "$1234"}.</p>
   *
   * @param value the monetary value to format
   * @return the formatted whole-dollar string
   */
  public static String wholePrice(BigDecimal value) {
    return "$" + value.setScale(0, RoundingMode.HALF_UP).toPlainString();
  }

  /**
   * <p>Formats a value as a signed whole-dollar price with no decimals,
   * e.g. {@code "+$1234"} or {@code "-$1234"}. Used as a compact alternative
   * to {@link #signedPrice(BigDecimal)} when display width is tight.</p>
   *
   * @param value the monetary value to format
   * @return the formatted signed whole-dollar string
   */
  public static String signedWholePrice(BigDecimal value) {
    BigDecimal rounded = value.setScale(0, RoundingMode.HALF_UP);
    return (rounded.signum() >= 0 ? "+$" : "-$") + rounded.abs().toPlainString();
  }

  /**
   * <p>Formats a game week as a plain label, e.g. {@code "Week 53"}.</p>
   *
   * @param week the total game week number (1-based)
   * @return the formatted week string
   */
  public static String week(int week) {
    return "Week " + week;
  }

  private static BigDecimal scale(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP);
  }
}
