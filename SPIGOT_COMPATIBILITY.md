# Spigot compatibility decisions

This fork keeps Spigot support as a hard requirement. Upstream fixes and
features are adopted when they can remain compatible with the server versions
already supported by QuickShop-Hikari 6.2.0.10. Paper-only or Folia-only work
may be skipped when adapting it would add substantial complexity or make the
Java 17 build unusable.

## Removed or replaced behavior

| Area | Decision | User-visible impact |
| --- | --- | --- |
| TNML Paper/Folia adapters | Replaced the Paper/Folia-specific menu, listener, item-stack, and player implementations with their Bukkit implementations. | Spigot and Paper use the same compatible path. Paper-specific optimization is not used; Folia is not a supported target of this fork. |
| TNML Folia runtime | The `TNML-Folia` runtime dependency is not bundled. | No Folia-specific scheduler/inventory integration is provided. |
| Paper block snapshot shortcut | Replaced `Block#getState(false)` with the Bukkit/Spigot `Block#getState()` API. | Display behavior remains available, but Paper's no-snapshot optimization is not used. |
| AngelChest compatibility module | Excluded from the Gradle release build because its legacy `AngelChestAPI` artifact cannot be resolved reliably from the vendor repository with the supported build toolchain. The existing source and Maven descriptor are retained for possible restoration. | `Compat-AngelChest` is not produced by the Gradle build. Core QuickShop behavior is unchanged. |
| IridiumSkyblock compatibility module | Excluded from the Gradle release build because the pinned 6.2.0.10 API artifact `com.iridium:IridiumSkyblock:4.1.0` is no longer available from its declared repository. Existing source and Maven descriptor are retained. | `Compat-IridiumSkyblock` is not produced by the Gradle build. Core QuickShop behavior is unchanged. |

## Compatibility bridges retained

- New potion metadata methods are called reflectively so Java 17 can compile
  against Spigot 1.20.1 while newer 1.20/1.21 servers keep potion behavior.
- The custom item-name API is called reflectively because it is absent from
  Spigot 1.20.1.
- Minecraft 1.21's `EntityType.ITEM` is resolved by name so the packet code can
  coexist with the older compile API.
- The WorldEdit compatibility module compiles against the stable 7.2.18 API
  instead of the Java 21-only 7.3.11 artifact, retaining Java 17 bytecode while
  using APIs that remain available in later WorldEdit releases.
- Java 17 builds include the 1.20 R1-R3 Spigot platform modules. Java 21 builds
  additionally include the 1.20 R4 and 1.21 R1-R5 modules from 6.2.0.10.

Add future skipped or removed upstream functionality to this file, including
the reason and the fallback seen by users.
