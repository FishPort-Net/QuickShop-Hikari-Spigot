package com.ghostchu.quickshop.api.shop.query.filters;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.query.Filter;
import org.jetbrains.annotations.NotNull;

public final class QUserOwnerFilter implements Filter<QUser> {

  @Override
  public boolean applies(@NotNull final Shop shop, @NotNull final QUser owner) {

    return owner.equals(shop.getOwner());
  }
}
