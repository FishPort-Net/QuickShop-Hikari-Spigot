package com.ghostchu.quickshop.api.event.economy;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Calling when success purchase in shop
 */
public class ShopSuccessPurchaseEvent extends AbstractQSEvent {

  private final ShopTransactionContext context;
  private final double tax;

  /**
   * Builds a new shop purchase event Will called when purchase ended
   *
   * @param shop               The shop bought from
   * @param purchaser          The player buying, may offline if purchase by plugin
   * @param purchaserInventory The purchaseing target inventory, *MAY NOT A PLAYER INVENTORY IF
   *                           PLUGIN PURCHASE THIS*
   * @param amount             The amount they're buying
   * @param tax                The tax in this purchase
   * @param total              The money in this purchase
   */
  public ShopSuccessPurchaseEvent(
          @NotNull final Shop shop, @NotNull final QUser purchaser, @NotNull final InventoryWrapper purchaserInventory, final int amount, final double total, final double tax) {

    this(new ShopTransactionContext(
            shop,
            purchaser,
            purchaserInventory,
            ShopTransactionDirection.fromShopType(shop.getShopType()),
            amount,
            BigDecimal.valueOf(shop.getPrice()),
            BigDecimal.valueOf(total)), tax);
  }

  /**
   * Builds a success event around the context from the corresponding pre-transaction event.
   *
   * @param context the shared transaction context
   * @param tax     the tax charged by the transaction
   */
  public ShopSuccessPurchaseEvent(@NotNull final ShopTransactionContext context, final double tax) {

    this.context = context;
    this.tax = tax;
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
   * @return the number of configured shop units traded
   */
  public int getTradeCount() {

    return context.getTradeCount();
  }

  /**
   * The total money changes in this purchase. Calculate tax, if you want get total without tax,
   * please use getBalanceWithoutTax()
   *
   * @return the total money with calculate tax
   */
  public double getBalance() {

    return context.getTotalPrice().doubleValue() - tax;
  }

  /**
   * The total money changes in this purchase. No calculate tax, if you want get total with tax,
   * please use getBalance()
   *
   * @return the total money without calculate tax
   */
  public double getBalanceWithoutTax() {

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
   * Gets the tax in this purchase
   *
   * @return The tax
   */
  public double getTax() {

    return this.tax;
  }

  /**
   * @return the shared transaction context
   */
  public @NotNull ShopTransactionContext getContext() {

    return context;
  }

  /**
   * @return the id shared by all lifecycle events for this transaction attempt
   */
  public @NotNull UUID getTransactionId() {

    return context.getTransactionId();
  }
}
