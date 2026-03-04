package edu.ntni.idi.idatt.millions;

import edu.ntni.idi.idatt.millions.model.Player;
import edu.ntni.idi.idatt.millions.model.Portfolio;
import edu.ntni.idi.idatt.millions.model.transaction.TransactionArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlayerTest {

  private Player player;
  private BigDecimal startingMoney;

  @BeforeEach
  void setup() {
    startingMoney = BigDecimal.valueOf(10000);
    player = new Player("Test Player", startingMoney);
  }

  @Test
  void testGetName() {
    assertEquals("Test Player", player.getName());
  }

  @Test
  void testGetMoneyInitial() {
    assertEquals(0, startingMoney.compareTo(player.getMoney()));
  }

  @Test
  void testAddMoney() {
    player.addMoney(BigDecimal.valueOf(500));
    assertEquals(0, BigDecimal.valueOf(10500).compareTo(player.getMoney()));
  }

  @Test
  void testWithdrawMoney() {
    player.withdrawMoney(BigDecimal.valueOf(2000));
    assertEquals(0, BigDecimal.valueOf(8000).compareTo(player.getMoney()));
  }

  @Test
  void testGetPortfolio() {
    assertNotNull(player.getPortfolio());
    assertInstanceOf(Portfolio.class, player.getPortfolio());
  }

  @Test
  void testGetTransactionArchive() {
    assertNotNull(player.getTransactionArchive());
    assertInstanceOf(TransactionArchive.class, player.getTransactionArchive());
  }

}
