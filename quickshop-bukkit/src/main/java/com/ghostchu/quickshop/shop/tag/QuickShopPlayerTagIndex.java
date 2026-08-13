package com.ghostchu.quickshop.shop.tag;

import com.ghostchu.quickshop.api.shop.tag.PlayerTagIndex;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class QuickShopPlayerTagIndex implements PlayerTagIndex {

  private final Map<Long, Set<String>> shopToTags = new ConcurrentHashMap<>();
  private final Map<String, Set<Long>> tagToShops = new ConcurrentHashMap<>();

  @Override
  public void addTag(final long shopId, final String tag) {

    shopToTags.computeIfAbsent(shopId, ignored->ConcurrentHashMap.newKeySet()).add(tag);
    tagToShops.computeIfAbsent(tag, ignored->ConcurrentHashMap.newKeySet()).add(shopId);
  }

  @Override
  public void removeTag(final long shopId, final String tag) {

    final Set<String> tags = shopToTags.get(shopId);
    if(tags != null) {
      tags.remove(tag);
      if(tags.isEmpty()) {
        shopToTags.remove(shopId, tags);
      }
    }
    final Set<Long> shops = tagToShops.get(tag);
    if(shops != null) {
      shops.remove(shopId);
      if(shops.isEmpty()) {
        tagToShops.remove(tag, shops);
      }
    }
  }

  @Override
  public Set<String> getTags(final long shopId) {

    return Collections.unmodifiableSet(shopToTags.getOrDefault(shopId, Collections.emptySet()));
  }

  @Override
  public Set<Long> getShops(final String tag) {

    return Collections.unmodifiableSet(tagToShops.getOrDefault(tag, Collections.emptySet()));
  }

  @Override
  public Set<Long> shops() {

    return Collections.unmodifiableSet(shopToTags.keySet());
  }

  @Override
  public int totalTags() {

    return shopToTags.values().stream().mapToInt(Set::size).sum();
  }

  @Override
  public boolean hasTag(final long shopId, final String tag) {

    final Set<String> tags = shopToTags.get(shopId);
    return tags != null && tags.contains(tag);
  }

  @Override
  public void clear() {

    shopToTags.clear();
    tagToShops.clear();
  }
}
