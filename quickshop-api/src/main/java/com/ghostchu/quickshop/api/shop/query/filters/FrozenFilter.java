package com.ghostchu.quickshop.api.shop.query.filters;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.query.Filter;
import org.jetbrains.annotations.NotNull;

public final class FrozenFilter implements Filter<Boolean> {

  @Override
  public boolean applies(@NotNull final Shop shop, @NotNull final Boolean frozen) {

    return shop.isFrozen() == frozen;
  }
}
