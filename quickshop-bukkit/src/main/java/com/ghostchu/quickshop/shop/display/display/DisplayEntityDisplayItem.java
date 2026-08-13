package com.ghostchu.quickshop.shop.display.display;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.display.DisplayApplicableCheckEvent;
import com.ghostchu.quickshop.api.event.display.ShopDisplayItemSpawnEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/** Spigot 1.20.1 implementation backed by ItemDisplay and optional TextDisplay entities. */
public final class DisplayEntityDisplayItem extends AbstractDisplayItem {

  private final DisplayEntityItemManager manager;
  private final ShopChunk chunk;
  private ItemDisplay itemDisplay;
  private TextDisplay textDisplay;
  private boolean spawned;

  DisplayEntityDisplayItem(final DisplayEntityItemManager manager, final Shop shop, final ShopChunk chunk) {

    super(shop);
    this.manager = manager;
    this.chunk = chunk;
  }

  @Override
  public boolean checkDisplayIsMoved() {

    return itemDisplay != null && itemDisplay.isValid()
           && itemDisplay.getLocation().distanceSquared(getDisplayLocation()) > 0.04D;
  }

  @Override
  public boolean checkDisplayNeedRegen() {

    return itemDisplay == null || !itemDisplay.isValid();
  }

  @Override
  public boolean checkIsShopEntity(final Entity entity) {

    return entity != null && (entity.equals(itemDisplay) || entity.equals(textDisplay));
  }

  @Override
  public void fixDisplayMoved() {

    respawn();
  }

  @Override
  public void fixDisplayNeedRegen() {

    respawn();
  }

  @Override
  public @Nullable Entity getDisplay() {

    return itemDisplay;
  }

  @Override
  public boolean isSpawned() {

    return spawned;
  }

  @Override
  public boolean isApplicableForPlayer(final Player player) {

    final DisplayApplicableCheckEvent event = new DisplayApplicableCheckEvent(shop, player.getUniqueId());
    event.setApplicable(true);
    event.callEvent();
    return event.isApplicable();
  }

  @Override
  public void remove(final boolean dontTouchWorld) {

    if(!dontTouchWorld) {
      if(itemDisplay != null) itemDisplay.remove();
      if(textDisplay != null) textDisplay.remove();
    }
    itemDisplay = null;
    textDisplay = null;
    spawned = false;
    manager.remove(chunk, shop.getLocation().hashCode(), this);
  }

  @Override
  public boolean removeDupe() {

    return false;
  }

  @Override
  public void respawn() {

    remove(false);
    spawn();
  }

  @Override
  public void safeGuard(@NotNull final Entity entity) {

    entity.setPersistent(false);
    entity.setInvulnerable(true);
  }

  @Override
  public void spawn() {

    Util.ensureThread(false);
    if(spawned || !shop.isLoaded() || getDisplayLocation() == null) {
      return;
    }
    if(new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.DISPLAY_ENTITY).callCancellableEvent()) {
      Log.debug("A plugin cancelled display entity spawning for shop " + shop.getShopId());
      return;
    }

    final QuickShop plugin = QuickShop.getInstance();
    final Location itemLocation = getDisplayLocation();
    itemDisplay = itemLocation.getWorld().spawn(itemLocation, ItemDisplay.class);
    itemDisplay.setItemStack(originalItemStack);
    itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
    itemDisplay.setTransformation(new Transformation(new Vector3f(), rotation(plugin),
            new Vector3f((float)plugin.getConfig().getDouble("shop.display-scale.x", 1.25D),
                         (float)plugin.getConfig().getDouble("shop.display-scale.y", 1.25D),
                         (float)plugin.getConfig().getDouble("shop.display-scale.z", 1.25D)),
            new AxisAngle4f()));
    itemDisplay.setVisibleByDefault(false);
    safeGuard(itemDisplay);

    if(plugin.getConfig().getBoolean("shop.text-display.enabled", false)) {
      final Location textLocation = itemLocation.clone().add(0,
              plugin.getConfig().getDouble("shop.text-display.y-offset", 0.8D), 0);
      textDisplay = textLocation.getWorld().spawn(textLocation, TextDisplay.class);
      textDisplay.setText(LegacyComponentSerializer.legacySection().serialize(DisplayTextRenderer.render(shop)));
      textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
      textDisplay.setBillboard(Display.Billboard.CENTER);
      textDisplay.setLineWidth(plugin.getConfig().getInt("shop.text-display.line-width", 200));
      textDisplay.setShadowed(plugin.getConfig().getBoolean("shop.text-display.shadow.enabled", true));
      textDisplay.setSeeThrough(plugin.getConfig().getBoolean("shop.text-display.see-through", false));
      textDisplay.setTextOpacity((byte)plugin.getConfig().getInt("shop.text-display.text-opacity", -1));
      textDisplay.setViewRange(Math.max(0.05F,
              plugin.getConfig().getInt("shop.text-display.range-blocks", 3) / 64.0F));
      textDisplay.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(),
              new Vector3f((float)plugin.getConfig().getDouble("shop.text-display.scale.x", 0.5D),
                           (float)plugin.getConfig().getDouble("shop.text-display.scale.y", 0.5D),
                           (float)plugin.getConfig().getDouble("shop.text-display.scale.z", 0.5D)),
              new AxisAngle4f()));
      textDisplay.setVisibleByDefault(false);
      safeGuard(textDisplay);
    }

    spawned = true;
    manager.track(chunk, shop.getLocation().hashCode(), this);
    Bukkit.getOnlinePlayers().forEach(this::showTo);
  }

  public void showTo(final Player player) {

    if(!spawned || !player.isOnline() || !player.getWorld().equals(shop.getLocation().getWorld())
       || !isApplicableForPlayer(player)) {
      return;
    }
    if(itemDisplay != null && itemDisplay.isValid()) {
      player.showEntity(QuickShop.getInstance().getJavaPlugin(), itemDisplay);
    }
    if(textDisplay != null && textDisplay.isValid()) {
      player.showEntity(QuickShop.getInstance().getJavaPlugin(), textDisplay);
    }
  }

  private AxisAngle4f rotation(final QuickShop plugin) {

    float yaw = 0F;
    final BlockData data = shop.getLocation().getBlock().getBlockData();
    if(data instanceof final Directional directional) {
      yaw = switch(directional.getFacing()) {
        case WEST -> 90F;
        case NORTH -> 180F;
        case EAST -> 270F;
        default -> 0F;
      };
    }
    return new AxisAngle4f((float)Math.toRadians(
            (float)plugin.getConfig().getDouble("shop.display-rotation.degrees", 0D) + yaw),
            (float)plugin.getConfig().getDouble("shop.display-rotation.x", 0D),
            (float)plugin.getConfig().getDouble("shop.display-rotation.y", 1D),
            (float)plugin.getConfig().getDouble("shop.display-rotation.z", 0D));
  }
}
