package edu.ntnu.idi.idatt.millions.model.transaction;

import edu.ntnu.idi.idatt.millions.model.player.Player;
import edu.ntnu.idi.idatt.millions.model.portfolio.Share;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a purchase transaction.
 *
 * <p>A purchase transaction buys a {@link Share} for a player. When the
 * transaction is committed, the following actions occur:</p>
 *
 * <ol>
 *   <li>The total purchase cost is withdrawn from the player's money</li>
 *   <li>The share is added to the player's portfolio</li>
 *   <li>The transaction is stored in the player's transaction archive</li>
 * </ol>
 *
 * <p>A purchase can only be committed once.</p>
 */
public final class Purchase extends Transaction {

  /**
   * Creates a new purchase transaction.
   *
   * @param share the share to purchase
   * @param week the week in which the purchase occurs
   */
  public Purchase(Share share, int week) {
    super(share, week, new PurchaseCalculator(share));
  }

  /**
   * Commits the purchase transaction.
   *
   * <p>The purchase is completed by withdrawing the total transaction
   * cost from the player, adding the share to the portfolio, and
   * storing the transaction in the archive.</p>
   *
   * @param player the player performing the purchase
   *
   * @throws NullPointerException if {@code player} is null
   * @throws IllegalStateException if the transaction is already committed
   * @throws IllegalStateException if the player does not have enough money
   */
  @Override
  public void commit(Player player) {

    Objects.requireNonNull(player, "Player cannot be null");

    if (isCommitted()) {
      throw new IllegalStateException("Transaction already committed");
    }

    BigDecimal totalCost = getCalculator().calculateTotal();

    if (player.getMoney().compareTo(totalCost) < 0) {
      throw new IllegalStateException("Player has insufficient funds");
    }

    player.getTransactionArchive().add(this);
    player.getPortfolio().addShare(getShare());
    player.withdrawMoney(totalCost);

    committed = true;
  }
}