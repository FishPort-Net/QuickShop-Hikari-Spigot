package com.ghostchu.quickshop;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.Library;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/** Loads the pre-relocated dependencies kept outside the plugin JAR. */
final class RuntimeLibraryLoader {

  private static final String METADATA = "quickshop-runtime.properties";
  private static final String RUNTIME_URL_PROPERTY = "com.ghostchu.quickshop.runtime.url";
  private static final List<String> REQUIRED_CLASSES = List.of(
          "com.ghostchu.quickshop.shade.cc.carm.lib.easysql.hikari.HikariDataSource",
          "com.ghostchu.quickshop.shade.com.google.common.cache.CacheBuilder",
          "com.ghostchu.quickshop.shade.com.mysql.cj.jdbc.Driver",
          "com.ghostchu.quickshop.shade.kong.unirest.Unirest",
          "com.ghostchu.quickshop.shade.org.h2.Driver"
  );

  private final QuickShopBukkit plugin;

  RuntimeLibraryLoader(@NotNull final QuickShopBukkit plugin) {

    this.plugin = plugin;
  }

  void load() throws IOException, ClassNotFoundException {

    final Properties metadata = loadMetadata();
    final String version = metadata.getProperty("version");
    final Library runtime = Library.builder()
            .groupId("com{}ghostchu{}quickshop")
            .artifactId("quickshop-runtime")
            .version(version)
            .url(System.getProperty(RUNTIME_URL_PROPERTY, metadata.getProperty("url")))
            .checksumFromBase64(metadata.getProperty("sha256-base64"))
            .build();
    new BukkitLibraryManager(plugin, "libs").loadLibrary(runtime);

    final ClassLoader classLoader = plugin.getClass().getClassLoader();
    for(final String requiredClass : REQUIRED_CLASSES) {
      Class.forName(requiredClass, false, classLoader);
    }
    plugin.getBootstrapLogger().info("Loaded private QuickShop runtime " + version);
  }

  @NotNull
  private Properties loadMetadata() throws IOException {

    try(InputStream input = plugin.getResource(METADATA)) {
      if(input == null) {
        throw new IOException("Missing " + METADATA);
      }
      final Properties metadata = new Properties();
      metadata.load(input);
      return metadata;
    }
  }
}
