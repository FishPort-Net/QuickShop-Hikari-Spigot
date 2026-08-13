package com.ghostchu.quickshop.api.shop.query;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Composable in-memory shop query.
 *
 * <p>The no-argument execute method queries all known shops. The collection overload lets callers
 * query a pre-scoped market, owner, tag, or world result without touching Bukkit state.</p>
 */
public final class ShopQuery {

  private final List<Predicate<Shop>> filters = new ArrayList<>();

  public <T> @NotNull ShopQuery filterBy(@NotNull final Filter<T> filter, @NotNull final T value) {

    filters.add(shop -> filter.applies(shop, value));
    return this;
  }

  public @NotNull ShopQuery filter(@NotNull final Predicate<Shop> filter) {

    filters.add(filter);
    return this;
  }

  public @NotNull List<Shop> execute() {

    return execute(QuickShopAPI.getInstance().getShopManager().getAllShops());
  }

  public @NotNull List<Shop> execute(@NotNull final Collection<? extends Shop> source) {

    final List<Shop> shops = new ArrayList<>();
    for(final Shop shop : source) {
      boolean matches = true;
      for(final Predicate<Shop> filter : filters) {
        if(!filter.test(shop)) {
          matches = false;
          break;
        }
      }
      if(matches) {
        shops.add(shop);
      }
    }
    return shops;
  }
}
