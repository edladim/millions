package edu.ntnu.idi.idatt.millions.view.components;

import edu.ntnu.idi.idatt.millions.view.Page;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

public class SidebarComponent extends VBox {

  private Button dashboardBtn;
  private Button portfolioBtn;
  private Button tradingBtn;
  private Label weekLabel;

  private Runnable onAdvanceWeek;
  private Consumer<Page> onNavigate;

  public SidebarComponent() {
    getStyleClass().add("sidebar");
    setPrefWidth(260);
    setMinWidth(260);
    setMaxWidth(260);
    setSpacing(4);
    setPadding(new Insets(24, 16, 24, 16));

  }

  private FontIcon setLogo () {
    FontIcon dashboardIcon = new FontIcon("fas-digital-tachograph");
    dashboardIcon.setStyle("-fx-icon-color: white;");
    dashboardIcon.setIconSize(16);
    return dashboardIcon;
  }

  private Button buildNavButton(String text, Page page) {
    Button btn = new Button(text);
    btn.getStyleClass().add("nav-btn");
    btn.setMaxWidth(Double.MAX_VALUE);
    btn.setOnAction(e -> {
      setActivePage(page);
      if (onNavigate != null) onNavigate.accept(page);
    });
    return btn;
  }

  public void setActivePage(Page page) {
    dashboardBtn.getStyleClass().removeAll("nav-btn-active");
    portfolioBtn.getStyleClass().removeAll("nav-btn-active");
    tradingBtn.getStyleClass().removeAll("nav-btn-active");

    switch (page) {
      case DASHBOARD -> dashboardBtn.getStyleClass().add("nav-btn-active");
      case PORTFOLIO -> portfolioBtn.getStyleClass().add("nav-btn-active");
      case TRADING   -> tradingBtn.getStyleClass().add("nav-btn-active");
    }
  }
}
