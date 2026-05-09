package edu.ntnu.idi.idatt.millions.view.components;

import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * UI component that renders a line chart for a stock's price history.
 * </p>
 *
 * <p>
 * The component displays a title, subtitle, and a line chart that can be
 * populated with weekly price data.
 * </p>
 */
public class StockChartComponent extends VBox {

  private final Label titleLabel;
  private final Label subtitleLabel;
  private final LineChart<Number, Number> lineChart;
  private final NumberAxis xAxis;
  private final NumberAxis yAxis;

  /**
   * <p>Constructs the chart component with initial symbol and company labels.</p>
   *
   * @param symbol  the stock symbol to show in the title
   * @param company the company name to show in the subtitle
   */
  public StockChartComponent(String symbol, String company) {
    getStyleClass().add("stat-card");
    setPadding(new Insets(5,10,5,10));
    setSpacing(5);

    titleLabel = new Label(symbol);
    titleLabel.getStyleClass().add("chart-title");

    subtitleLabel = new Label(company);
    subtitleLabel.getStyleClass().add("chart-subtitle");

    xAxis = new NumberAxis();
    xAxis.setLabel("Week");
    xAxis.setLowerBound(0);
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
    VBox.setVgrow(lineChart, Priority.ALWAYS);
    lineChart.setMaxWidth(Double.MAX_VALUE);

    getChildren().addAll(titleLabel, subtitleLabel, lineChart);
  }

  /**
   * <p>Updates the symbol and company labels shown above the chart.</p>
   *
   * @param symbol  the stock symbol to display
   * @param company the company name to display
   */
  public void setStockInfo(String symbol, String company) {
    titleLabel.setText(symbol);
    subtitleLabel.setText(company);
  }

  /**
   * <p>Replaces the chart data with the provided list of prices.</p>
   *
   * @param prices list of prices ordered by week
   */
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

  /**
   * <p>Clears the chart data and resets the labels.</p>
   */
  public void clear() {
    lineChart.getData().clear();
    titleLabel.setText("");
    subtitleLabel.setText("");
  }

  /**
   * <p>Sets the preferred height of the chart area.</p>
   *
   * @param height the preferred height in pixels
   */
  public void setChartHeight(double height) {
    lineChart.setPrefHeight(height);
  }

  /**
   * <p>Updates the label text shown on the X axis.</p>
   *
   * @param label the axis label
   */
  public void setXAxisLabel(String label) { xAxis.setLabel(label); }

  /**
   * <p>Updates the label text shown on the Y axis.</p>
   *
   * @param label the axis label
   */
  public void setYAxisLabel(String label) { yAxis.setLabel(label); }
}
