package com.ghostchu.quickshop.compatibility.ketting;

import com.ghostchu.quickshop.api.inventory.BlockInventoryWrapperManager;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.serialize.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.logging.Logger;

final class KettingContainerInventoryManager implements BlockInventoryWrapperManager {

  private final KettingContainerBridge bridge;

  KettingContainerInventoryManager(@NotNull final Logger logger) {

    this.bridge = new KettingContainerBridge(logger);
  }

  @Override
  public @Nullable InventoryWrapper resolve(@NotNull final Block block) {

    final BlockState state = block.getState();
    if(state instanceof InventoryHolder) {
      return null;
    }
    final Optional<KettingContainerBridge.ResolvedContainer> resolved = bridge.resolve(state);
    return resolved.map(container->new KettingContainerInventoryWrapper(
            this,
            bridge,
            block.getLocation(),
            block.getType(),
            container.inventory(),
            container.handle())).orElse(null);
  }

  @Override
  public @NotNull InventoryWrapper locate(@NotNull final String symbolLink) throws IllegalArgumentException {

    final BlockPos blockPos = BlockPos.deserialize(symbolLink);
    final World world = Bukkit.getWorld(blockPos.getWorld());
    if(world == null) {
      throw new IllegalArgumentException("Invalid symbol link: Invalid world name.");
    }
    final InventoryWrapper wrapper = resolve(world.getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    if(wrapper == null) {
      throw new IllegalArgumentException("Invalid symbol link: Target block is not a supported NMS Container.");
    }
    return wrapper;
  }

  @Override
  public @NotNull String mklink(@NotNull final InventoryWrapper wrapper) throws IllegalArgumentException {

    final Location location = wrapper.getLocation();
    if(!(wrapper instanceof KettingContainerInventoryWrapper) || location == null) {
      throw new IllegalArgumentException("Target is not a Ketting block inventory.");
    }
    return new BlockPos(location).serialize();
  }
}
