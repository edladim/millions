package edu.ntnu.idi.idatt.millions;

import edu.ntnu.idi.idatt.millions.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {
    MainView mainView = new MainView();

    primaryStage.setTitle("Millions");
    primaryStage.setScene(mainView.createScene());
    primaryStage.setMaximized(true);
    primaryStage.show();
  }
  static void main() {
    launch();
  }
}
