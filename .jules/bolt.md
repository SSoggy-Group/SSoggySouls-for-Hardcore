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

## 2024-05-18 - [Enum Caching for Tab Completions]
**Learning:** Calling `Enum.values()` inside command `onTabComplete` methods allocates a new array on every keystroke, causing unnecessary garbage collection overhead on highly active paths.
**Action:** Always pre-compute and cache `Enum.values()` mapping conversions (like `.name().toLowerCase()`) in a `static final List<String>` during class initialization to prevent redundant string and array allocations during tab completion.

## 2024-05-24 - [Avoid Stream map allocation overhead on player names in Brigadier]
**Learning:** In Fabric and Forge/NeoForge Brigadier command tab completions, doing `server.getPlayerList().stream().map(p -> p.getName())` inside `.suggests(...)` allocates a new stream and performs O(N) operations on every keystroke.
**Action:** Use `server.getPlayerNames()` natively available on the server instance, which returns a `String[]`. This avoids the overhead of the Stream API entirely during tab-completion.

## 2026-05-24 - [Avoid Stream map allocation overhead on player names in Paper]
**Learning:** In Bukkit/Paper command tab completions, doing `Bukkit.getOnlinePlayers().stream().map(Player::getName)` allocates a new stream and performs O(N) operations on every single keystroke.
**Action:** Use a direct loop to iterate over `Bukkit.getOnlinePlayers()` and add names to an `ArrayList` directly, filtering manually, to avoid the overhead of the Stream API entirely during highly frequent tab-completions.
