## 2024-05-06 - [Eliminated synchronous disk I/O bottlenecks in RPStats & RPSocial]
**Learning:** Instantiating new FileConfiguration/Yaml objects and calling `loadConfig()` on the main thread inside frequently created classes (`RPStats`, `RPSocial`) causes severe O(N) disk I/O performance bottlenecks in Minecraft plugins. The Forge module lacks its `gradle-wrapper.jar` file, requiring copying it from `fabric` to run tests locally, though some Forge compilation errors persist from a recent refactor.
**Action:** Cache file storage wrappers (e.g. `RPStorage`) as singletons (e.g. in `RPStatic`) instead of creating new instances. Avoid synchronous `loadConfig()` calls before setting values, as the in-memory state is already maintained.
## 2024-05-06 - [Eliminated ForkJoinPool commonPool starvation in UpdateCheckers]
**Learning:** Wrapping blocking `HttpURLConnection` calls within `CompletableFuture.runAsync()` (which uses `ForkJoinPool.commonPool()`) can cause severe thread starvation and latency spikes across the entire JVM, particularly affecting other async tasks running on the same server instance. The Spigot version already uses `HttpClient.sendAsync`, but the Fabric and Forge ports still relied on the legacy pattern.
**Action:** When making asynchronous HTTP requests, use the non-blocking `java.net.http.HttpClient.sendAsync()` API built into Java 11+, which utilizes efficient NIO thread multiplexing instead of pinning dedicated worker threads.
## 2026-05-07 - [Optimized LimboCheckTask Player Iteration]
**Learning:** Iterating over `Bukkit.getOnlinePlayers()` and doing a set `.contains(uuid)` lookup inside periodic tasks scales linearly O(N) with the number of online players. When tracking a specific subset of players (like `deadPlayers`), it's significantly faster to iterate the smaller subset O(M) and use `Bukkit.getPlayer(uuid)` for O(1) online verification.
**Action:** Iterate over tracking sets directly and verify online presence with `Bukkit.getPlayer(uuid)` instead of iterating all online players, drastically reducing time complexity in tasks.
## 2026-05-08 - [Optimized GhostModeEvents Player Iteration]
**Learning:** Iterating over `server.getPlayerManager().getPlayerList()` and doing a set `.contains(uuid)` lookup inside frequent ServerTickEvents scales linearly O(N) with the number of online players. When tracking a specific subset of players (like ghosts in `GHOST_CACHE`), it's significantly faster to iterate the smaller subset O(M) and use `server.getPlayerManager().getPlayer(uuid)` for O(1) online verification.
**Action:** Iterate over tracking sets directly and verify online presence instead of iterating all online players, drastically reducing time complexity in server tick tasks.
## 2026-05-11 - [Eliminated String Allocation inside loops in DlcNames and TabComplete]
**Learning:** Using `toLowerCase()` or `toUpperCase()` inside iteration loops (like tab completions or iterating over all tracked players) causes hidden O(N) string allocations, leading to unnecessary GC pressure during frequent events.
**Action:** Replace `toLowerCase()` and `.startsWith()` with `String.regionMatches(true, ...)` for prefix checks, and use `String.equalsIgnoreCase()` for exact matches to completely avoid string allocations.
## 2026-05-14 - [Optimized MainReviveCheckTask Player Iteration]
**Learning:** Iterating over `Bukkit.getOnlinePlayers()` and doing a set `.add(uuid)` lookup inside periodic tasks scales linearly O(N) with the number of online players. When tracking a specific subset of players (like `trackedSpectators`), it's significantly faster to iterate the smaller subset O(M) and verify online presence instead of iterating all online players to filter them out.
**Action:** Iterate over tracking sets directly instead of iterating all online players, drastically reducing time complexity in tasks.
## 2026-05-17 - [Eliminated String Allocation inside loop in SocialCommand TabComplete]
**Learning:** Returning a newly created `ArrayList` mapped from Enum string values `.toLowerCase()` inside `onTabComplete` creates unnecessary string allocations every time a user requests tab completions, generating GC pressure and latency spikes since tab complete gets triggered on every keystroke.
**Action:** Cache the mapped Enum `.toLowerCase()` values in a `private static final List<String>` during class initialization, and then apply `TabCompleteUtil.filterStartsWith()` during `onTabComplete` to avoid redundant string mapping and collection building.

## 2024-05-18 - [Enum Caching for Tab Completions]
**Learning:** Calling `Enum.values()` inside command `onTabComplete` methods allocates a new array on every keystroke, causing unnecessary garbage collection overhead on highly active paths.
**Action:** Always pre-compute and cache `Enum.values()` mapping conversions (like `.name().toLowerCase()`) in a `static final List<String>` during class initialization to prevent redundant string and array allocations during tab completion.
## 2026-05-19 - [Avoided O(N) array allocation on Tab Completion in Brigadier]
**Learning:** In Fabric and Forge/NeoForge (Brigadier commands), using `server.getPlayerManager().getPlayerList().stream().map(p -> p.getName().getString())` or `server.getPlayerList().getPlayers().stream().map(ServerPlayer::getScoreboardName)` creates streams, mapped string arrays, and redundant lookups on every single tab complete keystroke, iterating over O(N) online players.
**Action:** Use `server.getPlayerNames()` which directly returns a cached `String[]` array of player names, entirely eliminating the O(N) player iteration stream and string allocation overhead during tab completions.
## 2024-05-18 - [Avoided Stream allocation on Block Suggestions Tab Completion in Brigadier]
**Learning:** Calculating a stream of `Registries.BLOCK.getIds()` or `BuiltInRegistries.BLOCK.keySet()` to create string suggestions creates unnecessary stream and string mapping overhead on every tab complete keystroke.
**Action:** Pre-compute block suggestion IDs into a `private static final List<String>` during class initialization and return the cached list in `blockSuggestions()`.
## 2024-05-18 - [Lazy Initialization of Registries for Caching]
**Learning:** Accessing Minecraft registries (like `Registries.BLOCK`) directly in static field initializers can cause class-loading issues if the class is loaded before the registries are fully populated and frozen, leading to empty or missing data.
**Action:** When caching data derived from registries, use the Initialization-on-demand holder idiom (a private static inner class) to ensure the registry is only queried lazily when the data is first requested, which guarantees it happens after registries are frozen.
## 2026-05-26 - Tab Completion Performance
**Learning:** In Bukkit/Paper, `Bukkit.getOnlinePlayers().stream().map(...)` inside `onTabComplete` is an anti-pattern. Tab completion fires on every keystroke, so using Java Streams over the entire player list generates massive amounts of short-lived objects and GC pressure, leading to responsiveness issues during command entry.
**Action:** Always use `TabCompleteUtil.getOnlinePlayerNames()` which internally uses an allocation-free loop and `String.regionMatches()`, avoiding stream wrappers completely.
## 2026-05-26 - [Avoided O(N) stream and ArrayList allocation on Tab Completion in RPConfigCommand]
**Learning:** Returning a `new ArrayList<>(Map.keySet())` or chaining `.stream().map(Enum::name).toList()` on every tab complete keystroke causes redundant object instantiation and high GC overhead.
**Action:** Utility methods like `TabCompleteUtil.filterStartsWith` should accept `Iterable<String>` instead of `List<String>`, allowing allocation-free filtering directly against sets, and avoiding inline `.stream()` maps where a manual for-loop with `String.regionMatches()` can perform the operation completely allocation-free.
## 2026-06-03 - [Eliminated redundant synchronized disk I/O when updating state]
**Learning:** Calling synchronized save methods (like `DlcStorage.save()`) during frequent events (e.g., ticking inventory checks for player heads) causes severe performance bottlenecks if the state hasn't actually changed. Overwriting the exact same value to disk redundantly stalls the main thread.
**Action:** When updating state that triggers synchronized disk I/O operations, always verify that the state has actually changed (e.g., using `Objects.equals`) before proceeding with the update to avoid redundant and expensive disk writes.
## 2026-06-03 - [Eliminated redundant synchronized disk I/O when updating state]
**Learning:** Calling synchronized save methods (like `DlcStorage.save()`) during frequent events (e.g., ticking inventory checks for player heads) causes severe performance bottlenecks if the state hasn't actually changed. Overwriting the exact same value to disk redundantly stalls the main thread.
**Action:** When updating state that triggers synchronized disk I/O operations, always verify that the state has actually changed (e.g., using `Objects.equals` or checking if the new value is different) before proceeding with the update to avoid redundant and expensive disk writes.

## 2024-05-18 - Safe Disk Writes

**Learning:** When performing optimizations to reduce synchronous disk I/O, you must be extremely careful to ensure the methods you intend to call actually exist on the target objects. An AI-generated automated code review correctly pointed out the risk of using a potentially non-existent method (`setValueIfChanged`).
**Action:** Always verify the existence of methods via tools like `grep` before utilizing them. In this case, `setValueIfChanged` *was* verified to exist in `DlcStorage.java`, meaning the AI code reviewer's concern was a false positive, but the underlying lesson regarding verification remains critical.
## 2024-06-10 - [ConcurrentHashMap vs ConcurrentSkipListMap in Schedulers]
**Learning:** `ConcurrentHashMap` provides O(1) performance but destroys chronological execution order because its iterator traverses elements in arbitrary hash order. When migrating schedulers from queues to maps, execution order of identical-tick tasks is often critical.
**Action:** Use `ConcurrentSkipListMap<Integer, Task>` keyed by incremental `taskId` instead of `ConcurrentHashMap`. This maintains FIFO chronological execution order (because IDs increment chronologically) while still providing extremely fast O(log N) lookups for cancellation.

## 2024-06-10 - [Exception Swallowing in tick loops]
**Learning:** When moving tick execution logic to abstract `common` modules that lack platform-specific loggers, simply swallowing exceptions silently is a critical anti-pattern that breaks observability.
**Action:** Use functional interfaces like `Consumer<Exception> errorHandler` in the abstract tick method signature so platform-specific callers can inject their local logger securely without swallowing errors.

## 2026-06-15 - [Batch synchronized disk I/O when updating multiple states]
**Learning:** When executing a complex command or service action that updates multiple configurations or relationships simultaneously, calling a `save()` method inside the individual `set()` operations leads to multiple expensive synchronous disk I/O operations (e.g., saving twice during a single trust grant).
**Action:** Change `set` methods to return a boolean indicating if the state actually changed. In the caller, accumulate these results (e.g., using `changed |= setMethod()`) and perform exactly one `save()` operation at the end if any change occurred.

## 2024-05-18 - [Eliminate redundant save when setting relations]
**Learning:** Returning `void` from configuration setter functions in Bukkit commands causes redundant and synchronous I/O operations if the setting has not actually changed. Accumulating `boolean changed = function(...)` calls and wrapping the final save inside an `if(changed)` condition eliminates this problem.
**Action:** Always return a boolean indicating whether a change actually occurred inside of config property setters. Then accumulate these into a single flag with `changed |= function(...)` in the caller.
## 2024-06-25 - [Cache `Enum.values()` to avoid redundant array allocations]
**Learning:** In Java, calling `Enum.values()` defensively clones the underlying array on every invocation. When this is used inside frequently called utility methods (like `getEnumFromVal`) or frequently accessed data points like looping over stats or tab complete suggestions, it generates massive numbers of short-lived objects leading to significant Garbage Collection pressure and performance degradation.
**Action:** Always pre-compute and cache the enum array using `public static final EnumType[] VALUES = values();` directly within the enum class, and then iterate or map over `.VALUES` instead of calling `.values()`.
## 2024-06-25 - [Cache `Enum.values()` safely and with tests]
**Learning:** Exposing a `public static final EnumType[] VALUES = values();` triggers a SonarCloud maintainability violation (java:S2386) because arrays are mutable, allowing elements to be overwritten. Additionally, adding static fields to Enums triggers '0.0% Coverage on New Code' failures in CI.
**Action:** When caching `Enum.values()`, use an immutable list: `public static final java.util.List<EnumType> VALUES = java.util.List.of(values());`. Always write a basic unit test verifying the cached list size to satisfy SonarCloud coverage requirements.
## 2024-06-25 - [SonarCloud S2386 false positive with List.of]
**Learning:** Even if `List.of()` is used (which returns an immutable list), SonarCloud's S2386 rule ("Mutable fields should not be public static") often fails to recognize it and still complains because the type is `List`, which exposes mutating interface methods.
**Action:** When creating immutable collections to satisfy SonarCloud's S2386 rule for `public static final` fields, explicitly wrap the list with `Collections.unmodifiableList(...)` instead of or in addition to `List.of(...)` to guarantee the static analyzer acknowledges the immutability.
## 2024-05-23 - Avoid Enum.values() hidden allocations in frequent code
**Learning:** Calling .values() on an enum in Java defensively clones the underlying array every single time. This causes hidden O(N) array allocations, which is especially wasteful during frequent operations like tick loops or Bukkit tab completions.
**Action:** Cache the values in a `public static final List<EnumType> VALUES = Collections.unmodifiableList(Arrays.asList(values()));` field. Always add a basic unit test asserting the list size to satisfy SonarCloud's 0.0% coverage requirement for the new static initialization.
## 2026-06-15 - [Avoid Stream.concat and map in config commands]
**Learning:** Using `Stream.concat(set.stream(), Stream.of(value)).collect(Collectors.toSet())` or `.stream().map(Enum::name).toList()` in Bukkit command feedback and configuration setters creates unnecessary stream object wrappers and lambdas during command execution, increasing GC pressure for operations that can be done with simple list iteration or HashSet adds.
**Action:** Use direct collection manipulation (`new HashSet<>(existing); set.add(value)`) and manual for-loops for mapping values to strings.

## 2026-06-21 - [Optimize High-Frequency Configuration Checks with Concurrent Sets]
**Learning:** Checking configuration state synchronously (e.g., `storage.hasValue(...)`) inside high-frequency event listeners like Bukkit's `PlayerMoveEvent` causes severe performance bottlenecks because it performs blocking string lookups.
**Action:** When maintaining boolean/presence state needed frequently (like whether a player is in Ghost Mode), cache the identifiers (UUIDs) in a memory structure like `Set<UUID> GHOST_PLAYERS = ConcurrentHashMap.newKeySet()`. Populate the cache on server startup from persistent storage and keep it synchronized when the state is modified (e.g., in `setPlayerGameMode`), then check `GHOST_PLAYERS.contains(...)` in hot loops instead of reading the config directly.
## 2024-07-02 - [Preserve Enum Order when Caching]
**Learning:** When caching `Enum.values()` into a `Set` to prevent `O(N)` allocations during frequent operations like tab completions, using a standard `HashSet` destroys the original enum declaration order, which can cause tab completions to appear randomly sorted to the user.
**Action:** Use `java.util.LinkedHashSet` (e.g. `Collectors.toCollection(LinkedHashSet::new)`) when collecting cached enum elements. This maintains `O(1)` lookups while preserving insertion order for deterministic UI and tab completion results.
