package edu.ntnu.idi.idatt.millions.view.components;

import java.math.BigDecimal;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * UI component that renders a line chart for a stock's price history.
 *
 * <p>The component displays a title, subtitle, and a line chart that can be populated with weekly
 * price data.
 */
public class StockChartComponent extends VBox {

  private final Label titleLabel;
  private final Label subtitleLabel;
  private final LineChart<Number, Number> lineChart;
  private final NumberAxis horizontalAxis;
  private final NumberAxis verticalAxis;

  /**
   * Constructs the chart component with initial symbol and company labels.
   *
   * @param symbol the stock symbol to show in the title
   * @param company the company name to show in the subtitle
   */
  public StockChartComponent(String symbol, String company) {
    getStyleClass().add("stat-card");
    setPadding(new Insets(5, 10, 5, 10));
    setSpacing(5);

    titleLabel = new Label(symbol);
    titleLabel.getStyleClass().add("chart-title");

    subtitleLabel = new Label(company);
    subtitleLabel.getStyleClass().add("chart-subtitle");

    horizontalAxis = new NumberAxis();
    horizontalAxis.setLabel("Week");
    horizontalAxis.setTickLabelFormatter(
        new NumberAxis.DefaultFormatter(horizontalAxis) {
          @Override
          public String toString(Number value) {
            return String.valueOf(value.intValue());
          }
        });
    horizontalAxis.getStyleClass().add("chart-axis");

    verticalAxis = new NumberAxis();
    verticalAxis.setLabel("Price ($)");
    verticalAxis.getStyleClass().add("chart-axis");
    verticalAxis.setForceZeroInRange(false);

    lineChart = new LineChart<>(horizontalAxis, verticalAxis);
    lineChart.setCreateSymbols(false);
    lineChart.setLegendVisible(false);
    lineChart.setAnimated(false);
    lineChart.getStyleClass().add("stock-line-chart");
    VBox.setVgrow(lineChart, Priority.ALWAYS);
    lineChart.setMaxWidth(Double.MAX_VALUE);

    getChildren().addAll(titleLabel, subtitleLabel, lineChart);
  }

  /**
   * Updates the symbol and company labels shown above the chart.
   *
   * @param symbol the stock symbol to display
   * @param company the company name to display
   */
  public void setStockInfo(String symbol, String company) {
    titleLabel.setText(symbol);
    subtitleLabel.setText(company);
  }

  /**
   * Replaces the chart data with the provided list of prices, labelling the x-axis from 1 to {@code
   * prices.size()}. Used for charts (e.g. the portfolio value chart) whose data begins at week 1.
   *
   * @param prices list of prices ordered by week
   */
  public void setData(List<BigDecimal> prices) {
    setData(prices, prices == null ? 1 : prices.size());
  }

  /**
   * Replaces the chart data with the provided list of prices and aligns the x-axis so that the last
   * data point is labelled {@code lastWeek}.
   *
   * <p>Pre-simulated history that predates week 1 is shown with negative week numbers (e.g. W-29,
   * W-15, W0), letting players see price history before the game started.
   *
   * @param prices list of prices ordered by week, oldest first
   * @param lastWeek the game week corresponding to the last entry in {@code prices}
   */
  public void setData(List<BigDecimal> prices, int lastWeek) {
    lineChart.getData().clear();

    if (prices == null || prices.isEmpty()) {
      return;
    }

    XYChart.Series<Number, Number> series = new XYChart.Series<>();

    int n = prices.size();
    int firstWeek = lastWeek - (n - 1);
    for (int i = 0; i < n; i++) {
      int week = firstWeek + i;
      series.getData().add(new XYChart.Data<>(week, prices.get(i).doubleValue()));
    }

    horizontalAxis.setAutoRanging(false);
    horizontalAxis.setLowerBound(firstWeek - 1);
    horizontalAxis.setUpperBound(lastWeek + 1);
    horizontalAxis.setTickUnit(Math.max(1, Math.ceil((lastWeek - firstWeek + 1) / 10.0)));

    lineChart.getData().add(series);

    boolean isPositive = n > 1 && prices.get(n - 1).compareTo(prices.get(0)) >= 0;

    lineChart.getStyleClass().removeAll("chart-positive", "chart-negative");
    lineChart.getStyleClass().add(isPositive ? "chart-positive" : "chart-negative");
  }

  /** Clears the chart data and resets the labels. */
  public void clear() {
    lineChart.getData().clear();
    titleLabel.setText("");
    subtitleLabel.setText("");
  }

  /**
   * Sets the preferred height of the chart area.
   *
   * @param height the preferred height in pixels
   */
  public void setChartHeight(double height) {
    lineChart.setPrefHeight(height);
  }

  /**
   * Updates the label text shown on the X axis.
   *
   * @param label the axis label
   */
  public void setXaxisLabel(String label) {
    horizontalAxis.setLabel(label);
  }

  /**
   * Updates the label text shown on the Y axis.
   *
   * @param label the axis label
   */
  public void setYaxisLabel(String label) {
    verticalAxis.setLabel(label);
  }
}
