package com.ghostchu.quickshop.util.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class QuickShopPreviewGUIHolder implements InventoryHolder {

  private Inventory inventory;

  @Override
  public @NotNull Inventory getInventory() {

    if(this.inventory == null) {
      throw new IllegalStateException("Preview inventory has not been initialized");
    }
    return this.inventory;
  }

  public void setInventory(@NotNull final Inventory inventory) {

    if(this.inventory != null) {
      throw new IllegalStateException("Preview inventory has already been initialized");
    }
    this.inventory = inventory;
  }

}
