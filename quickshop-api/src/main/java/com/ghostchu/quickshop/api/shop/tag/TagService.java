package com.ghostchu.quickshop.api.shop.tag;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Normalization and persistence operations for player-owned shop tags. */
public interface TagService {

  String SYS_FAV = "@fav";
  String SYS_WATCH = "@watch";
  String SYS_AVOID = "@avoid";

  @Nullable String normalizeTag(@Nullable String input, boolean allowSystem);

  String displayTag(String stored);

  CompletableFuture<Boolean> addShopTag(UUID player, long shopId, String tag);

  CompletableFuture<Boolean> removeShopTag(UUID player, long shopId, String tag);

  CompletableFuture<Boolean> removeAllShopTags(long shopId);

  CompletableFuture<Boolean> removeAllShopTagsBy(long shopId, UUID player);

  CompletableFuture<Boolean> removeAllPlayerTags(UUID player);

  CompletableFuture<Boolean> removeTagFromShops(UUID player, String tag);
}
