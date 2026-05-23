package edu.ntnu.idi.idatt.millions.view.dialogs;

import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.model.transaction.TransactionCalculator;
import edu.ntnu.idi.idatt.millions.view.util.ViewFormatter;
import java.math.BigDecimal;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 * <p>
 * Utility class for displaying transaction-related dialogs to the user.
 * </p>
 *
 * <p>
 * Centralizes construction of ControlsFX {@link Notifications} dialogs so that
 * controllers do not need to know about UI components, formatting, or
 * label text. The class cannot be instantiated; use the static methods
 * directly.
 * </p>
 */
public final class TransactionDialog {

  private TransactionDialog() {}

  /**
   * <p>Displays a confirmation dialog summarizing a completed purchase.</p>
   *
   * @param tx       the committed purchase transaction
   * @param cashLeft the player's remaining cash balance after the purchase
   */
  public static void showPurchaseConfirmation(Transaction tx, BigDecimal cashLeft) {
    TransactionCalculator calc = tx.getCalculator();
    String symbol = tx.getShare().getStock().getSymbol();
    String quantity = ViewFormatter.quantity(tx.getShare().getQuantity());

    Notifications.create()
            .title("Purchase Confirmation")
            .text("Bought " + quantity + " × " + symbol
                    + "Cost: " + ViewFormatter.price(calc.calculateGross()) + "\n"
                    + "Commission:  " + ViewFormatter.price(calc.calculateCommission()) + "\n"
                    + "Total paid:  " + ViewFormatter.price(calc.calculateTotal()) + "\n"
                    + "Cash left:   " + ViewFormatter.price(cashLeft)
            )
            .hideAfter(Duration.seconds(5))
            .darkStyle()
            .showInformation();
  }

  /**
   * <p>Displays a confirmation dialog summarizing a completed sale.</p>
   *
   * @param tx          the committed sale transaction
   * @param cashBalance the player's cash balance after the sale
   */
  public static void showSaleConfirmation(Transaction tx, BigDecimal cashBalance) {
    TransactionCalculator calc = tx.getCalculator();
    String symbol = tx.getShare().getStock().getSymbol();
    String quantity = ViewFormatter.quantity(tx.getShare().getQuantity());

    Notifications.create()
            .title("Sale Confirmation")
            .text("Sold " + quantity + " × " + symbol
                    + "Proceeds:     " + ViewFormatter.price(calc.calculateGross()) + "\n"
                    + "Commission:   " + ViewFormatter.price(calc.calculateCommission()) + "\n"
                    + "Tax:          " + ViewFormatter.price(calc.calculateTax()) + "\n"
                    + "Net received: " + ViewFormatter.price(calc.calculateTotal()) + "\n"
                    + "Cash balance: " + ViewFormatter.price(cashBalance))
            .hideAfter(Duration.seconds(5))
            .darkStyle()
            .showInformation();
  }

  /**
   * <p>Displays an error dialog with the given title and message.</p>
   *
   * @param title   the dialog title
   * @param message the error message body
   */
  public static void showError(String title, String message) {
    Notifications.create()
            .title(title)
            .text(message)
            .position(Pos.TOP_RIGHT)
            .hideAfter(Duration.seconds(6))
            .darkStyle()
            .showError();
  }
}
