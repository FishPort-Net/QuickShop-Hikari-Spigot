# Spigot compatibility decisions

This fork publishes one Spigot JAR with a Java 17 core and lazily loaded Java
21 platform adapters. Upstream fixes and features are adopted when they remain
compatible with the Spigot versions represented in QuickShop-Hikari 6.2.0.10.
Paper and Folia platform support is not included in the release artifact.

## Current release support

The `6.2.0.10-spigot.4` release has the following deliberately narrow support
matrix:

| Area | Supported target |
| --- | --- |
| Distribution | One self-contained shaded JAR for both compatibility levels |
| Java 17 path | Ketting 1.20.1 through its Spigot path, plus Spigot/CraftBukkit 1.20 through 1.20.4 (`v1_20_R1` to `v1_20_R3`) |
| Java 21 path | Spigot/CraftBukkit 1.20.5/1.20.6 (`v1_20_R4`) and Minecraft 1.21 through 1.21.5 (`v1_21_R1` to `v1_21_R4`) |
| Server platform | Spigot-compatible execution only; the Paper platform implementation and Folia support declaration are omitted |
| Database drivers | Bundled and relocated H2 2.1.214, MySQL Connector/J 8.4.0, and CsvJdbc 1.0.42; JDBC service descriptors are merged |
| Private libraries | Bundled and relocated inside QuickShop; they are not injected into the server-wide or plugin-shared class loader |

The release JAR must be assembled with Java 21 so it contains both adapter
levels. Java 17 servers resolve only the Java 17 core and 1.20 R1-R3 classes;
the Java 21 adapters are selected reflectively only on their matching modern
Spigot revisions. Paper and Folia servers are outside the supported release
matrix.

## Removed or replaced behavior

| Area | Decision | User-visible impact |
| --- | --- | --- |
| TNML Paper/Folia adapters | Replaced the Paper/Folia-specific menu, listener, item-stack, and player implementations with their Bukkit implementations. | The supported Spigot path uses the compatible Bukkit implementations. Paper-specific optimization is not used; Paper and Folia are not release targets. |
| TNML Folia runtime | The `TNML-Folia` runtime dependency is not bundled. | No Folia-specific scheduler/inventory integration is provided. |
| Paper block snapshot shortcut | Replaced `Block#getState(false)` with the Bukkit/Spigot `Block#getState()` API. | Display behavior remains available, but Paper's no-snapshot optimization is not used. |
| Folia region ownership checks | Upstream's region-thread-only stock calculation (`62fb03959`) was not adopted because it directly calls Paper/Folia region ownership APIs. | Spigot keeps the 6.2.0.10 inventory/cache path. Folia region-thread guarantees are not provided. |
| Java 21 virtual database threads | Upstream's virtual-thread database executor (`c9a10c8db`) was not adopted. | Database work uses the Java 17-compatible executor retained from 6.2.0.10. |
| Post-range Minecraft adapters | Adapters introduced after the retained 1.21.5 platform range, including 1.21.10/1.21.11-specific work, are omitted. | No support is claimed beyond the modern Spigot adapters already represented in this fork. |
| AngelChest compatibility module | Excluded from the Gradle release build because its legacy `AngelChestAPI` artifact cannot be resolved reliably from the vendor repository with the supported build toolchain. The existing source and Maven descriptor are retained for possible restoration. | `Compat-AngelChest` is not produced by the Gradle build. Core QuickShop behavior is unchanged. |
| IridiumSkyblock compatibility module | Excluded from the Gradle release build because the pinned 6.2.0.10 API artifact `com.iridium:IridiumSkyblock:4.1.0` is no longer available from its declared repository. Existing source and Maven descriptor are retained. | `Compat-IridiumSkyblock` is not produced by the Gradle build. Core QuickShop behavior is unchanged. |

## Compatibility bridges retained

- New potion metadata methods are called reflectively so the Java 17 core can
  compile against Spigot 1.20.1 while modern Spigot servers retain their
  potion behavior.
- The custom item-name API is called reflectively because it is absent from
  Spigot 1.20.1.
- Minecraft 1.21's `EntityType.ITEM` is resolved by name so the Java 17 core
  can coexist with the modern Spigot adapters.
- The WorldEdit compatibility module compiles against the stable 7.2.18 API
  instead of the Java 21-only 7.3.11 artifact, retaining Java 17 bytecode while
  using APIs that remain available in later WorldEdit releases.
- Release builds include Java 17-compatible Spigot 1.20 R1-R3 plus the lazily
  loaded Java 21 Spigot 1.20 R4 and 1.21 platform modules. The Paper platform
  module is not included.

Add future skipped or removed upstream functionality to this file, including
the reason and the fallback seen by users.
