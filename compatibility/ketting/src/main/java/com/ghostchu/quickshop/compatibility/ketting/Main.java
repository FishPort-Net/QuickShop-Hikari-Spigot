package com.ghostchu.quickshop.compatibility.ketting;

import com.ghostchu.quickshop.api.QuickShopAPI;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

  private QuickShopAPI api;
  private KettingContainerInventoryManager manager;

  @Override
  public void onLoad() {

    this.api = QuickShopAPI.getInstance();
    this.manager = new KettingContainerInventoryManager(getLogger());
    this.api.getInventoryWrapperRegistry().register(this, manager);
  }

  @Override
  public void onEnable() {

    getLogger().info("Generic Ketting container bridge enabled.");
  }

  @Override
  public void onDisable() {

    if(this.api != null) {
      this.api.getInventoryWrapperRegistry().unregister(this);
    }
  }
}
