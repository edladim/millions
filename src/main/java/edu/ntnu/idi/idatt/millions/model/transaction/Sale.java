package edu.ntnu.idi.idatt.millions.model.transaction;

import edu.ntnu.idi.idatt.millions.model.Player;
import edu.ntnu.idi.idatt.millions.model.Share;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a sale transaction.
 *
 * <p>A sale transaction sells a {@link Share} owned by a player. When
 * the transaction is committed, the following actions occur:</p>
 *
 * <ol>
 *   <li>The total sale value is added to the player's money</li>
 *   <li>The share is removed from the player's portfolio</li>
 *   <li>The transaction is stored in the player's transaction archive</li>
 * </ol>
 *
 * <p>A sale can only be committed once.</p>
 */
public final class Sale extends Transaction {

  /**
   * Creates a new sale transaction.
   *
   * @param share the share to sell
   * @param week the week in which the sale occurs
   */
  public Sale(Share share, int week) {
    super(share, week, new SaleCalculator(share));
  }

  /**
   * Commits the sale transaction.
   *
   * <p>The sale is completed by adding the total sale value to the
   * player's money, removing the share from the portfolio, and storing
   * the transaction in the archive.</p>
   *
   * @param player the player performing the sale
   *
   * @throws NullPointerException if {@code player} is null
   * @throws IllegalStateException if the transaction is already committed
   * @throws IllegalStateException if the player does not own the share
   */
  @Override
  public void commit(Player player) {

    Objects.requireNonNull(player, "Player cannot be null");

    if (isCommitted()) {
      throw new IllegalStateException("Transaction already committed");
    }

    if (!player.getPortfolio().contains(getShare())) {
      throw new IllegalStateException("Player does not own the share");
    }

    BigDecimal totalValue = getCalculator().calculateTotal();

    player.addMoney(totalValue);
    player.getPortfolio().removeShare(getShare());
    player.getTransactionArchive().add(this);

    committed = true;
  }
}