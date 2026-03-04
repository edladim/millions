package edu.ntni.idi.idatt.millions.model.transaction;

import edu.ntni.idi.idatt.millions.model.Share;
import edu.ntni.idi.idatt.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionArchiveTest {

  private TransactionArchive archive;
  private Stock stock;
  private Share share;

  @BeforeEach
  void setup() {
    archive = new TransactionArchive();
    stock = new Stock("AAPL", "Apple Inc.", BigDecimal.valueOf(150));
    share = new Share(stock, BigDecimal.valueOf(100), BigDecimal.valueOf(10));
  }

  @Test
  void testIsEmptyInitially() {
    assertTrue(archive.isEmpty());
  }

  @Test
  void testAddTransaction() {
    Purchase purchase = new Purchase(share, 1);
    assertTrue(archive.addTransaction(purchase));
    assertFalse(archive.isEmpty());
  }

  @Test
  void testGetTransactionByWeek() {
    Purchase purchase = new Purchase(share, 1);
    Purchase purchase1 = new Purchase(share, 1);
    Purchase purchase2 = new Purchase(share, 2);
    Sale sale1 = new Sale(share, 1);

    archive.addTransaction(purchase1);
    archive.addTransaction(purchase2);
    archive.addTransaction(sale1);

    List<Transaction> week1 = archive.getTransactions(1);
    assertEquals(2, week1.size());
  }

  @Test
  void testGetPurchaseByWeek() {
    Purchase purchase1 = new Purchase(share, 1);
    Sale sale1 = new Sale(share, 1);

    archive.addTransaction(purchase1);
    archive.addTransaction(sale1);

    List<Transaction> purchases = archive.getPurchases(1);
    assertEquals(1, purchases.size());
    assertInstanceOf(Purchase.class, purchases.get(0));
  }

  @Test
  void testGetSalesByWeek() {
    Purchase purchase1 = new Purchase(share, 1);
    Sale sale1 = new Sale(share, 1);

    archive.addTransaction(purchase1);
    archive.addTransaction(sale1);

    List<Transaction> sales = archive.getSales(1);
    assertEquals(1, sales.size());
    assertInstanceOf(Sale.class, sales.get(0));
  }

  @Test
  void testCountDistinctWeeks() {
    Purchase purchase1 = new Purchase(share, 1);
    Purchase purchase2 = new Purchase(share, 1);
    Sale sale1 = new Sale(share, 2);
    Sale sale2 = new Sale(share, 3);

    archive.addTransaction(purchase1);
    archive.addTransaction(purchase2);
    archive.addTransaction(sale1);
    archive.addTransaction(sale2);

    assertEquals(3, archive.countDistictWeeks());
  }
}
