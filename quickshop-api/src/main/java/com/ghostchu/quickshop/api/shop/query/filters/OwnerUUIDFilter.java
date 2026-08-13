package com.ghostchu.quickshop.api.shop.query.filters;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.query.Filter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class OwnerUUIDFilter implements Filter<UUID> {

  @Override
  public boolean applies(@NotNull final Shop shop, @NotNull final UUID owner) {

    return owner.equals(shop.getOwner().getUniqueId());
  }
}
