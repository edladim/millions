package edu.ntni.idi.idatt.millions;

import edu.ntni.idi.idatt.millions.transaction.Transaction;

import java.util.List;

public class TransactionArchive {
  private List<Transaction> transactions;

  public TransactionArchive() {

  }

  public boolean addTransaction(Transaction transaction) {
    return transactions.add(transaction);
  }

  public boolean isEmpty() {
    return transactions.isEmpty();
  }

  public List<Transaction> getTransactions(int week) {
    return transactions.stream().filter(transaction -> transaction.getWeek() == week).toList();
  }
/*
  public List<Transaction> getPurchases(int week) {

  }

  public List<Transaction> getSales(int week) {

  }

  public int countDistictWeeks() {

  }

 */
}
