package com.ghostchu.quickshop.api.shop.query.filters;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.query.Filter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public final class MaterialFilter implements Filter<Material> {

  @Override
  public boolean applies(@NotNull final Shop shop, @NotNull final Material material) {

    return shop.getItem().getType() == material;
  }
}
