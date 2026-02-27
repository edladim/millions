package edu.ntni.idi.idatt.millions;

import edu.ntni.idi.idatt.millions.transaction.Purchase;
import edu.ntni.idi.idatt.millions.transaction.Sale;
import edu.ntni.idi.idatt.millions.transaction.Transaction;

import java.util.List;

public class TransactionArchive {
  private List<Transaction> transactions;

  public TransactionArchive() {

  }

  public boolean addTransaction(Transaction transaction) {
    transactions.add(transaction);
    return true;
  }

  public boolean isEmpty() {
    return transactions.isEmpty();
  }

  public List<Transaction> getTransactions(int week) {
    return transactions.stream()
            .filter(transaction -> transaction.getWeek() == week)
            .toList();
  }

  public List<Transaction> getPurchases(int week) {
    return transactions.stream()
            .filter(transaction -> transaction.getWeek() == week && transaction instanceof Purchase)
            .toList();
  }

  public List<Transaction> getSales(int week) {
    return transactions.stream()
            .filter(transaction -> transaction.getWeek() == week && transaction instanceof Sale)
            .toList();
  }

  public int countDistictWeeks() {
    return (int) transactions.stream() //Caster int siden count returnerer long
            .map(Transaction::getWeek)
            .distinct()
            .count();
  }
}
