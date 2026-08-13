package com.ghostchu.quickshop.api.shop.query;

import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

/** A reusable condition for selecting shops. */
@FunctionalInterface
public interface Filter<T> {

  boolean applies(@NotNull Shop shop, @NotNull T value);
}
