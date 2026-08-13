package com.ghostchu.quickshop.shop.tag;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.tag.PlayerTagIndex;
import com.ghostchu.quickshop.api.shop.tag.TagManager;
import com.ghostchu.quickshop.api.shop.tag.TagService;
import com.ghostchu.quickshop.api.shop.tag.TaggingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class QuickShopTagManager implements TagManager {

  private final ConcurrentHashMap<UUID, PlayerTagIndex> playerIndexes = new ConcurrentHashMap<>();
  private final QuickShop plugin;
  private final TagService service;

  public QuickShopTagManager(final QuickShop plugin) {

    this.plugin = plugin;
    this.service = new QuickShopTagService(plugin);
  }

  private PlayerTagIndex index(final UUID player) {

    return playerIndexes.computeIfAbsent(player, ignored->new QuickShopPlayerTagIndex());
  }

  @Override
  public void loadAllFromDB() {

    playerIndexes.clear();
    plugin.getDatabaseHelper().loadAllTags();
  }

  @Override
  public TaggingResult addTag(final long shopId, final UUID player, final String tag, final boolean persist) {

    final String normalized = service.normalizeTag(tag, tag != null && tag.startsWith("@"));
    if(shopId < 1 || normalized == null) {
      return TaggingResult.INVALID_TAG;
    }
    final PlayerTagIndex index = index(player);
    if(index.hasTag(shopId, normalized)) {
      return TaggingResult.ALREADY_EXISTS;
    }
    index.addTag(shopId, normalized);
    if(persist) {
      service.addShopTag(player, shopId, normalized).thenAccept(success->{
        if(!success) {
          index.removeTag(shopId, normalized);
          plugin.logger().warn("Failed to persist shop tag {} for shop {}", normalized, shopId);
        }
      }).exceptionally(error->{
        index.removeTag(shopId, normalized);
        plugin.logger().warn("Failed to persist shop tag " + normalized + " for shop " + shopId, error);
        return null;
      });
    }
    return TaggingResult.SUCCESS;
  }

  @Override
  public TaggingResult toggleTag(final long shopId, final UUID player, final String tag) {

    final String normalized = service.normalizeTag(tag, tag != null && tag.startsWith("@"));
    if(normalized == null) {
      return TaggingResult.INVALID_TAG;
    }
    return hasTag(shopId, player, normalized)
            ? removeTag(shopId, player, normalized) : addTag(shopId, player, normalized);
  }

  @Override
  public TaggingResult removeTag(final long shopId, final UUID player, final String tag) {

    final String normalized = service.normalizeTag(tag, tag != null && tag.startsWith("@"));
    if(normalized == null) {
      return TaggingResult.INVALID_TAG;
    }
    final PlayerTagIndex index = playerIndexes.get(player);
    if(index == null || !index.hasTag(shopId, normalized)) {
      return TaggingResult.NOT_FOUND;
    }
    index.removeTag(shopId, normalized);
    cleanup(player, index);
    service.removeShopTag(player, shopId, normalized).thenAccept(success->{
      if(!success) {
        index(player).addTag(shopId, normalized);
        plugin.logger().warn("Failed to remove persisted shop tag {} from shop {}", normalized, shopId);
      }
    }).exceptionally(error->{
      index(player).addTag(shopId, normalized);
      plugin.logger().warn("Failed to remove persisted shop tag " + normalized + " from shop " + shopId, error);
      return null;
    });
    return TaggingResult.SUCCESS;
  }

  @Override
  public boolean removeTag(final UUID player, final String tag) {

    final String normalized = service.normalizeTag(tag, tag != null && tag.startsWith("@"));
    if(normalized == null) {
      return false;
    }
    final PlayerTagIndex index = playerIndexes.get(player);
    boolean removed = false;
    if(index != null) {
      for(final long shopId : new ArrayList<>(index.getShops(normalized))) {
        index.removeTag(shopId, normalized);
        removed = true;
      }
      cleanup(player, index);
    }
    service.removeTagFromShops(player, normalized);
    return removed;
  }

  @Override
  public boolean removeAllShopTags(final long shopId) {

    boolean removed = false;
    for(final var entry : playerIndexes.entrySet()) {
      final PlayerTagIndex index = entry.getValue();
      for(final String tag : new HashSet<>(index.getTags(shopId))) {
        index.removeTag(shopId, tag);
        removed = true;
      }
      cleanup(entry.getKey(), index);
    }
    service.removeAllShopTags(shopId);
    return removed;
  }

  @Override
  public boolean removeAllShopTagsBy(final long shopId, final UUID player) {

    final PlayerTagIndex index = playerIndexes.get(player);
    boolean removed = false;
    if(index != null) {
      for(final String tag : new HashSet<>(index.getTags(shopId))) {
        index.removeTag(shopId, tag);
        removed = true;
      }
      cleanup(player, index);
    }
    service.removeAllShopTagsBy(shopId, player);
    return removed;
  }

  @Override
  public boolean removeAllPlayerTags(final UUID player) {

    final PlayerTagIndex removed = playerIndexes.remove(player);
    service.removeAllPlayerTags(player);
    return removed != null;
  }

  @Override
  public int totalTags() {

    return playerIndexes.values().stream().mapToInt(PlayerTagIndex::totalTags).sum();
  }

  @Override
  public int totalTagsByPlayer(final UUID player) {

    final PlayerTagIndex index = playerIndexes.get(player);
    return index == null? 0 : index.totalTags();
  }

  @Override
  public Set<String> tagsFilteredByShop(final UUID player, final long shopId) {

    final PlayerTagIndex index = playerIndexes.get(player);
    return index == null? Collections.emptySet() : index.getTags(shopId);
  }

  @Override
  public List<Long> shopsFilteredByTag(final UUID player, final String tag) {

    final String normalized = service.normalizeTag(tag, tag != null && tag.startsWith("@"));
    if(normalized == null) {
      return Collections.emptyList();
    }
    final PlayerTagIndex index = playerIndexes.get(player);
    return index == null? Collections.emptyList() : new ArrayList<>(index.getShops(normalized));
  }

  @Override
  public List<Long> shopsFilteredByTags(final UUID player, final List<String> tags) {

    final PlayerTagIndex index = playerIndexes.get(player);
    if(index == null || tags.isEmpty()) {
      return Collections.emptyList();
    }
    Set<Long> result = null;
    for(final String tag : tags) {
      final String normalized = service.normalizeTag(tag, tag != null && tag.startsWith("@"));
      if(normalized == null) {
        return Collections.emptyList();
      }
      if(result == null) {
        result = new HashSet<>(index.getShops(normalized));
      } else {
        result.retainAll(index.getShops(normalized));
      }
      if(result.isEmpty()) {
        return Collections.emptyList();
      }
    }
    return new ArrayList<>(result);
  }

  @Override
  public boolean hasTag(final long shopId, final UUID player, final String tag) {

    final String normalized = service.normalizeTag(tag, tag != null && tag.startsWith("@"));
    if(normalized == null) {
      return false;
    }
    final PlayerTagIndex index = playerIndexes.get(player);
    return index != null && index.hasTag(shopId, normalized);
  }

  @Override
  public TagService service() {

    return service;
  }

  private void cleanup(final UUID player, final PlayerTagIndex index) {

    if(index.totalTags() == 0) {
      playerIndexes.remove(player, index);
    }
  }
}
