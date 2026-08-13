package com.ghostchu.quickshop.api.shop.query;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.api.shop.query.filters.FrozenFilter;
import com.ghostchu.quickshop.api.shop.query.filters.MaterialFilter;
import com.ghostchu.quickshop.api.shop.query.filters.OwnerUUIDFilter;
import com.ghostchu.quickshop.api.shop.query.filters.QUserOwnerFilter;
import com.ghostchu.quickshop.api.shop.query.filters.TypeFilter;
import com.ghostchu.quickshop.api.shop.query.filters.WorldUUIDFilter;
import org.bukkit.Material;

import java.util.UUID;

/** Built-in filters supported by the Spigot-compatible shop model. */
public final class Filters {

  public static final Filter<Material> ITEM_TYPE = new MaterialFilter();
  public static final Filter<UUID> OWNER_UUID = new OwnerUUIDFilter();
  public static final Filter<QUser> OWNER_QUSER = new QUserOwnerFilter();
  public static final Filter<Boolean> FROZEN = new FrozenFilter();
  public static final Filter<ShopType> TYPE = new TypeFilter();
  public static final Filter<UUID> WORLD_UUID = new WorldUUIDFilter();

  private Filters() {
  }
}
