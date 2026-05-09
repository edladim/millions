package edu.ntnu.idi.idatt.millions.view;

import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.model.transaction.TransactionCalculator;
import java.math.BigDecimal;
import javafx.scene.control.Alert;

/**
 * <p>
 * Utility class for displaying transaction-related dialogs to the user.
 * </p>
 *
 * <p>
 * Centralises construction of JavaFX {@link Alert} dialogs so that
 * controllers do not need to know about UI components, formatting, or
 * label text. The class cannot be instantiated; use the static methods
 * directly.
 * </p>
 */
public final class TransactionDialog {

  private TransactionDialog() {}

  /**
   * <p>Displays a confirmation dialog summarising a completed purchase.</p>
   *
   * @param tx       the committed purchase transaction
   * @param cashLeft the player's remaining cash balance after the purchase
   */
  public static void showPurchaseConfirmation(Transaction tx, BigDecimal cashLeft) {
    TransactionCalculator calc = tx.getCalculator();
    String symbol   = tx.getShare().getStock().getSymbol();
    String quantity = ViewFormatter.quantity(tx.getShare().getQuantity());

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Purchase Confirmed");
    alert.setHeaderText("Bought " + quantity + " × " + symbol);
    alert.setContentText(
          "Cost:        " + ViewFormatter.price(calc.calculateGross())      + "\n"
        + "Commission:  " + ViewFormatter.price(calc.calculateCommission()) + "\n"
        + "Total paid:  " + ViewFormatter.price(calc.calculateTotal())      + "\n"
        + "Cash left:   " + ViewFormatter.price(cashLeft)
    );
    alert.showAndWait();
  }

  /**
   * <p>Displays a confirmation dialog summarising a completed sale.</p>
   *
   * @param tx          the committed sale transaction
   * @param cashBalance the player's cash balance after the sale
   */
  public static void showSaleConfirmation(Transaction tx, BigDecimal cashBalance) {
    TransactionCalculator calc = tx.getCalculator();
    String symbol   = tx.getShare().getStock().getSymbol();
    String quantity = ViewFormatter.quantity(tx.getShare().getQuantity());

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Sale Confirmed");
    alert.setHeaderText("Sold " + quantity + " × " + symbol);
    alert.setContentText(
          "Proceeds:     " + ViewFormatter.price(calc.calculateGross())      + "\n"
        + "Commission:   " + ViewFormatter.price(calc.calculateCommission()) + "\n"
        + "Tax:          " + ViewFormatter.price(calc.calculateTax())        + "\n"
        + "Net received: " + ViewFormatter.price(calc.calculateTotal())      + "\n"
        + "Cash balance: " + ViewFormatter.price(cashBalance)
    );
    alert.showAndWait();
  }

  /**
   * <p>Displays an error dialog with the given title and message.</p>
   *
   * @param title   the dialog title
   * @param message the error message body
   */
  public static void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
