package com.ghostchu.quickshop;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/** Downloads, verifies and relocates QuickShop's private runtime dependencies. */
final class RuntimeLibraryLoader {

  private static final String MANIFEST = "quickshop-libraries.properties";
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

    final Properties manifest = loadManifest();
    if(!"1".equals(required(manifest, "format"))) {
      throw new IOException("Unsupported " + MANIFEST + " format");
    }

    final List<Relocation> relocations = loadRelocations(manifest);
    final BukkitLibraryManager libraryManager = new BukkitLibraryManager(plugin, "libs");
    // Libby uses Maven Central only for its isolated relocation toolchain.
    libraryManager.addMavenCentral();
    final Path cacheDirectory = plugin.getDataFolder().toPath().resolve("libs");
    final int libraryCount = requiredInt(manifest, "library.count");
    for(int i = 0; i < libraryCount; i++) {
      final String prefix = "library." + i + ".";
      final Library.Builder builder = Library.builder()
              .groupId(required(manifest, prefix + "group"))
              .artifactId(required(manifest, prefix + "artifact"))
              .version(required(manifest, prefix + "version"))
              .url(required(manifest, prefix + "url"))
              .checksumFromBase64(required(manifest, prefix + "sha256-base64"));
      relocations.forEach(builder::relocate);
      loadLibrary(libraryManager, cacheDirectory, builder.build());
    }

    final ClassLoader classLoader = plugin.getClass().getClassLoader();
    for(final String requiredClass : REQUIRED_CLASSES) {
      Class.forName(requiredClass, false, classLoader);
    }
    plugin.getBootstrapLogger().info("Loaded " + libraryCount + " isolated QuickShop runtime libraries");
  }

  @NotNull
  private Properties loadManifest() throws IOException {

    try(InputStream input = plugin.getResource(MANIFEST)) {
      if(input == null) {
        throw new IOException("Missing " + MANIFEST);
      }
      final Properties manifest = new Properties();
      manifest.load(input);
      return manifest;
    }
  }

  @NotNull
  private List<Relocation> loadRelocations(@NotNull final Properties manifest) throws IOException {

    final int count = requiredInt(manifest, "relocation.count");
    final List<Relocation> relocations = new ArrayList<>(count);
    for(int i = 0; i < count; i++) {
      final String prefix = "relocation." + i + ".";
      relocations.add(new Relocation(
              required(manifest, prefix + "source"),
              required(manifest, prefix + "target")));
    }
    return relocations;
  }

  private void loadLibrary(
          @NotNull final BukkitLibraryManager libraryManager,
          @NotNull final Path cacheDirectory,
          @NotNull final Library library) throws IOException {

    final Path original = cacheDirectory.resolve(library.getPath());
    final Path relocated = cacheDirectory.resolve(library.getRelocatedPath());
    final Path relocatedChecksum = relocated.resolveSibling(relocated.getFileName() + ".sha256");
    if(Files.exists(original) && !checksumMatches(original, library.getChecksum())) {
      plugin.getBootstrapLogger().warning("Discarding invalid cached library " + original.getFileName());
      Files.delete(original);
      Files.deleteIfExists(relocated);
      Files.deleteIfExists(relocatedChecksum);
    }
    final boolean relocatedValid = Files.exists(relocated)
            && checksumMatches(relocated, relocatedChecksum, library.getChecksum());
    if(Files.exists(relocated) && !relocatedValid) {
      plugin.getBootstrapLogger().warning("Rebuilding invalid relocated library " + relocated.getFileName());
      Files.delete(relocated);
      Files.deleteIfExists(relocatedChecksum);
    }

    final Path resolved = libraryManager.downloadLibrary(library);
    if(!checksumMatches(original, library.getChecksum())) {
      throw new IOException("Invalid cached checksum after downloading " + library);
    }
    if(!relocatedValid) {
      Files.writeString(relocatedChecksum,
              Base64.getEncoder().encodeToString(library.getChecksum()) + System.lineSeparator()
                      + Base64.getEncoder().encodeToString(sha256(resolved)) + System.lineSeparator());
    }
    libraryManager.loadLibrary(library);
  }

  private boolean checksumMatches(
          @NotNull final Path file,
          @NotNull final Path checksumFile,
          @NotNull final byte[] sourceChecksum) throws IOException {

    if(!Files.exists(checksumFile)) {
      return false;
    }
    try {
      final List<String> checksums = Files.readAllLines(checksumFile);
      return checksums.size() == 2
              && MessageDigest.isEqual(Base64.getDecoder().decode(checksums.get(0)), sourceChecksum)
              && checksumMatches(file, Base64.getDecoder().decode(checksums.get(1)));
    } catch(final IllegalArgumentException e) {
      return false;
    }
  }

  private boolean checksumMatches(@NotNull final Path file, @NotNull final byte[] expected) throws IOException {

    return Files.exists(file) && MessageDigest.isEqual(sha256(file), expected);
  }

  @NotNull
  private byte[] sha256(@NotNull final Path file) throws IOException {

    try(InputStream input = Files.newInputStream(file)) {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      final byte[] buffer = new byte[8192];
      int read;
      while((read = input.read(buffer)) >= 0) {
        digest.update(buffer, 0, read);
      }
      return digest.digest();
    } catch(final NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
    }
  }

  @NotNull
  private String required(@NotNull final Properties manifest, @NotNull final String key) throws IOException {

    final String value = manifest.getProperty(key);
    if(value == null || value.isBlank()) {
      throw new IOException("Missing " + key + " in " + MANIFEST);
    }
    return value;
  }

  private int requiredInt(@NotNull final Properties manifest, @NotNull final String key) throws IOException {

    try {
      return Integer.parseInt(required(manifest, key));
    } catch(final NumberFormatException e) {
      throw new IOException("Invalid " + key + " in " + MANIFEST, e);
    }
  }
}
