package com.ghostchu.quickshop.api.event.economy;

import com.ghostchu.quickshop.api.shop.ShopType;
import org.jetbrains.annotations.NotNull;

/**
 * Describes a shop transaction from the player's point of view.
 */
public enum ShopTransactionDirection {
  /** The player buys items from the shop. */
  PLAYER_BUY,
  /** The player sells items to the shop. */
  PLAYER_SELL,
  /** The direction cannot be determined, for example for a frozen shop. */
  UNKNOWN;

  /**
   * Converts QuickShop's shop-oriented type to a player-oriented direction.
   *
   * @param shopType the shop type
   *
   * @return the player-oriented transaction direction
   */
  public static @NotNull ShopTransactionDirection fromShopType(@NotNull final ShopType shopType) {

    return switch(shopType) {
      case BUYING -> PLAYER_SELL;
      case SELLING -> PLAYER_BUY;
      case FROZEN -> UNKNOWN;
    };
  }
}
