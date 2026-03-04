package edu.ntni.idi.idatt.millions;

import edu.ntni.idi.idatt.millions.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
