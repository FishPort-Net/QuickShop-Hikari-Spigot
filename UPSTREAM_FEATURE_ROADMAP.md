# Spigot 1.20.1 Upstream Feature Roadmap

This fork ports upstream features selectively onto the 6.2.0.10-compatible Spigot model. The
upstream commits below must not be cherry-picked wholesale: several also replace the shop API,
raise platform assumptions, or remove Spigot support.

## Ported on `feature/spigot-market-features-1.20.1`

### Shop query and market browser

- Source: `ca5b5cf6b` (flexible shop queries), plus the world filter from `b96a3cd6f`.
- Added composable material, owner, frozen, type, world, and custom predicate filters.
- Extended `/qs browse` with world, buying/selling, favorite/watch/custom-tag, text search,
  price sort, and newest-first filters.
- Browse entries now open the existing trade GUI.
- The 6.3 `ShopState` and `IShopType` API replacements were deliberately adapted to the 6.2
  `isFrozen()` and `ShopType` API.

### Tags, favorites, and watching

- Source: the tag subsystem of `9e476c405`.
- Reused the tag table and low-level database operations already present in 6.2.0.10.
- Added an in-memory per-player bidirectional index, tag API, startup loading, orphan cleanup when
  a shop is deleted, `/qs tag`, `/qs favorite`, and `/qs watch`.
- Lists use the market GUI rather than importing the 6.3 pagination and complete Shop API rewrite.
- `@avoid` remains reserved in the API but has no command or market exclusion behavior yet.

### Inventory count cache and asynchronous queries

- Sources: `988fc7561` and the portable intent of `a9c9fa053`.
- Loads the existing external stock/space cache together with every shop and keeps it in memory.
- Main-thread inventory calculations refresh memory and database caches.
- Off-thread getters no longer block on a database query. New async getters use the memory cache
  and schedule a main-thread calculation only when the cache has never been initialized.
- Market availability filtering waits on the async getters without reading Bukkit inventories from
  an async thread.
- Upstream Paper/Folia region ownership checks were intentionally replaced with Bukkit main-thread
  scheduling for Spigot 1.20.1.

### Display entities and rendering

- Sources: `3c6e31812`, `a655463a2`, `d23b21ab9`, `43f350a57`, and the rendering concepts from
  `297ff6dfd`.
- Added display type `3` backed by Spigot `ItemDisplay` and optional `TextDisplay`, both available in
  the supported 1.20.1 API.
- Added chest-facing rotation, scale, configurable text lines and placeholders, chunk indexing,
  cleanup, and player join/chunk visibility refresh.
- Kept the existing virtual item display as default (`display-type: 2`).
- No Paper scheduler, Paper components, or Folia region API is used by the new implementation.

## Deferred for later 1.20.1 work

- Display-entity click hitboxes. Upstream routes `Interaction` entity clicks through the rewritten
  6.3 interaction API. Port this separately only after defining a small 6.2-compatible event bridge;
  shops remain fully usable through their container and sign meanwhile.
- A dedicated market configuration file and fully configurable GUI layouts from the late 6.3 GUI
  series. The useful search/filter/sort architecture is present, but copying the complete GUI stack
  would also pull in new Shop state/type/economy abstractions.
- GUI buttons for adding/removing tags, favorites, and watches. Commands and filtered market views
  are present; GUI controls can be added without changing persistence.
- `@avoid` filtering and watch-triggered player notifications. The storage/index foundation exists,
  but notification semantics and rate limiting should be specified first.
- Reusable sign rendering components from `297ff6dfd`. Text-display placeholders were ported; the
  sign half depends heavily on the 6.3 Shop layout provider and is not required for display type 3.

## Not planned for this compatibility line

- Paper/Folia-only schedulers, region ownership APIs, menu implementations, and components.
- The full `9e476c405` Shop meta/state/trading/tax/metrics rewrite.
- Java 21-only core bytecode or Minecraft versions beyond the fork's declared support matrix.
- Map-addon cache changes from `1a90087de` unless those addons are restored and verified on Spigot.

Bundled translations remain English fallback plus the administrator override system. Crowdin OTA
is disabled by default through `use-crowdin-ota: false`; maintainers can provide
`overrides/zh_cn/messages.yml`. No new bundled `zh_cn` snapshot is tracked by this fork.
