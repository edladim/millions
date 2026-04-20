package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.view.MainView;
import javafx.stage.Stage;

public class MainController {
  private final Stage primaryStage;
  private final MainView view;
  private final Player player;
  private final Exchange exchange;

  public MainController(Stage primaryStage,
                        MainView view,
                        Player player,
                        Exchange exchange) {
    this.primaryStage = primaryStage;
    this.view         = view;
    this.player       = player;
    this.exchange     = exchange;

    view.setOnAdvanceWeek(this::advanceWeek);
  }

  public void advanceWeek() {
    exchange.advance();
    view.setWeek(exchange.getWeek());
    refresh();
  }

  public void refresh() {

  }
}
