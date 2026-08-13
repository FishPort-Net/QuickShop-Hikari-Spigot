package com.ghostchu.quickshop.api.shop.query.filters;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.query.Filter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class WorldUUIDFilter implements Filter<UUID> {

  @Override
  public boolean applies(@NotNull final Shop shop, @NotNull final UUID world) {

    return shop.getLocation().getWorld() != null && shop.getLocation().getWorld().getUID().equals(world);
  }
}
