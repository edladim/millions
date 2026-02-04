package edu.ntni.idi.idatt.millions;

import java.math.BigDecimal;
import java.util.List;

public class Stock {
  private String symbol;
  private String company;
  private List<BigDecimal> prices;

  public Stock(String symbol, String company, List<BigDecimal> prices) {
    this.symbol = symbol;
    this.company = company;
    this.prices = prices;
  }

  public String getSymbol() {
    return symbol;
  }

  public String getCompany() {
    return company;
  }

  public List<BigDecimal> getPrices() {
    return prices;
  }

  public BigDecimal getSalePrice() {
    return prices.get(0);
  }

  public void addNewSalesPrice(BigDecimal price) {
    prices.add(price);
  }
}
