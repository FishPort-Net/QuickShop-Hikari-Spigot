package com.ghostchu.quickshop.api.database.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ShopRecord {

  private DataRecord dataRecord;
  private InfoRecord infoRecord;
  private int cachedStock;
  private int cachedSpace;
  private boolean inventoryCacheInitialized;

  /** Preserves the 6.2 constructor for addons that create records without an inventory cache. */
  public ShopRecord(final DataRecord dataRecord, final InfoRecord infoRecord) {

    this(dataRecord, infoRecord, -2, -2, false);
  }
}
