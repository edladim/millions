package edu.ntnu.idi.idatt.millions.model.transaction;

import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Share;
import java.util.Objects;

/**
 * Abstract factory for creating and committing {@link Transaction} objects.
 *
 * <p>This class follows the <em>Factory Method</em> design pattern. Subclasses
 * define which concrete {@link Transaction} type to instantiate by implementing
 * {@link #create(Share, int)}, while this base class provides the shared
 * {@link #createAndCommit(Share, int, Player)} behaviour that creates a
 * transaction and immediately commits it.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * TransactionFactory factory = new PurchaseFactory();
 * Transaction t = factory.createAndCommit(share, week, player);
 * }</pre>
 *
 * @see PurchaseFactory
 * @see SaleFactory
 */
public abstract class TransactionFactory {

  /**
   * Creates a new transaction for the given share and week.
   *
   * <p>Subclasses decide which concrete {@link Transaction} type to return.</p>
   *
   * @param share the share involved in the transaction, cannot be null
   * @param week  the trading week, must be positive
   * @return a new, uncommitted {@link Transaction}
   * @throws NullPointerException if {@code share} is null
   * @throws IllegalArgumentException if {@code week} is not positive
   */
  public abstract Transaction create(Share share, int week);

  /**
   * Creates a transaction and immediately commits it against the given player.
   *
   * <p>This method delegates creation to {@link #create(Share, int)} and then
   * calls {@link Transaction#commit(Player)}, so both steps are always
   * performed together without duplicating this logic in every subclass.</p>
   *
   * @param share  the share involved in the transaction, cannot be null
   * @param week   the trading week, must be positive
   * @param player the player performing the transaction, cannot be null
   * @return the committed {@link Transaction}
   * @throws NullPointerException if any argument is null
   * @throws IllegalArgumentException if {@code week} is not positive
   * @throws IllegalStateException if the transaction cannot be committed
   *                               (e.g. insufficient funds or missing share)
   */
  public Transaction createAndCommit(Share share, int week, Player player) {
    Objects.requireNonNull(player, "Player cannot be null");
    Transaction transaction = create(share, week);
    transaction.commit(player);
    return transaction;
  }
}
