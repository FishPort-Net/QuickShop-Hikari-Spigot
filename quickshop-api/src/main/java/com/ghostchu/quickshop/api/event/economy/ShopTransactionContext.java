package com.ghostchu.quickshop.api.event.economy;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Shared context for every lifecycle event belonging to one shop transaction attempt.
 *
 * <p>The transaction id is unique per attempt. The shop id is QuickShop's persistent database id;
 * callers must not use {@link Shop#getRuntimeRandomUniqueId()} for persistent integrations.</p>
 */
public final class ShopTransactionContext {

  private final UUID transactionId;
  private final Shop shop;
  private final long shopId;
  private final ShopType shopType;
  private final ShopTransactionDirection direction;
  private final QUser trader;
  private final @Nullable UUID traderUniqueId;
  private final InventoryWrapper traderInventory;
  private final QUser shopOwner;
  private final ItemStack item;
  private final int tradeCount;
  private final int itemAmount;
  private final BigDecimal unitPrice;
  private BigDecimal totalPrice;
  private final @Nullable String currency;

  /**
   * Creates a transaction context with a generated transaction id.
   *
   * @param shop            the shop being traded with
   * @param trader          the player or user performing the trade
   * @param traderInventory the inventory participating in the trade
   * @param direction       the direction from the trader's point of view
   * @param tradeCount      the number of configured shop units being traded
   * @param unitPrice       the price of one configured shop unit
   * @param totalPrice      the effective total price before tax
   */
  public ShopTransactionContext(
          @NotNull final Shop shop,
          @NotNull final QUser trader,
          @NotNull final InventoryWrapper traderInventory,
          @NotNull final ShopTransactionDirection direction,
          final int tradeCount,
          @NotNull final BigDecimal unitPrice,
          @NotNull final BigDecimal totalPrice) {

    this(UUID.randomUUID(), shop, trader, traderInventory, direction, tradeCount, unitPrice, totalPrice);
  }

  /**
   * Creates a transaction context with an explicit transaction id.
   *
   * @param transactionId   the unique id for this transaction attempt
   * @param shop            the shop being traded with
   * @param trader          the player or user performing the trade
   * @param traderInventory the inventory participating in the trade
   * @param direction       the direction from the trader's point of view
   * @param tradeCount      the number of configured shop units being traded
   * @param unitPrice       the price of one configured shop unit
   * @param totalPrice      the effective total price before tax
   */
  public ShopTransactionContext(
          @NotNull final UUID transactionId,
          @NotNull final Shop shop,
          @NotNull final QUser trader,
          @NotNull final InventoryWrapper traderInventory,
          @NotNull final ShopTransactionDirection direction,
          final int tradeCount,
          @NotNull final BigDecimal unitPrice,
          @NotNull final BigDecimal totalPrice) {

    this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
    this.shop = Objects.requireNonNull(shop, "shop");
    this.shopId = shop.getShopId();
    this.shopType = shop.getShopType();
    this.direction = Objects.requireNonNull(direction, "direction");
    this.trader = Objects.requireNonNull(trader, "trader");
    this.traderUniqueId = trader.getUniqueId();
    this.traderInventory = Objects.requireNonNull(traderInventory, "traderInventory");
    this.shopOwner = shop.getOwner();
    this.item = shop.getItem().clone();
    this.tradeCount = tradeCount;
    this.itemAmount = tradeCount * this.item.getAmount();
    this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
    this.totalPrice = Objects.requireNonNull(totalPrice, "totalPrice");
    this.currency = shop.getCurrency();
  }

  /**
   * @return the unique id shared by the pre, success, and failure events for this attempt
   */
  public @NotNull UUID getTransactionId() {

    return transactionId;
  }

  /**
   * @return the live shop involved in the transaction
   */
  public @NotNull Shop getShop() {

    return shop;
  }

  /**
   * @return QuickShop's persistent shop database id
   */
  public long getShopId() {

    return shopId;
  }

  /**
   * @return the shop type captured when the transaction started
   */
  public @NotNull ShopType getShopType() {

    return shopType;
  }

  /**
   * @return the transaction direction from the trader's point of view
   */
  public @NotNull ShopTransactionDirection getDirection() {

    return direction;
  }

  /**
   * @return the user performing the transaction
   */
  public @NotNull QUser getTrader() {

    return trader;
  }

  /**
   * Returns the trader UUID when the supplied user has one. QuickShop's normal player trade paths
   * always provide it, while programmatically constructed legacy events may not.
   *
   * @return the trader UUID, if available
   */
  public @NotNull Optional<UUID> getTraderUniqueId() {

    return Optional.ofNullable(traderUniqueId);
  }

  /**
   * @return the inventory participating in the transaction
   */
  public @NotNull InventoryWrapper getTraderInventory() {

    return traderInventory;
  }

  /**
   * @return the shop owner captured when the transaction started
   */
  public @NotNull QUser getShopOwner() {

    return shopOwner;
  }

  /**
   * @return a clone of the shop item captured when the transaction started
   */
  public @NotNull ItemStack getItem() {

    return item.clone();
  }

  /**
   * @return the number of configured shop units requested by the trader
   */
  public int getTradeCount() {

    return tradeCount;
  }

  /**
   * @return the actual number of individual items transferred
   */
  public int getItemAmount() {

    return itemAmount;
  }

  /**
   * @return the price of one configured shop unit
   */
  public @NotNull BigDecimal getUnitPrice() {

    return unitPrice;
  }

  /**
   * @return the effective total price before tax
   */
  public @NotNull BigDecimal getTotalPrice() {

    return totalPrice;
  }

  void setTotalPrice(@NotNull final BigDecimal totalPrice) {

    this.totalPrice = Objects.requireNonNull(totalPrice, "totalPrice");
  }

  /**
   * @return the configured shop currency, or {@code null} for the default currency
   */
  public @Nullable String getCurrency() {

    return currency;
  }
}
