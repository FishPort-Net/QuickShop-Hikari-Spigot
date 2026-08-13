package com.ghostchu.quickshop.compatibility.ketting;

import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperIterator;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;

final class KettingContainerInventoryWrapper implements InventoryWrapper {

  private final KettingContainerBridge bridge;
  private final Object containerIdentity;
  private final Inventory inventory;
  private final Location location;
  private final KettingContainerInventoryManager manager;
  private final Material material;

  KettingContainerInventoryWrapper(
          @NotNull final KettingContainerInventoryManager manager,
          @NotNull final KettingContainerBridge bridge,
          @NotNull final Location location,
          @NotNull final Material material,
          @NotNull final Inventory inventory,
          @NotNull final Object containerIdentity) {

    this.manager = manager;
    this.bridge = bridge;
    this.location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    this.material = material;
    this.inventory = inventory;
    this.containerIdentity = containerIdentity;
  }

  @Override
  public @NotNull InventoryWrapperIterator iterator() {

    final int size = inventory.getSize();
    return new InventoryWrapperIterator() {
      private int currentIndex;
      private int lastIndex = -1;

      @Override
      public boolean hasNext() {

        return currentIndex < size;
      }

      @Override
      public @Nullable ItemStack next() {

        if(!hasNext()) {
          throw new NoSuchElementException();
        }
        lastIndex = currentIndex++;
        return inventory.getItem(lastIndex);
      }

      @Override
      public void setCurrent(@Nullable final ItemStack stack) {

        if(lastIndex < 0) {
          throw new IllegalStateException("next() must be called before setCurrent()");
        }
        inventory.setItem(lastIndex, stack);
      }
    };
  }

  @Override
  public void clear() {

    for(int slot = 0; slot < inventory.getSize(); slot++) {
      inventory.setItem(slot, null);
    }
  }

  @Override
  public @NotNull ItemStack[] createSnapshot() {

    final ItemStack[] snapshot = new ItemStack[inventory.getSize()];
    for(int slot = 0; slot < snapshot.length; slot++) {
      final ItemStack item = inventory.getItem(slot);
      snapshot[slot] = item == null? null : item.clone();
    }
    return snapshot;
  }

  @Override
  public @NotNull InventoryWrapperManager getWrapperManager() {

    return manager;
  }

  @Override
  public @Nullable InventoryHolder getHolder() {

    return null;
  }

  @Override
  public @NotNull InventoryWrapperType getInventoryType() {

    return InventoryWrapperType.BUKKIT;
  }

  @Override
  public @NotNull Location getLocation() {

    return location.clone();
  }

  @Override
  public boolean isValid() {

    final Block block = location.getBlock();
    if(block.getType() != material) {
      return false;
    }
    final Optional<KettingContainerBridge.ResolvedContainer> resolved = bridge.resolve(block.getState());
    return resolved.isPresent() && resolved.get().handle() == containerIdentity;
  }

  @Override
  public boolean isNeedUpdate() {

    return !isValid();
  }

  @Override
  public boolean restoreSnapshot(@NotNull final ItemStack[] snapshot) {

    if(snapshot.length != inventory.getSize()) {
      return false;
    }
    for(int slot = 0; slot < snapshot.length; slot++) {
      final ItemStack item = snapshot[slot];
      inventory.setItem(slot, item == null? null : item.clone());
    }
    return true;
  }

  @Override
  public void setContents(final ItemStack[] itemStacks) {

    if(itemStacks.length > inventory.getSize()) {
      throw new IllegalArgumentException("Invalid inventory size (" + itemStacks.length + "); expected " + inventory.getSize() + " or less");
    }
    for(int slot = 0; slot < inventory.getSize(); slot++) {
      final ItemStack item = slot < itemStacks.length? itemStacks[slot] : null;
      inventory.setItem(slot, item);
    }
  }
}
