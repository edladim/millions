package edu.ntni.idi.idatt.millions;

import edu.ntni.idi.idatt.millions.model.Player;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;

public class PlayerTest {

  private Player player;
  private BigDecimal startingMoney;

  @BeforeEach
  void setup() {
    startingMoney = BigDecimal.valueOf(10000);
    player = new Player("Test Player", startingMoney);
  }


}
