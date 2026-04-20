package edu.ntnu.idi.idatt.millions.view;

/**
 * <p>
 * Immutable data object carrying the display values for a single stock row
 * in the trading view.
 * </p>
 *
 * <p>
 * All string fields are pre-formatted for display (e.g. {@code "$12.34"}).
 * Use {@link ViewFormatter} to produce the formatted values before constructing
 * an instance.
 * </p>
 *
 * @param symbol     the stock ticker symbol, e.g. {@code "AAPL"}
 * @param company    the full company name
 * @param price      the formatted current price, e.g. {@code "$12.34"}
 * @param change     the formatted price change, e.g. {@code "+0.56"}
 * @param high       the formatted day-high price, e.g. {@code "$13.00"}
 * @param low        the formatted day-low price, e.g. {@code "$11.80"}
 * @param isPositive whether the price change is positive or zero
 */
public record StockRowData(
    String symbol,
    String company,
    String price,
    String change,
    String high,
    String low,
    boolean isPositive
) {}
