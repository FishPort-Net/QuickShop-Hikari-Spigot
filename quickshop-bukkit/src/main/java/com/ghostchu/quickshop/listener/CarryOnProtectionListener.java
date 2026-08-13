package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/** Prevents Carry On from moving shop containers and signs without deleting the shop. */
public class CarryOnProtectionListener extends AbstractProtectionListener {

  private boolean carryOnInstalled;
  private Method getCarryData;
  private Method isKeyPressed;

  public CarryOnProtectionListener(@NotNull final QuickShop plugin) {

    super(plugin);
    detectCarryOn();
  }

  @Override
  public void register() {

    if(carryOnInstalled) {
      super.register();
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInteract(final PlayerInteractEvent event) {

    final Block block = event.getClickedBlock();
    if(event.getAction() != Action.RIGHT_CLICK_BLOCK || block == null || !isCarryGesture(event.getPlayer()) || !isShopBlock(block)) {
      return;
    }
    event.setCancelled(true);
    event.setUseInteractedBlock(Event.Result.DENY);
    event.setUseItemInHand(Event.Result.DENY);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onBreak(final BlockBreakEvent event) {

    if(isCarryGesture(event.getPlayer()) && isShopBlock(event.getBlock())) {
      event.setCancelled(true);
    }
  }

  private boolean isShopBlock(@NotNull final Block block) {

    if(plugin.getShopManager().getShopIncludeAttached(block.getLocation()) != null) {
      return true;
    }
    final Block attached = Util.isWallSign(block.getType())? Util.getAttached(block) : null;
    return attached != null && plugin.getShopManager().getShopIncludeAttached(attached.getLocation()) != null;
  }

  private boolean isCarryGesture(@NotNull final Player player) {

    if(!player.getInventory().getItemInMainHand().getType().isAir()
       || !player.getInventory().getItemInOffHand().getType().isAir()) {
      return false;
    }
    if(getCarryData == null || isKeyPressed == null) {
      return player.isSneaking();
    }
    try {
      final Object handle = player.getClass().getMethod("getHandle").invoke(player);
      final Object carryData = getCarryData.invoke(null, handle);
      return (boolean)isKeyPressed.invoke(carryData);
    } catch(final ReflectiveOperationException | RuntimeException e) {
      return player.isSneaking();
    }
  }

  private void detectCarryOn() {

    final ClassLoader classLoader = plugin.getJavaPlugin().getServer().getClass().getClassLoader();
    try {
      final Class<?> dataManager = Class.forName("tschipp.carryon.common.carry.CarryOnDataManager", false, classLoader);
      carryOnInstalled = true;
      final Class<?> carryData = Class.forName("tschipp.carryon.common.carry.CarryOnData", false, classLoader);
      for(final Method method : dataManager.getMethods()) {
        if(method.getName().equals("getCarryData") && method.getParameterCount() == 1) {
          getCarryData = method;
          break;
        }
      }
      if(getCarryData == null) {
        throw new NoSuchMethodException("CarryOnDataManager#getCarryData");
      }
      isKeyPressed = carryData.getMethod("isKeyPressed");
    } catch(final ClassNotFoundException e) {
      carryOnInstalled = false;
    } catch(final ReflectiveOperationException | LinkageError e) {
      carryOnInstalled = true;
      plugin.logger().warn("Carry On detected, but its key state is unavailable; using the sneaking fallback.");
    }
  }
}
