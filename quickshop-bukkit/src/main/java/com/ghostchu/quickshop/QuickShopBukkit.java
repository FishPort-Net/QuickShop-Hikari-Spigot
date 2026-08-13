package com.ghostchu.quickshop;

import com.ghostchu.quickshop.platform.Platform;
import com.ghostchu.quickshop.platform.spigot.AbstractSpigotPlatform;
import com.ghostchu.quickshop.util.PackageUtil;
import com.vdurmont.semver4j.Semver;
import io.papermc.lib.PaperLib;
import kong.unirest.Unirest;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.logging.Level;

public class QuickShopBukkit extends JavaPlugin {

  @Getter
  private final java.util.logging.Logger bootstrapLogger = java.util.logging.Logger.getLogger("QuickShop-Hikari/Bootstrap");
  private Platform platform;
  private Logger logger;
  private QuickShop quickShop;
  private Throwable abortLoading;

  @Override
  public void reloadConfig() {

    super.reloadConfig();
    this.quickShop.reloadConfigSubModule();
  }

  @Override
  public void onLoad() {

    final long startLoadAt = System.currentTimeMillis();
    try {
      bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> Execute the initialization sequence");
      bootstrapLogger.info("Bootloader preparing for startup, please wait...");
      bootstrapLogger.info("Initializing private runtime libraries...");
      loadLibraries();
      bootstrapLogger.info("Initializing platform...");
      loadPlatform();
      bootstrapLogger.info("Boot QuickShop instance...");
      initQuickShop();
      // SLF4J now should available
      bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> Complete (" + (System.currentTimeMillis() - startLoadAt) + "ms). Waiting for enable...");
    } catch(final Throwable e) {
      bootstrapLogger.log(Level.SEVERE, "Failed to startup the QuickShop-Hikari due unexpected exception!", e);
      Bukkit.getPluginManager().disablePlugin(this);
      abortLoading = e;
      throw new IllegalStateException("Boot failure", e);
    }
  }

  @Override
  public void onDisable() {

    final long shutdownAtTime = System.currentTimeMillis();
    bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> Execute the shutdown sequence");
    this.quickShop.onDisable();
    bootstrapLogger.info("Cleaning up resources...");
    HandlerList.unregisterAll(this);
    QuickShop.folia().getScheduler().cancelAllTasks();
    Bukkit.getServicesManager().unregisterAll(this);
    Unirest.shutDown(true);
    Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
    this.platform.shutdown();
    bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> All Complete (" + (System.currentTimeMillis() - shutdownAtTime) + "ms)");
  }

  @Override
  public void onEnable() {

    if(abortLoading != null) {
      throw new IllegalStateException("Plugin is disabled due an loading error", abortLoading);
    }
    final long enableAtTime = System.currentTimeMillis();
    bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> Execute the enable sequence");
    this.quickShop.onEnable();
    bootstrapLogger.info("QuickShop-" + getFork() + " - Bootstrap -> All Complete. (" + (System.currentTimeMillis() - enableAtTime) + "ms)");
  }

  /**
   * Return the QuickShop fork name.
   *
   * @return The fork name.
   */
  @NotNull
  public String getFork() {

    return "Hikari";
  }

  private void loadLibraries() throws IOException, ClassNotFoundException {

    new RuntimeLibraryLoader(this).load();
    new UnirestLibLoader(this);
  }


  private void loadPlatform() throws Exception {

    int platformId = 0;
    if(PaperLib.isSpigot()) {
      platformId = 1;
    }
    if(PaperLib.isPaper()) {
      platformId = 2;
    }

    platformId = PackageUtil.parsePackageProperly("forcePlatform").asInteger(platformId);
    try {
      switch(platformId) {
        case 1 -> {
          bootstrapLogger.info("Platform detected: Spigot");
          bootstrapLogger.warning("=================================================================");
          bootstrapLogger.warning("=========================   ATTENTION   =========================");
          bootstrapLogger.warning("=================================================================");
          bootstrapLogger.warning("Use Paper or Paper's fork to get best performance and enhanced features!");
          bootstrapLogger.warning("Spigot lacks modern functionality and overall performance fixes.");
          bootstrapLogger.warning("=================================================================");

          initNbtApi();

          //noinspection deprecation
          final String internalNMSVersion = AbstractSpigotPlatform.getNMSVersion();
          this.platform = switch(internalNMSVersion) {
            case "v1_20_R1" -> createSpigotPlatform("com.ghostchu.quickshop.platform.spigot.v1_20_1.Spigot1201Platform");
            case "v1_20_R2" -> createSpigotPlatform("com.ghostchu.quickshop.platform.spigot.v1_20_2.Spigot1202Platform");
            case "v1_20_R3" -> createSpigotPlatform("com.ghostchu.quickshop.platform.spigot.v1_20_3.Spigot1203Platform");
            case "v1_20_R4" -> createSpigotPlatform("com.ghostchu.quickshop.platform.spigot.v1_20_4.Spigot1205Platform");
            case "v1_21_R1" -> createSpigotPlatform("com.ghostchu.quickshop.platform.spigot.v1_21_1.Spigot1210Platform");
            case "v1_21_R2" -> createSpigotPlatform("com.ghostchu.quickshop.platform.spigot.v1_21_3.Spigot1231Platform");
            case "v1_21_R3" -> createSpigotPlatform("com.ghostchu.quickshop.platform.spigot.v1_21_4.Spigot1214Platform");
            case "v1_21_R4" -> createSpigotPlatform("com.ghostchu.quickshop.platform.spigot.v1_21_5.Spigot1215Platform");
            default -> {
              bootstrapLogger.warning("This Spigot build does not support server revision " + internalNMSVersion + ".");
              Bukkit.getPluginManager().disablePlugin(this);
              throw new IllegalStateException("Unsupported Spigot server revision: " + internalNMSVersion);
            }
          };
        }
        case 2 -> throw new UnsupportedOperationException("This release targets Spigot; the Paper platform is not bundled.");
        default -> throw new UnsupportedOperationException("Unsupported server platform");
      }
      try {
        this.logger = this.platform.getSlf4jLogger(this);
      } catch(final Throwable th) {
        this.logger = LoggerFactory.getLogger(getDescription().getName());
      }
      logger.info("Slf4jLogger initialized");
      bootstrapLogger.info("Platform initialized: " + this.platform.getClass().getName());
    } catch(final Throwable e) {
      throw new Exception("Failed to initialize the platform", e);
    }
  }

  private Platform createSpigotPlatform(final String className) throws ReflectiveOperationException {

    return Class.forName(className, true, getClassLoader())
            .asSubclass(Platform.class)
            .getConstructor(org.bukkit.plugin.Plugin.class)
            .newInstance(this);
  }

  private void initNbtApi() {

    new NbtApiInitializer(bootstrapLogger);
  }

  private void initQuickShop() {

    bootstrapLogger.info("Creating QuickShop instance...");
    this.quickShop = new QuickShop(this, logger, platform);
    this.quickShop.onLoad();
  }

  @NotNull
  public Logger logger() {

    return this.logger;
  }

  @NotNull
  public Platform platform() {

    return this.platform;
  }

  /**
   * Returns QS version, this method only exist on QuickShop forks If running other QuickShop forks,
   * result may not is "Reremake x.x.x" If running QS official, Will throw exception.
   *
   * @return Plugin Version
   */
  @NotNull
  public String getVersion() {

    return getDescription().getVersion();
  }

  @NotNull
  public Semver getSemVersion() {

    try {
      return new Semver(getDescription().getVersion());
    } catch(final Exception e) {
      return new Semver("0.0.0.0");
    }
  }

  public QuickShop getQuickShop() {

    return quickShop;
  }

  static class UnirestLibLoader {

    public UnirestLibLoader(final QuickShopBukkit plugin) {

      plugin.getBootstrapLogger().info("Initialing Unirest...");
      Unirest.config()
              .concurrency(10, 5)
              .setDefaultHeader("User-Agent", "QuickShop/" + plugin.getFork() + "-" + plugin.getDescription().getVersion() + " Java/" + System.getProperty("java.version"));
      Unirest.config().verifySsl(PackageUtil.parsePackageProperly("verifySSL").asBoolean());
      if(PackageUtil.parsePackageProperly("proxyHost").isPresent()) {
        plugin.getBootstrapLogger().info("Unirest proxy feature has been enabled.");
        Unirest.config().proxy(PackageUtil.parsePackageProperly("proxyHost").asString("127.0.0.1"), PackageUtil.parsePackageProperly("proxyPort").asInteger(1080));
      }
      if(PackageUtil.parsePackageProperly("proxyUsername").isPresent()) {
        plugin.getBootstrapLogger().info("Unirest proxy authentication activated.");
        Unirest.config().proxy(PackageUtil.parsePackageProperly("proxyHost").asString("127.0.0.1"), PackageUtil.parsePackageProperly("proxyPort").asInteger(1080), PackageUtil.parsePackageProperly("proxyUsername").asString(""), PackageUtil.parsePackageProperly("proxyPassword").asString(""));
      }
    }
  }
}
