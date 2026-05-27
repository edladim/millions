package edu.ntnu.idi.idatt.millions.model.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stores completed financial transactions.
 *
 * <p>The transaction archive keeps track of all executed transactions in the system. Transactions
 * may represent purchases or sales and are associated with a specific week.
 *
 * <p>The archive allows querying transactions by week and transaction type, as well as determining
 * how many weeks contain trading activity.
 */
public final class TransactionArchive {

  private final List<Transaction> transactions = new ArrayList<>();

  /** Creates an empty transaction archive. */
  public TransactionArchive() {}

  /**
   * Adds a transaction to the archive.
   *
   * @param transaction the transaction to add
   * @return {@code true} if the transaction was added
   * @throws NullPointerException if {@code transaction} is null
   */
  public boolean add(Transaction transaction) {
    Objects.requireNonNull(transaction, "Transaction cannot be null");

    return transactions.add(transaction);
  }

  /**
   * Checks whether the archive contains no transactions.
   *
   * @return {@code true} if the archive is empty
   */
  public boolean isEmpty() {
    return transactions.isEmpty();
  }

  /**
   * Returns all transactions in the archive, in insertion order.
   *
   * @return an unmodifiable list of all transactions, never {@code null}
   */
  public List<Transaction> getAll() {
    return List.copyOf(transactions);
  }

  /**
   * Returns all transactions performed in a given week.
   *
   * @param week the week number
   * @return a list of transactions from that week
   * @throws IllegalArgumentException if {@code week} is negative
   */
  public List<Transaction> getTransactions(int week) {

    if (week < 0) {
      throw new IllegalArgumentException("Week cannot be negative");
    }

    return transactions.stream().filter(t -> t.getWeek() == week).toList();
  }

  /**
   * Returns all purchase transactions performed in a given week.
   *
   * @param week the week number
   * @return a list of purchase transactions
   * @throws IllegalArgumentException if {@code week} is negative
   */
  public List<Purchase> getPurchases(int week) {

    if (week < 0) {
      throw new IllegalArgumentException("Week cannot be negative");
    }

    return transactions.stream()
        .filter(t -> t.getWeek() == week)
        .filter(t -> t instanceof Purchase)
        .map(t -> (Purchase) t)
        .toList();
  }

  /**
   * Returns all sale transactions performed in a given week.
   *
   * @param week the week number
   * @return a list of sale transactions
   * @throws IllegalArgumentException if {@code week} is negative
   */
  public List<Sale> getSales(int week) {

    if (week < 0) {
      throw new IllegalArgumentException("Week cannot be negative");
    }

    return transactions.stream()
        .filter(t -> t.getWeek() == week)
        .filter(t -> t instanceof Sale)
        .map(t -> (Sale) t)
        .toList();
  }

  /**
   * Counts the number of distinct weeks with trading activity.
   *
   * <p>A week is counted if at least one transaction occurred in that week.
   *
   * @return number of distinct weeks containing transactions
   */
  public int countDistinctWeeks() {

    return (int) transactions.stream().map(Transaction::getWeek).distinct().count();
  }
}
