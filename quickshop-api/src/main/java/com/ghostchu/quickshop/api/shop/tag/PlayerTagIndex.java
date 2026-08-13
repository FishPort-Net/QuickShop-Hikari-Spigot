package com.ghostchu.quickshop.api.shop.tag;

import java.util.Set;

/** Per-player bidirectional index of shops and tags. */
public interface PlayerTagIndex {

  void addTag(long shopId, String tag);

  void removeTag(long shopId, String tag);

  Set<String> getTags(long shopId);

  Set<Long> getShops(String tag);

  Set<Long> shops();

  int totalTags();

  boolean hasTag(long shopId, String tag);

  void clear();
}
