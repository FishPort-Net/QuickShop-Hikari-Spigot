package com.ghostchu.quickshop.shop.display.display;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;

/** Configurable text-display renderer kept independent from Paper APIs. */
public final class DisplayTextRenderer {

  private DisplayTextRenderer() {
  }

  public static Component render(final Shop shop) {

    final QuickShop plugin = QuickShop.getInstance();
    final List<String> lines = plugin.getConfig().getStringList("shop.text-display.lines");
    final String price = plugin.getEconomy() == null || shop.getLocation().getWorld() == null
            ? Double.toString(shop.getPrice())
            : plugin.getEconomy().format(shop.getPrice(), shop.getLocation().getWorld(), shop.getCurrency());
    Component result = Component.empty();
    for(int index = 0; index < lines.size(); index++) {
      result = result.append(MiniMessage.miniMessage().deserialize(lines.get(index),
              Placeholder.component("item_name", Util.getItemStackName(shop.getItem())),
              Placeholder.unparsed("price_alone", price),
              Placeholder.unparsed("amount", Integer.toString(shop.getShopStackingAmount())),
              Placeholder.unparsed("price_amount", price + " / " + shop.getShopStackingAmount()),
              Placeholder.unparsed("owner", shop.getOwner().getDisplay()),
              Placeholder.unparsed("type", shop.getShopType().name()),
              Placeholder.unparsed("status", shop.isFrozen()? "FROZEN" : "ACTIVE")));
      if(index + 1 < lines.size()) {
        result = result.append(Component.newline());
      }
    }
    return result;
  }
}
