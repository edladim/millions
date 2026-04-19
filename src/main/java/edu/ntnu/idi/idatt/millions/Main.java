package edu.ntnu.idi.idatt.millions;

import edu.ntnu.idi.idatt.millions.fileHandler.CsvStockReader;
import edu.ntnu.idi.idatt.millions.filehandler.StockFileException;
import edu.ntnu.idi.idatt.millions.filehandler.StockReader;
import edu.ntnu.idi.idatt.millions.model.Exchange;
import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Share;
import edu.ntnu.idi.idatt.millions.model.Stock;
import edu.ntnu.idi.idatt.millions.observer.PortfolioObserverA;
import edu.ntnu.idi.idatt.millions.view.MainView;
import edu.ntnu.idi.idatt.millions.view.pages.PortfolioView;
import edu.ntnu.idi.idatt.millions.view.pages.TradingView;
import javafx.application.Application;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.nio.file.Path;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends Application {

  MainView mainView = new MainView();

  @Override
  public void start(Stage primaryStage) throws Exception {

    initialize();

    primaryStage.setTitle("Millions");
    primaryStage.setScene(mainView.createScene());
    primaryStage.setMaximized(true);
    primaryStage.show();
  }
  static void main() {
    launch();
  }

  private void initialize() throws StockFileException {
    Path path = Path.of("/Users/oskarhellum-reppen/Library/CloudStorage/OneDrive-NTNU/Prog2/Ovinger/millions/src/main/resources/StockData.csv");
    StockReader reader = new CsvStockReader(path);
    Exchange exchange = new Exchange("Exchange", reader.readStockData());
    Player player = new Player("Oskar", new BigDecimal("100000"));
    PortfolioView portfolioView = (PortfolioView) mainView.getPortfolioView();

    PortfolioObserverA observer = new PortfolioObserverA(player.getPortfolio(), portfolioView, player);
    player.getPortfolio().addObserver(observer);

    TradingView tradingView = (TradingView) mainView.getTradingView();

    tradingView.clearStocks();
    for (Stock stock : exchange.getStocks()) {
      String symbol = stock.getSymbol();
      String company = stock.getCompany();

      String price = "$" + stock.getSalesPrice().toPlainString();
      String change = stock.getLatestPriceChange().toPlainString();

      String high = "$" + stock.getHighestPrice().toPlainString();
      String low  = "$" + stock.getLowestPrice().toPlainString();

      boolean isPositive = stock.getLatestPriceChange()
              .compareTo(BigDecimal.ZERO) >= 0;

      tradingView.addStockRow(symbol, company, price, change, high, low, isPositive);
    }

    Stock apple = new Stock("AAPL", "Apple Inc.", new BigDecimal("150.00"));
    Share share = new Share(apple, new BigDecimal("2"), new BigDecimal("150.00"));
    player.getPortfolio().addShare(share);


  }
}
