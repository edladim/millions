package edu.ntnu.idi.idatt.millions.view.components;

import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

public class StockChartComponent extends VBox {

  private final Label titleLabel;
  private final Label subtitleLabel;
  private final LineChart<Number, Number> lineChart;
  private final NumberAxis xAxis;
  private final NumberAxis yAxis;

  public StockChartComponent(String symbol, String company) {
    getStyleClass().add("stat-card");
    setPadding(new Insets(5,20,5,20));
    setSpacing(5);

    titleLabel = new Label(symbol);
    titleLabel.getStyleClass().add("chart-title");

    subtitleLabel = new Label(company);
    subtitleLabel.getStyleClass().add("chart-subtitle");

    xAxis = new NumberAxis();
    xAxis.setLabel("Week");
    xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis) {
      @Override
      public String toString(Number value) {
        return "W" + value.intValue();
      }
    });
    xAxis.getStyleClass().add("chart-axis");

    yAxis = new NumberAxis();
    yAxis.setLabel("Price ($)");
    yAxis.getStyleClass().add("chart-axis");
    yAxis.setForceZeroInRange(false);

    lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setCreateSymbols(false);
    lineChart.setLegendVisible(false);
    lineChart.setAnimated(false);
    lineChart.getStyleClass().add("stock-line-chart");
    lineChart.setPrefHeight(220);
    lineChart.setMaxWidth(Double.MAX_VALUE);

    getChildren().addAll(titleLabel, subtitleLabel, lineChart);
  }

  public void setStockInfo(String symbol, String company) {
    titleLabel.setText(symbol);
    subtitleLabel.setText(company);
  }

  public void setData(List<BigDecimal> prices) {
    lineChart.getData().clear();

    if (prices == null || prices.isEmpty()) return;

    XYChart.Series<Number, Number> series = new XYChart.Series<>();

    for (int i = 0; i < prices.size(); i++) {
      series.getData().add(
              new XYChart.Data<>(i + 1, prices.get(i).doubleValue())
      );
    }

    lineChart.getData().add(series);

    boolean isPositive = prices.size() > 1 &&
            prices.get(prices.size() - 1)
                    .compareTo(prices.get(0)) >= 0;

    lineChart.getStyleClass().removeAll("chart-positive", "chart-negative");
    lineChart.getStyleClass().add(isPositive ? "chart-positive" : "chart-negative");
  }

  public void clear() {
    lineChart.getData().clear();
    titleLabel.setText("");
    subtitleLabel.setText("");
  }

  public void setChartHeight(double height) {
    lineChart.setPrefHeight(height);
  }

  public void setXAxisLabel(String label) { xAxis.setLabel(label); }
  public void setYAxisLabel(String label) { yAxis.setLabel(label); }
}
