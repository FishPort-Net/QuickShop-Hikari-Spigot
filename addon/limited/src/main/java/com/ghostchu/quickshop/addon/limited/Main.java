package com.ghostchu.quickshop.addon.limited;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.limited.command.SubCommand_Limit;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.event.CalendarEvent;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.event.management.ShopClickEvent;
import com.ghostchu.quickshop.api.localization.text.Text;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {

  public static Main instance;
  private QuickShop plugin;

  private CommandContainer container;

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    plugin.getCommandManager().unregisterCmd(container);
  }

  @Override
  public void onEnable() {
    // Plugin startup logic
    instance = this;
    saveDefaultConfig();
    Bukkit.getPluginManager().registerEvents(this, this);
    this.plugin = QuickShop.getInstance();
    this.container = CommandContainer.builder()
            .prefix("limit")
            .permission("quickshopaddon.limit.use")
            .description((locale)->plugin.text().of("addon.limited.commands.limit").forLocale(locale))
            .executor(new SubCommand_Limit(plugin))
            .build();
    plugin.getCommandManager().registerCmd(container);

  }

  @EventHandler(ignoreCancelled = true)
  public void shopPurchase(final ShopPurchaseEvent event) {

    final Shop shop = event.getShop();
    final ConfigurationSection storage = shop.getExtra(this);
    if(storage.getInt("limit") < 1) {
      return;
    }
    final int limit = storage.getInt("limit");
    final UUID uuid = event.getPurchaser().getUniqueIdIfRealPlayer().orElse(null);
    if(uuid != null) {
      final int playerUsedLimit = getPlayerUsedLimit(shop, storage, uuid);
      final int requestedAmount = event.getAmount();
      if(requestedAmount < 1 || (long)playerUsedLimit + requestedAmount > limit) {
        final Text text = plugin.text().of(event.getPurchaser(), "addon.limited.trade-limit-reached-cancel-reason");
        text.send();
        // Keep this addon independent from QuickShop's privately relocated Adventure classes.
        // QSCancellable converts the plain String into its own Component inside QuickShop.
        event.setCancelled(true, text.plain());
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void shopClick(final ShopClickEvent event) {

    if(event.isPhase(Phase.POST)) {

      final Shop shop = event.shop().get();
      final ConfigurationSection storage = shop.getExtra(this);
      if(storage.getInt("limit") < 1) {
        Log.debug("Shop limit is not enabled on this shop.");
        return;
      }
      final int limit = storage.getInt("limit");
      final int playerUsedLimit = getPlayerUsedLimit(shop, storage, event.user().getUniqueId());
      plugin.text().of(event.user(), "addon.limited.remains-limits", getRemainingLimit(limit, playerUsedLimit)).send();
      Log.debug("Shop limit is enabled on this shop. Limit: " + limit + " Used: " + playerUsedLimit);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void shopPurchaseSuccess(final ShopSuccessPurchaseEvent event) {

    final Shop shop = event.getShop();
    final ConfigurationSection storage = shop.getExtra(this);
    if(storage.getInt("limit") < 1) {
      return;
    }
    final UUID uuid = event.getPurchaser().getUniqueIdIfRealPlayer().orElse(null);
    if(uuid != null) {
      final int limit = storage.getInt("limit");
      final int purchasedAmount = event.getAmount();
      if(purchasedAmount < 1) {
        getLogger().warning("Ignored a successful shop transaction with a non-positive item amount: " + purchasedAmount);
        return;
      }
      int playerUsedLimit = getPlayerUsedLimit(shop, storage, uuid);
      playerUsedLimit = (int)Math.min(Integer.MAX_VALUE, (long)playerUsedLimit + purchasedAmount);
      storage.set("data." + uuid, playerUsedLimit);
      shop.setExtra(this, storage);
      final Player player = Bukkit.getPlayer(uuid);
      if(player != null) {
        player.sendTitle(plugin.text().of(player, "addon.limited.titles.title").legacy(),
                         plugin.text().of(player, "addon.limited.titles.subtitle", getRemainingLimit(limit, playerUsedLimit)).legacy());
      }
    }
  }

  private int getPlayerUsedLimit(
          final Shop shop,
          final ConfigurationSection storage,
          final UUID uuid) {

    final String path = "data." + uuid;
    final int storedAmount = storage.getInt(path, 0);
    if(storedAmount >= 0) {
      return storedAmount;
    }
    storage.set(path, 0);
    shop.setExtra(this, storage);
    getLogger().warning("Reset a negative Limited usage counter for shop " + shop.getShopId() + " and player " + uuid);
    return 0;
  }

  private static int getRemainingLimit(final int limit, final int used) {

    return (int)Math.max(0L, (long)limit - used);
  }

  @EventHandler(ignoreCancelled = true)
  public void scheduleEvent(final CalendarEvent event) {

    if(event.getCalendarTriggerType() == CalendarEvent.CalendarTriggerType.SECOND
       || event.getCalendarTriggerType() == CalendarEvent.CalendarTriggerType.NOTHING_CHANGED) {
      return;
    }
    Util.asyncThreadRun(()->plugin.getShopManager().getAllShops().forEach(shop->{
      final ConfigurationSection manager = shop.getExtra(this);
      final int limit = manager.getInt("limit");
      if(limit < 1) {
        return;
      }
      if(StringUtils.isEmpty(manager.getString("period"))) {
        return;
      }
      try {
        if(event.getCalendarTriggerType().ordinal() >= CalendarEvent.CalendarTriggerType.valueOf(manager.getString("period")).ordinal()) {
          manager.set("data", null);
          shop.setExtra(this, manager);
          Log.debug("Limit data has been reset. Shop -> " + shop);
        }
      } catch(final IllegalArgumentException ignored) {
        Log.debug("Limit data failed to reset. Shop -> " + shop + " type " + manager.getString("period") + " not exists.");
        manager.set("period", null);
        shop.setExtra(this, manager);
      }
    }));

  }
}
