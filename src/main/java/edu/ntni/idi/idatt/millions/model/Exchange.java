package edu.ntni.idi.idatt.millions.model;

import java.util.*;
import java.util.stream.Collectors;

public class Exchange {
  private String name;
  private int week;
  private Map<String, Stock> stockMap; // symbol to be used as key
  private Random random;

  public Exchange(String name, List<Stock> stocks) {
    this.name = name;
    this.week = 1;
    this.stockMap = new HashMap<>();
    for (Stock stock : stocks) {
      this.stockMap.put(stock.getSymbol(), stock);
    }
    this.random = new Random();
  }

  public String getName() {
    return name;
  }

  public int getWeek() {
    return week;
  }

  public boolean hasStock(String symbol) {
    return stockMap.containsKey(symbol);
  }

  public Stock getStock(String symbol) {
    return stockMap.get(symbol);
  }

  public List<Stock> findStocks(String searchTerms) {
    List<Stock> stocks = new ArrayList<>();
    String lowerCaseSearchTerms = searchTerms.toLowerCase(); // case-insensitive
    return stockMap.values().stream()
            .filter(stock -> stock.getCompany().toLowerCase().contains(lowerCaseSearchTerms) ||
                    stock.getSymbol().toLowerCase().contains(lowerCaseSearchTerms))
            .collect(Collectors.toList());
  }

  //TODO: add buy() and sell() method

  public void advance() {
    week += 1;
    // TODO: update stock prices
  }
}
