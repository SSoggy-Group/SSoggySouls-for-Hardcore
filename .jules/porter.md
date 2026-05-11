## 2026-05-06 - [Head No-Despawn Parity]
**Learning:** To prevent item entities from despawning across platforms, the APIs differ significantly: Bukkit/Spigot requires cancelling the `ItemDespawnEvent`, Fabric provides the explicit `ItemEntity.setNeverDespawn()` method, and Forge uses `ItemEntity.setUnlimitedLifetime()`.
**Action:** Always verify the specific item entity lifecycle methods provided by the target mod loader when porting despawn logic, rather than assuming standard mappings.
## 2026-05-08 - [Portal Travel Blocking Parity]
**Learning:** To prevent dimension travel (e.g., via Nether or End portals) and mirror Bukkit/Paper's `PlayerPortalEvent` cancellation:
- In LexForge, subscribe to the `net.minecraftforge.event.entity.EntityTravelToDimensionEvent` and cancel it via `event.setCanceled(true)`.
- In Fabric, inject a Mixin at the `HEAD` of `ServerPlayerEntity.teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/entity/Entity;` (return type `Entity` and target parameter `TeleportTarget`) and use `cir.setReturnValue((ServerPlayerEntity)(Object)this)` to return the original player, gracefully cancelling the teleport logic.
**Action:** Always intercept `teleportTo` returning `Entity` in Fabric or use `EntityTravelToDimensionEvent` in Forge when mimicking Paper's `PlayerPortalEvent`.
## 2026-05-10 - [Forge macOS LWJGL Native Resolution Failure]
**Learning:** On macOS (arm64), `./gradlew :forge:compileJava` fails at the dependency resolution phase with `Could not find lwjgl-freetype-3.3.3-natives-macos-patch.jar`. This jar does not exist on Maven Central; it is only available in the Forge maven for specific platform targets. `--refresh-dependencies` does not fix it. The failure is pre-existing and unrelated to code changes — Linux CI resolves the natives correctly. When verifying Forge changes locally on macOS, compile NeoForge instead (same MC API surface, different event bus package names) as a proxy check, and rely on CI for definitive Forge build confirmation.
**Action:** Do not block a Forge PR on this macOS failure. Run `--configure-on-demand` on NeoForge/Fabric locally and let CI handle Forge verification.
