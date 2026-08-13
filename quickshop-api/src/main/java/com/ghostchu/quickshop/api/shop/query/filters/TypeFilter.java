package com.ghostchu.quickshop.api.shop.query.filters;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.api.shop.query.Filter;
import org.jetbrains.annotations.NotNull;

public final class TypeFilter implements Filter<ShopType> {

  @Override
  public boolean applies(@NotNull final Shop shop, @NotNull final ShopType type) {

    return shop.getShopType() == type;
  }
}
