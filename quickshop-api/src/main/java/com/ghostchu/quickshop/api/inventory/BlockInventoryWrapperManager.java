package com.ghostchu.quickshop.api.inventory;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An inventory wrapper manager that can resolve inventories backed by blocks.
 *
 * <p>Implementations must return {@code null} for unsupported blocks. Providers should fail softly
 * when their target platform is not present so they can be installed without changing the core
 * plugin's platform compatibility.</p>
 */
public interface BlockInventoryWrapperManager extends InventoryWrapperManager {

  /**
   * Resolves the live inventory represented by a block.
   *
   * @param block block to inspect
   *
   * @return a live inventory wrapper, or {@code null} when this provider does not support the block
   */
  @Nullable
  InventoryWrapper resolve(@NotNull Block block);
}
