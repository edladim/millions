package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.view.MainView;
import javafx.stage.Stage;

/**
 * <p>Main controller that wires the game model to the main view.</p>
 * <p>Handles week advancement and refreshes UI data based on the current state.</p>
 */
public class MainController {
  private final Stage primaryStage;
  private final MainView view;
  private final Player player;
  private final Exchange exchange;

  /**
   * <p>Creates a main controller for the running game session.</p>
   *
   * @param primaryStage primary application window.
   * @param view main view to update.
   * @param player active player model.
   * @param exchange active exchange model.
   */
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

  /**
   * <p>Advances the exchange by one week and refreshes the UI.</p>
   */
  public void advanceWeek() {
    exchange.advance();
    view.setWeek(exchange.getWeek());
    refresh();
  }

  /**
   * <p>Refreshes view state from the current model data.</p>
   */
  public void refresh() {

  }
}
