package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.api.shop.query.Filters;
import com.ghostchu.quickshop.api.shop.query.ShopQuery;
import com.ghostchu.quickshop.api.shop.tag.TagService;
import com.ghostchu.quickshop.util.Util;
import net.tnemc.menu.core.compatibility.MenuPlayer;
import net.tnemc.menu.core.manager.MenuManager;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SHOPS_DATA;


public class SubCommand_Browse implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Browse(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    boolean worldOnly = false;
    ShopType type = null;
    String playerTag = null;
    boolean tagRequested = false;
    Comparator<Shop> ordering = Comparator.comparingLong(Shop::getShopId);
    final List<String> searchTerms = new ArrayList<>();
    for(final String argument : parser.getArgs()) {
      final String value = argument.toLowerCase(Locale.ROOT);
      switch(value) {
        case "world" -> worldOnly = true;
        case "sell", "selling" -> type = ShopType.SELLING;
        case "buy", "buying" -> type = ShopType.BUYING;
        case "favorite", "favorites" -> playerTag = TagService.SYS_FAV;
        case "watch", "watching" -> playerTag = TagService.SYS_WATCH;
        case "price-asc" -> ordering = Comparator.comparingDouble(Shop::getPrice);
        case "price-desc" -> ordering = Comparator.comparingDouble(Shop::getPrice).reversed();
        case "newest" -> ordering = Comparator.comparingLong(Shop::getShopId).reversed();
        default -> {
          if(value.startsWith("tag:")) {
            tagRequested = true;
            playerTag = plugin.tagManager().service().normalizeTag(value.substring(4), false);
          } else {
            searchTerms.add(value.replace(' ', '_'));
          }
        }
      }
    }
    if(tagRequested && playerTag == null) {
      plugin.text().of(sender, "tag-invalid").send();
      return;
    }

    final boolean filterWorld = worldOnly;
    final ShopType filterType = type;
    final String filterTag = playerTag;
    final Comparator<Shop> shopOrdering = ordering;
    final Location origin = sender.getLocation().clone();

    Util.asyncThreadRun(()->{
      final List<Shop> source = new ArrayList<>();
      if(filterTag == null) {
        source.addAll(plugin.getShopManager().getAllShops());
      } else {
        for(final long shopId : plugin.tagManager().shopsFilteredByTag(sender.getUniqueId(), filterTag)) {
          final Shop shop = plugin.getShopManager().getShop(shopId);
          if(shop != null) source.add(shop);
        }
      }

      final ShopQuery query = new ShopQuery().filterBy(Filters.FROZEN, false);
      if(filterWorld && origin.getWorld() != null) {
        query.filterBy(Filters.WORLD_UUID, origin.getWorld().getUID());
      }
      if(filterType != null) {
        query.filterBy(Filters.TYPE, filterType);
      }
      if(!searchTerms.isEmpty()) {
        query.filter(shop->{
          final String material = shop.getItem().getType().name().toLowerCase(Locale.ROOT);
          final String shopName = shop.getShopName() == null? "" : shop.getShopName().toLowerCase(Locale.ROOT);
          final String owner = shop.getOwner().getDisplay().toLowerCase(Locale.ROOT);
          return searchTerms.stream().allMatch(term->material.contains(term)
                                                       || shopName.contains(term)
                                                       || owner.contains(term));
        });
      }
      final List<CompletableFuture<Shop>> availabilityQueries = query.execute(source).stream()
              .map(shop->(shop.isBuying()? shop.getRemainingSpaceAsync() : shop.getRemainingStockAsync())
                      .thenApply(available->available == 0? null : shop))
              .toList();
      CompletableFuture.allOf(availabilityQueries.toArray(CompletableFuture[]::new)).thenRun(()->{
        final List<Shop> shops = availabilityQueries.stream()
                .map(CompletableFuture::join)
                .filter(java.util.Objects::nonNull)
                .sorted(shopOrdering)
                .toList();
        Util.mainThreadRun(()->{
          if(!sender.isOnline()) return;
          if(shops.isEmpty()) {
            plugin.text().of(sender, "browse-no-results").send();
            return;
          }
          final MenuViewer viewer = new MenuViewer(sender.getUniqueId());
          viewer.addData(SHOPS_DATA, shops);
          MenuManager.instance().addViewer(viewer);
          final MenuPlayer menuPlayer = plugin.createMenuPlayer(sender);
          MenuManager.instance().open("qs:browse", 1, menuPlayer);
        });
      }).exceptionally(error->{
        plugin.logger().warn("Failed to query shop availability for market browser", error);
        return null;
      });
    });
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return List.of("world", "sell", "buy", "favorite", "watch", "tag:<tag>",
                     "price-asc", "price-desc", "newest", "<item|shop|owner>");
    }
    return Collections.emptyList();
  }
}
