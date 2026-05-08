package org.ssoggy.ssoggysouls.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.GameMode;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.HeadDropListener;
import org.ssoggy.ssoggysouls.hrm.RevivalStructureListener;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostModeEvents;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcDeaths;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcNames;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcStat;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcStats;
import org.ssoggy.ssoggysouls.hrm.dlc.util.GhostState;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles core lifecycle events: joining, quitting, and dying.
 */
public class MainServerListener {

    private final DatabaseManager db;

    private MainServerListener(DatabaseManager db) {
        this.db = db;
        registerJoinEvent();
        registerQuitEvent();
        registerDeathEvent();
        registerRespawnEvent();
    }

    public static void register(DatabaseManager db) {
        new MainServerListener(db);
    }

    private void registerJoinEvent() {
        // Player Join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID uuid = player.getUuid();

            // Run async DB fetch
            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(uuid);
                if (data == null) {
                    long graceMs = ConfigManager.parseGracePeriod(ConfigManager.getConfig().getGracePeriod());
                    data = PlayerData.createNew(uuid, player.getName().getString(),
                            ConfigManager.getConfig().getDefaultLives(), graceMs);
                    db.savePlayer(data);
                } else {
                    data.setUsername(player.getName().getString());
                    db.savePlayer(data);
                }
                DlcNames.cache(uuid, player.getName().getString());

                final PlayerData finalData = data;
                server.execute(() -> handleJoinSync(player, finalData));
            });
        });
    }

    private void handleJoinSync(ServerPlayerEntity player, PlayerData data) {
        // Apply a pending offline revival (teleport + restore gamemode + effects)
        GlobalPos pending = RevivalStructureListener.consumePendingRevival(player.getUuid());
        if (pending != null) {
            setGhostModeAttributes(player, false);
            ServerWorld targetWorld = player.getServer().getWorld(pending.dimension());
            RevivalStructureListener.restoreAtStructure(player, targetWorld != null ? targetWorld : player.getServerWorld(), pending.pos());
            return;
        }

        if (data.isDead()) {
            if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
                ServerTransferUtil.sendToLimbo(player);
                return;
            }

            // Dead player joined -> Ghost mode (Adventure)
            if (player.interactionManager.getGameMode() != GameMode.ADVENTURE) {
                player.changeGameMode(GameMode.ADVENTURE);
                setGhostModeAttributes(player, true);
                player.sendMessage(MessageUtil.get("ghost-mode-active"), false);
            }
        } else if (player.interactionManager.getGameMode() == GameMode.ADVENTURE) {
            // Alive player was in ghost -> Restore
            player.changeGameMode(GameMode.SURVIVAL);
            setGhostModeAttributes(player, false);
        }
    }

    private void registerQuitEvent() {
        // Player Quit
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID uuid = player.getUuid();
            long now = System.currentTimeMillis();
            CompletableFuture.runAsync(() -> db.setLastSeen(uuid, now));
        });
    }

    private void registerDeathEvent() {
        // Player Death
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            UUID uuid = player.getUuid();
            ServerPlayerEntity killer = damageSource.getAttacker() instanceof ServerPlayerEntity serverPlayer ? serverPlayer : null;

            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(uuid);
                if (data == null) return; // Should not happen if they joined

                if (data.isInGracePeriod(ConfigManager.parseGracePeriod(ConfigManager.getConfig().getGracePeriod()))) {
                    return; // Grace period protects from life loss
                }

                int remaining = data.decrementLife();
                db.savePlayer(data);
                new DlcStats(uuid).incrementStat(DlcStat.DEATHS, 1);
                if (killer != null) {
                    DlcNames.cache(killer.getUuid(), killer.getName().getString());
                    new DlcStats(killer.getUuid()).incrementStat(DlcStat.KILLS, 1);
                }

                player.server.execute(() -> handleDeathSync(player, data, remaining));
            });
        });
    }

    private void handleDeathSync(ServerPlayerEntity player, PlayerData data, int remaining) {
        if (data.isDead()) {
            if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
                player.sendMessage(MessageUtil.get("death-sending-to-limbo"), false);
                ServerTransferUtil.sendToLimbo(player);
                return;
            }

            GhostState state = GhostState.getServerState(player.getServer());
            state.deathLocations.put(player.getUuid(), player.getBlockPos());
            state.markDirty();
            DlcDeaths.recordDeath(
                    player.getUuid(),
                    player.getName().getString(),
                    player.getServerWorld().getRegistryKey().getValue().toString(),
                    player.getBlockPos().getX(),
                    player.getBlockPos().getY(),
                    player.getBlockPos().getZ()
            );

            player.changeGameMode(GameMode.ADVENTURE);
            setGhostModeAttributes(player, true);
            GhostModeEvents.updateGhostStatus(player.getUuid(), true);
            org.ssoggy.ssoggysouls.listener.LimboServerListener.updateLimboStatus(player.getUuid(), true);
            player.sendMessage(MessageUtil.get("death-now-ghost"), false);

            // Trigger head drop now that we know isDead is true (avoids race with DB state)
            if (ConfigManager.getConfig().isDropHeads()) {
                HeadDropListener.triggerHeadDrop(player);
            }
        } else {
            player.sendMessage(MessageUtil.get("death-life-lost", "lives", remaining), false);
        }
    }

    private void registerRespawnEvent() {
        // Player Respawn
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            UUID uuid = newPlayer.getUuid();
            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(uuid);
                if (data != null && data.isDead()) {
                    newPlayer.server.execute(() -> handleRespawnSync(newPlayer));
                }
            });
        });
    }

    private void handleRespawnSync(ServerPlayerEntity player) {
        if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
            ServerTransferUtil.sendToLimbo(player);
            return;
        }

        player.changeGameMode(GameMode.ADVENTURE);
        setGhostModeAttributes(player, true);
    }

    public static void setGhostModeAttributes(ServerPlayerEntity player, boolean isGhost) {
        player.setInvisible(isGhost);
        player.setInvulnerable(isGhost);
        player.getAbilities().allowFlying = false; // Ghosts cannot fly, they walk
        player.getAbilities().flying = false;
        player.sendAbilitiesUpdate();

        // Also add custom ghost effects (darkness, cave sounds) from the DLC
        if (isGhost) {
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.DARKNESS, 60, 0, false, false));
        }
    }
}
