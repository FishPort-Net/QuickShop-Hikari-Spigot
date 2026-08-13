package com.ghostchu.quickshop.api.shop.tag;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** In-memory tag index backed by the QuickShop database. */
public interface TagManager {

  void loadAllFromDB();

  default TaggingResult addTag(final long shopId, final UUID player, final String tag) {
    return addTag(shopId, player, tag, true);
  }

  TaggingResult addTag(long shopId, UUID player, String tag, boolean persist);

  TaggingResult toggleTag(long shopId, UUID player, String tag);

  TaggingResult removeTag(long shopId, UUID player, String tag);

  boolean removeTag(UUID player, String tag);

  boolean removeAllShopTags(long shopId);

  boolean removeAllShopTagsBy(long shopId, UUID player);

  boolean removeAllPlayerTags(UUID player);

  int totalTags();

  int totalTagsByPlayer(UUID player);

  Set<String> tagsFilteredByShop(UUID player, long shopId);

  List<Long> shopsFilteredByTag(UUID player, String tag);

  List<Long> shopsFilteredByTags(UUID player, List<String> tags);

  boolean hasTag(long shopId, UUID player, String tag);

  TagService service();
}
