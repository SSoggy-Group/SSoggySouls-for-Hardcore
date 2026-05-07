## 2026-05-06 - [Head No-Despawn Parity]
**Learning:** To prevent item entities from despawning across platforms, the APIs differ significantly: Bukkit/Spigot requires cancelling the `ItemDespawnEvent`, Fabric provides the explicit `ItemEntity.setNeverDespawn()` method, and Forge uses `ItemEntity.setUnlimitedLifetime()`.
**Action:** Always verify the specific item entity lifecycle methods provided by the target mod loader when porting despawn logic, rather than assuming standard mappings.
## 2026-03-13 - [No Head Despawn]
**Learning:** Native `setNeverDespawn()` (Fabric) and `setUnlimitedLifetime()` (Forge) methods for `ItemEntity` are sometimes insufficient alone due to aggressive age reset logic in Minecraft or conflicting mods/plugins.
**Action:** For robust despawn prevention that mirrors Spigot/Paper's `ItemDespawnEvent` cancellation, explicitly cancel despawning logic. In Forge, cancel `ItemExpireEvent` and reset lifetime. In Fabric, mixin to `ItemEntity.tick`, intercept the natural age threshold (`>= 6000`), and enforce the lifetime extension.
