package com.ghostchu.quickshop.economy;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.AbstractEconomy;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/** Resolves how money flows to and from owners of unlimited shops. */
public final class ShopOwnerMoneyPolicy {

  private static final String CONFIG_ROOT = "shop.unlimited-shop-owner-money";
  private static final String LEGACY_CONFIG = "shop.pay-unlimited-shop-owners";

  private ShopOwnerMoneyPolicy() {
  }

  /**
   * Returns whether proceeds from an unlimited selling shop should be deposited to its owner.
   * Limited shops always pay their owner.
   */
  public static boolean shouldPayOwner(@NotNull final Shop shop) {

    return !shop.isUnlimited() || resolve(shop).payOwner();
  }

  /**
   * Returns whether purchases by an unlimited buying shop should be withdrawn from its owner.
   * Limited shops always take money from their owner.
   */
  public static boolean shouldTakeFromOwner(@NotNull final Shop shop) {

    return !shop.isUnlimited() || resolve(shop).takeFromOwner();
  }

  /** Returns the number of shop trade units the owner can currently afford. */
  public static int ownerAffordableAmount(@NotNull final AbstractEconomy economy,
                                          @NotNull final Shop shop) {

    if(!shouldTakeFromOwner(shop) || shop.getPrice() <= 0.0d) {
      return Integer.MAX_VALUE;
    }
    final double balance = economy.getBalance(shop.getOwner(), shop.getLocation().getWorld(), shop.getCurrency());
    if(balance == Double.POSITIVE_INFINITY || balance >= shop.getPrice() * Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    if(!Double.isFinite(balance)) {
      return 0;
    }
    return Math.max(0, (int)Math.floor(balance / shop.getPrice()));
  }

  /**
   * Sends the virtual account's configured insufficient-funds message.
   *
   */
  public static void sendInsufficientFundsMessage(@NotNull final Player player,
                                                  @NotNull final Shop shop,
                                                  final double price,
                                                  final double balance) {

    final String template = resolve(shop).insufficientFundsMessage();
    if(template == null || template.isBlank()) {
      QuickShop.getInstance().text().of(player, "the-owner-cant-afford-to-buy-from-you",
                                        QuickShop.getInstance().getShopManager().format(price, shop.getLocation().getWorld(), shop.getCurrency()),
                                        QuickShop.getInstance().getShopManager().format(balance, shop.getLocation().getWorld(), shop.getCurrency())).send();
      return;
    }
    final QuickShop plugin = QuickShop.getInstance();
    final String account = shop.getOwner().getUsernameOptional().orElse(shop.getOwner().getDisplay());
    final Component message = plugin.getPlatform().miniMessage().deserialize(template,
            Placeholder.unparsed("price", plugin.getShopManager().format(price, shop.getLocation().getWorld(), shop.getCurrency())),
            Placeholder.unparsed("balance", plugin.getShopManager().format(balance, shop.getLocation().getWorld(), shop.getCurrency())),
            Placeholder.unparsed("account", account),
            Placeholder.unparsed("owner", shop.getOwner().getDisplay()));
    MsgUtil.sendDirectMessage(player, message);
  }

  /** Returns the virtual account's configured out-of-funds sign text, if any. */
  @Nullable
  public static Component outOfFundsSign(@NotNull final Shop shop) {

    final String template = resolve(shop).outOfFundsSign();
    if(template == null || template.isBlank()) {
      return null;
    }
    final String account = shop.getOwner().getUsernameOptional().orElse(shop.getOwner().getDisplay());
    return QuickShop.getInstance().getPlatform().miniMessage().deserialize(template,
            Placeholder.unparsed("account", account),
            Placeholder.unparsed("owner", shop.getOwner().getDisplay()));
  }

  @NotNull
  private static ResolvedPolicy resolve(@NotNull final Shop shop) {

    final ConfigurationSection config = QuickShop.getInstance().getConfig();
    final boolean legacy = config.getBoolean(LEGACY_CONFIG, false);
    boolean payOwner = config.getBoolean(CONFIG_ROOT + ".defaults.pay-owner", legacy);
    boolean takeFromOwner = config.getBoolean(CONFIG_ROOT + ".defaults.take-from-owner", legacy);
    String insufficientFundsMessage = null;
    String outOfFundsSign = null;

    final ConfigurationSection account = virtualAccountSection(config, shop.getOwner());
    if(account != null) {
      if(account.isSet("pay-owner")) {
        payOwner = account.getBoolean("pay-owner");
      }
      if(account.isSet("take-from-owner")) {
        takeFromOwner = account.getBoolean("take-from-owner");
      }
      insufficientFundsMessage = account.getString("insufficient-funds-message");
      outOfFundsSign = account.getString("out-of-funds-sign");
    }
    return new ResolvedPolicy(payOwner, takeFromOwner, insufficientFundsMessage, outOfFundsSign);
  }

  @Nullable
  private static ConfigurationSection virtualAccountSection(@NotNull final ConfigurationSection config,
                                                             @NotNull final QUser owner) {

    if(owner.isRealPlayer() || owner.getUsername() == null) {
      return null;
    }
    final ConfigurationSection accounts = config.getConfigurationSection(CONFIG_ROOT + ".virtual-accounts");
    if(accounts == null) {
      return null;
    }
    final String username = owner.getUsername();
    final Map<String, Object> values = accounts.getValues(false);
    Object configured = values.get(username);
    if(configured == null) {
      configured = values.entrySet().stream()
              .filter(entry -> entry.getKey().equalsIgnoreCase(username))
              .map(Map.Entry::getValue)
              .findFirst()
              .orElse(null);
    }
    return configured instanceof final ConfigurationSection section ? section : null;
  }

  private record ResolvedPolicy(boolean payOwner,
                                boolean takeFromOwner,
                                @Nullable String insufficientFundsMessage,
                                @Nullable String outOfFundsSign) {
  }
}
