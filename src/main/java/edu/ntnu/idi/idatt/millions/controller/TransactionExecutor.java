package edu.ntnu.idi.idatt.millions.controller;

import edu.ntnu.idi.idatt.millions.model.transaction.Transaction;
import edu.ntnu.idi.idatt.millions.view.TransactionDialog;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>Helper that executes a transaction-producing action and uniformly reports
 * success or failure through {@link TransactionDialog}.</p>
 *
 * <p>Centralises the try/catch pattern used by buy and sell handlers in the
 * trading and portfolio controllers: validation errors raised by the model are
 * surfaced as user-friendly error dialogs, and unexpected exceptions are
 * surfaced as generic error dialogs so the UI never crashes silently.</p>
 *
 * <p>This class cannot be instantiated; use the static method directly.</p>
 */
public final class TransactionExecutor {

  private TransactionExecutor() {}

  /**
   * <p>Runs the given action and dispatches the produced {@link Transaction}
   * to the {@code onSuccess} consumer. Validation errors
   * ({@link IllegalStateException}, {@link IllegalArgumentException}) are shown
   * under {@code errorTitle}; any other exception is shown under
   * {@code "Unexpected error"}.</p>
   *
   * @param errorTitle the dialog title shown for validation errors
   * @param action     a supplier that executes the transaction and returns it
   * @param onSuccess  callback invoked with the resulting transaction on success
   */
  public static void execute(
      String errorTitle,
      Supplier<Transaction> action,
      Consumer<Transaction> onSuccess
  ) {
    try {
      Transaction tx = action.get();
      onSuccess.accept(tx);
    } catch (IllegalStateException | IllegalArgumentException e) {
      TransactionDialog.showError(errorTitle, e.getMessage());
    } catch (Exception e) {
      TransactionDialog.showError(
          "Unexpected error",
          "Could not complete: " + e.getMessage()
      );
    }
  }
}
