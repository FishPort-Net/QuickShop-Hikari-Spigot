package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.tag.TaggingResult;
import net.tnemc.menu.core.manager.MenuManager;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SHOPS_DATA;

/** Player-owned shop tags without the upstream Shop API rewrite. */
public final class SubCommand_Tag implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Tag(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel,
                        @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      usage(sender);
      return;
    }
    switch(parser.getArgs().get(0).toLowerCase(Locale.ROOT)) {
      case "add" -> add(sender, parser);
      case "remove", "delete" -> remove(sender, parser);
      case "clear" -> clear(sender, parser);
      case "list" -> list(sender, parser);
      case "shops" -> shops(sender, parser);
      case "purge" -> purge(sender, parser);
      default -> usage(sender);
    }
  }

  private void add(final Player sender, final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      usage(sender);
      return;
    }
    final String tag = normalize(sender, parser.getArgs().get(1));
    final Shop shop = findShop(sender, parser, 2);
    if(tag == null || shop == null) {
      if(shop == null) plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }
    final TaggingResult result = plugin.tagManager().addTag(shop.getShopId(), sender.getUniqueId(), tag);
    plugin.text().of(sender, result == TaggingResult.SUCCESS? "tag-added" : "tag-add-duplicate", tag).send();
  }

  private void remove(final Player sender, final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      usage(sender);
      return;
    }
    final String tag = normalize(sender, parser.getArgs().get(1));
    final Shop shop = findShop(sender, parser, 2);
    if(tag == null || shop == null) {
      if(shop == null) plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }
    final TaggingResult result = plugin.tagManager().removeTag(shop.getShopId(), sender.getUniqueId(), tag);
    plugin.text().of(sender, result == TaggingResult.SUCCESS? "tag-removed" : "tag-remove-not-exists", tag).send();
  }

  private void clear(final Player sender, final CommandParser parser) {

    final Shop shop = findShop(sender, parser, 1);
    if(shop == null) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }
    plugin.tagManager().removeAllShopTagsBy(shop.getShopId(), sender.getUniqueId());
    plugin.text().of(sender, "tag-cleared").send();
  }

  private void list(final Player sender, final CommandParser parser) {

    final Shop shop = findShop(sender, parser, 1);
    if(shop == null) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }
    final Set<String> tags = plugin.tagManager().tagsFilteredByShop(sender.getUniqueId(), shop.getShopId());
    if(tags.isEmpty()) {
      plugin.text().of(sender, "tag-query-no-tag").send();
      return;
    }
    plugin.text().of(sender, "tag-query", tags.size()).send();
    for(final String tag : tags) {
      plugin.text().of(sender, "tag-query-listing", plugin.tagManager().service().displayTag(tag)).send();
    }
  }

  private void shops(final Player sender, final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      usage(sender);
      return;
    }
    final String tag = normalize(sender, parser.getArgs().get(1));
    if(tag == null) return;
    final List<Shop> shops = new ArrayList<>();
    for(final long shopId : plugin.tagManager().shopsFilteredByTag(sender.getUniqueId(), tag)) {
      final Shop shop = plugin.getShopManager().getShop(shopId);
      if(shop != null) shops.add(shop);
    }
    if(shops.isEmpty()) {
      plugin.text().of(sender, "tag-query-no-tag").send();
      return;
    }
    final MenuViewer viewer = new MenuViewer(sender.getUniqueId());
    viewer.addData(SHOPS_DATA, shops);
    MenuManager.instance().addViewer(viewer);
    MenuManager.instance().open("qs:browse", 1, plugin.createMenuPlayer(sender));
  }

  private void purge(final Player sender, final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      usage(sender);
      return;
    }
    final String tag = normalize(sender, parser.getArgs().get(1));
    if(tag == null) return;
    plugin.tagManager().removeTag(sender.getUniqueId(), tag);
    plugin.text().of(sender, "tag-shops-cleared", tag).send();
  }

  private @Nullable String normalize(final Player sender, final String input) {

    final String tag = plugin.tagManager().service().normalizeTag(input, false);
    if(tag == null) plugin.text().of(sender, "tag-invalid").send();
    return tag;
  }

  private void usage(final Player sender) {

    plugin.text().of(sender, "command-incorrect", "/qs tag <add|remove|clear|list|shops|purge> [tag] [shop-id]").send();
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull final Player sender,
                                               @NotNull final String commandLabel,
                                               @NotNull final CommandParser parser) {

    return parser.getArgs().size() == 1
            ? List.of("add", "remove", "clear", "list", "shops", "purge")
            : Collections.emptyList();
  }
}
