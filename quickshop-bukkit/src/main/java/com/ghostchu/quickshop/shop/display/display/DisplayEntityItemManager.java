package com.ghostchu.quickshop.shop.display.display;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.shop.SimpleShopChunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks real Bukkit display entities by chunk for player visibility and cleanup. */
public final class DisplayEntityItemManager {

  private final Map<ShopChunk, Map<Integer, DisplayEntityDisplayItem>> chunks = new ConcurrentHashMap<>();

  public DisplayEntityDisplayItem create(@NotNull final Shop shop) {

    final ShopChunk chunk = SimpleShopChunk.fromLocation(shop.getLocation());
    return chunks.computeIfAbsent(chunk, ignored->new ConcurrentHashMap<>())
            .computeIfAbsent(shop.getLocation().hashCode(), ignored->new DisplayEntityDisplayItem(this, shop, chunk));
  }

  void track(final ShopChunk chunk, final int locationHash, final DisplayEntityDisplayItem display) {

    chunks.computeIfAbsent(chunk, ignored->new ConcurrentHashMap<>()).put(locationHash, display);
  }

  void remove(final ShopChunk chunk, final int locationHash, final DisplayEntityDisplayItem display) {

    chunks.computeIfPresent(chunk, (ignored, displays)->{
      displays.remove(locationHash, display);
      return displays.isEmpty()? null : displays;
    });
  }

  public void addPlayer(@NotNull final Player player) {

    final Location location = player.getLocation();
    final int radius = Math.max(2, QuickShop.getInstance().getJavaPlugin().getServer().getViewDistance());
    final int centerX = location.getBlockX() >> 4;
    final int centerZ = location.getBlockZ() >> 4;
    for(int x = centerX - radius; x <= centerX + radius; x++) {
      for(int z = centerZ - radius; z <= centerZ + radius; z++) {
        final Map<Integer, DisplayEntityDisplayItem> displays = chunks.get(
                new SimpleShopChunk(player.getWorld().getName(), x, z));
        if(displays != null) {
          displays.values().forEach(display->display.showTo(player));
        }
      }
    }
  }
}
