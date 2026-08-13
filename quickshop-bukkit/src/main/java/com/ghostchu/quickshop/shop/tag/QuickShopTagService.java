package com.ghostchu.quickshop.shop.tag;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import com.ghostchu.quickshop.api.shop.tag.TagService;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public final class QuickShopTagService implements TagService {

  public static final int MAX_TAG_LENGTH = 32;
  private static final Pattern VALID_TAG_PATTERN = Pattern.compile("^[a-z0-9_-]+$");
  private final QuickShop plugin;

  public QuickShopTagService(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public @Nullable String normalizeTag(@Nullable String input, final boolean allowSystem) {

    if(input == null) {
      return null;
    }
    input = input.trim().toLowerCase(Locale.ROOT);
    if(input.startsWith("#")) {
      input = input.substring(1);
    }
    final boolean system = input.startsWith("@");
    if(system) {
      if(!allowSystem) {
        return null;
      }
      input = input.substring(1);
    }
    if(input.isEmpty() || input.length() > MAX_TAG_LENGTH || !VALID_TAG_PATTERN.matcher(input).matches()) {
      return null;
    }
    return system? "@" + input : input;
  }

  @Override
  public String displayTag(final String stored) {

    if(stored == null) {
      return "";
    }
    return switch(stored) {
      case SYS_FAV -> "Favorite";
      case SYS_WATCH -> "Watch";
      case SYS_AVOID -> "Avoid";
      default -> "#" + stored;
    };
  }

  @Override
  public CompletableFuture<Boolean> addShopTag(final UUID player, final long shopId, final String tag) {

    return plugin.getDatabaseHelper().tagShop(player, shopId, tag).thenApply(result->result != null && result > 0);
  }

  @Override
  public CompletableFuture<Boolean> removeShopTag(final UUID player, final long shopId, final String tag) {

    return plugin.getDatabaseHelper().removeShopTag(player, shopId, tag).thenApply(result->result != null && result > 0);
  }

  @Override
  public CompletableFuture<Boolean> removeAllShopTags(final long shopId) {

    return plugin.getDatabaseHelper().removeAllShopTags(shopId).thenApply(result->result != null && result > 0);
  }

  @Override
  public CompletableFuture<Boolean> removeAllShopTagsBy(final long shopId, final UUID player) {

    return plugin.getDatabaseHelper().removeShopAllTag(player, shopId).thenApply(result->result != null && result > 0);
  }

  @Override
  public CompletableFuture<Boolean> removeAllPlayerTags(final UUID player) {

    return plugin.getDatabaseHelper().removeAllTagsBy(player).thenApply(result->result != null && result > 0);
  }

  @Override
  public CompletableFuture<Boolean> removeTagFromShops(final UUID player, final String tag) {

    return plugin.getDatabaseHelper().removeTagFromShops(player, tag).thenApply(result->result != null && result > 0);
  }
}
