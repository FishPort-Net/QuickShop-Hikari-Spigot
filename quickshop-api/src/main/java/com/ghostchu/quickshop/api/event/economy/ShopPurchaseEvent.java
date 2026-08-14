package com.ghostchu.quickshop.api.event.economy;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.event.QSCancellable;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Calling when purchaser purchased a shop
 */
public class ShopPurchaseEvent extends AbstractQSEvent implements QSCancellable {

  private final ShopTransactionContext context;

  private boolean cancelled;
  private @Nullable Component cancelReason;

  /**
   * Builds a new shop purchase event Will called when purchase starting For recording purchase,
   * please listen to ShopSuccessPurchaseEvent.
   *
   * @param shop               The shop bought from
   * @param purchaser          The player buying, may offline if purchase by plugin
   * @param purchaserInventory The purchaseing target inventory, *MAY NOT A PLAYER INVENTORY IF
   *                           PLUGIN PURCHASE THIS*
   * @param amount             The amount they're buying
   * @param total              The total balance in this purchase
   */
  public ShopPurchaseEvent(@NotNull final Shop shop, @NotNull final QUser purchaser, @NotNull final InventoryWrapper purchaserInventory, final int amount, final double total) {

    this(new ShopTransactionContext(
            shop,
            purchaser,
            purchaserInventory,
            ShopTransactionDirection.fromShopType(shop.getShopType()),
            amount,
            BigDecimal.valueOf(shop.getPrice()),
            BigDecimal.valueOf(total)));
  }

  /**
   * Builds a pre-transaction event around a shared lifecycle context.
   *
   * @param context the context that will also be used by the success or failure event
   */
  public ShopPurchaseEvent(@NotNull final ShopTransactionContext context) {

    this.context = context;
  }

  /**
   * Gets the item stack amounts
   *
   * @return Item stack amounts
   */
  public int getAmount() {

    return context.getItemAmount();
  }

  /**
   * Gets the number of configured shop units requested in this transaction. A shop unit may contain
   * more than one individual item.
   *
   * @return the requested shop unit count
   */
  public int getTradeCount() {

    return context.getTradeCount();
  }

  @Override
  public @Nullable Component getCancelReason() {

    return this.cancelReason;
  }

  @Override
  public void setCancelled(final boolean cancel, @Nullable final Component reason) {

    this.cancelled = cancel;
    this.cancelReason = reason;
  }

  /**
   * Gets the purchaser, that maybe is a online/offline/virtual player.
   *
   * @return The purchaser uuid
   */
  public @NotNull QUser getPurchaser() {

    return context.getTrader();
  }

  /**
   * Gets the inventory of purchaser (the item will put to)
   *
   * @return The inventory
   */
  public @NotNull InventoryWrapper getPurchaserInventory() {

    return context.getTraderInventory();
  }

  /**
   * Gets the shop
   *
   * @return the shop
   */
  public @NotNull Shop getShop() {

    return context.getShop();
  }

  /**
   * Gets the total money trans for
   *
   * @return Total money
   */
  public double getTotal() {

    return context.getTotalPrice().doubleValue();
  }

  /**
   * Gets the effective total price before tax without converting it to a double.
   *
   * @return the effective total price
   */
  public @NotNull BigDecimal getTotalPrice() {

    return context.getTotalPrice();
  }

  /**
   * Sets new total money trans for
   *
   * @param total Total money
   */
  public void setTotal(final double total) {

    setTotalPrice(BigDecimal.valueOf(total));
  }

  /**
   * Sets the effective total price before tax.
   *
   * @param total the new effective total price
   */
  public void setTotalPrice(@NotNull final BigDecimal total) {

    context.setTotalPrice(total);
  }

  /**
   * Gets the shared lifecycle context.
   *
   * @return the transaction context
   */
  public @NotNull ShopTransactionContext getContext() {

    return context;
  }

  /**
   * Gets the id shared by all lifecycle events for this transaction attempt.
   *
   * @return the transaction id
   */
  public @NotNull UUID getTransactionId() {

    return context.getTransactionId();
  }

  @Override
  public boolean isCancelled() {

    return this.cancelled;
  }
}
