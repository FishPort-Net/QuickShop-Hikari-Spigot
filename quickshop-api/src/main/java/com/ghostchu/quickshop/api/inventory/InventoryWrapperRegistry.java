package com.ghostchu.quickshop.api.inventory;

import com.google.common.collect.MapMaker;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class InventoryWrapperRegistry {

  private final Map<String, InventoryWrapperManager> registry = new MapMaker().makeMap();
  private final List<String> registrationOrder = new CopyOnWriteArrayList<>();

  @Nullable
  public String find(final InventoryWrapperManager manager) {

    for(final Map.Entry<String, InventoryWrapperManager> entry : registry.entrySet()) {
      if(entry.getValue() == manager || entry.getValue().equals(manager)) {
        return entry.getKey();
      }
    }
    return null;
  }

  @Nullable
  public InventoryWrapperManager get(final String pluginName) {

    return registry.get(pluginName);
  }

  /**
   * Resolves a block inventory using registered block inventory providers. Providers are queried in
   * registration order, which keeps QuickShop's built-in Bukkit inventory handling as the primary
   * path and compatibility providers as fallbacks.
   *
   * @param block block to inspect
   *
   * @return a live inventory wrapper, or {@code null} when no provider supports the block
   */
  @Nullable
  public InventoryWrapper resolve(@NotNull final Block block) {

    for(final String providerName : registrationOrder) {
      final InventoryWrapperManager manager = registry.get(providerName);
      if(manager instanceof final BlockInventoryWrapperManager blockManager) {
        final InventoryWrapper wrapper = blockManager.resolve(block);
        if(wrapper != null) {
          return wrapper;
        }
      }
    }
    return null;
  }

  public void register(@NotNull final Plugin plugin, @NotNull final InventoryWrapperManager manager) {

    if(registry.containsKey(plugin.getName())) {
      plugin.getLogger().warning("Nag Author: Plugin " + plugin.getName() + " already have a registered InventoryWrapperManager: "
                                 + registry.get(plugin.getName()).getClass().getName() +
                                 " but trying register another new manager: " + manager.getClass().getName() +
                                 ". This may cause unexpected behavior! Replacing with new instance...");
    } else {
      registrationOrder.add(plugin.getName());
    }
    registry.put(plugin.getName(), manager);
  }

  public void unregister(@NotNull final Plugin plugin) {

    registry.remove(plugin.getName());
    registrationOrder.remove(plugin.getName());
  }

}
