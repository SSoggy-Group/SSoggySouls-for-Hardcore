## 2026-05-06 - [Head No-Despawn Parity]
**Learning:** To prevent item entities from despawning across platforms, the APIs differ significantly: Bukkit/Spigot requires cancelling the `ItemDespawnEvent`, Fabric provides the explicit `ItemEntity.setNeverDespawn()` method, and Forge uses `ItemEntity.setUnlimitedLifetime()`.
**Action:** Always verify the specific item entity lifecycle methods provided by the target mod loader when porting despawn logic, rather than assuming standard mappings.
