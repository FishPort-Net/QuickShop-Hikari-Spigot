package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.tag.TaggingResult;
import net.tnemc.menu.core.compatibility.MenuPlayer;
import net.tnemc.menu.core.manager.MenuManager;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SHOPS_DATA;

/** Implements the favorite and watch commands on top of reserved player tags. */
public final class SubCommand_SystemTag implements CommandHandler<Player> {

  private final QuickShop plugin;
  private final String tag;
  private final String messagePrefix;

  public SubCommand_SystemTag(final QuickShop plugin, final String tag, final String messagePrefix) {

    this.plugin = plugin;
    this.tag = tag;
    this.messagePrefix = messagePrefix;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel,
                        @NotNull final CommandParser parser) {

    if(!parser.getArgs().isEmpty() && parser.getArgs().get(0).equalsIgnoreCase("list")) {
      openList(sender);
      return;
    }
    final Shop shop = findShop(sender, parser, 0);
    if(shop == null) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }
    final TaggingResult result = plugin.tagManager().toggleTag(shop.getShopId(), sender.getUniqueId(), tag);
    if(result != TaggingResult.SUCCESS) {
      plugin.text().of(sender, messagePrefix + "-unable").send();
      return;
    }
    final String key = plugin.tagManager().hasTag(shop.getShopId(), sender.getUniqueId(), tag)
            ? messagePrefix + "-added" : messagePrefix + "-removed";
    plugin.text().of(sender, key).send();
  }

  private void openList(final Player sender) {

    final List<Shop> shops = new ArrayList<>();
    for(final long shopId : plugin.tagManager().shopsFilteredByTag(sender.getUniqueId(), tag)) {
      final Shop shop = plugin.getShopManager().getShop(shopId);
      if(shop != null) {
        shops.add(shop);
      }
    }
    if(shops.isEmpty()) {
      plugin.text().of(sender, messagePrefix + "-none").send();
      return;
    }
    final MenuViewer viewer = new MenuViewer(sender.getUniqueId());
    viewer.addData(SHOPS_DATA, shops);
    MenuManager.instance().addViewer(viewer);
    final MenuPlayer menuPlayer = plugin.createMenuPlayer(sender);
    MenuManager.instance().open("qs:browse", 1, menuPlayer);
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull final Player sender,
                                               @NotNull final String commandLabel,
                                               @NotNull final CommandParser parser) {

    return parser.getArgs().size() == 1? List.of("list", "<shop-id>") : Collections.emptyList();
  }
}
