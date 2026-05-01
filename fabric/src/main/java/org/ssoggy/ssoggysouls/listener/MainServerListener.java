package org.ssoggy.ssoggysouls.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostModeEvents;
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

    private final SSoggySoulsMod plugin;
    // Assume database manager is available from Mod plugin instance.
    // For now we will pass it or fetch it. We will need a getter in SSoggySoulsMod.
    private DatabaseManager db;

    public MainServerListener(SSoggySoulsMod plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        registerJoinEvent();
        registerQuitEvent();
        registerDeathEvent();
        registerRespawnEvent();
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
                            plugin.getDefaultLives(), graceMs); 
                    db.savePlayer(data);
                } else {
                    data.setUsername(player.getName().getString());
                    db.savePlayer(data);
                }

                final PlayerData finalData = data;
                server.execute(() -> handleJoinSync(player, finalData));
            });
        });
    }

    private void handleJoinSync(ServerPlayerEntity player, PlayerData data) {
        if (data.isDead()) {
            if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
                ServerTransferUtil.sendToLimbo(player);
                return;
            }

            // Dead player joined -> Ghost mode (Adventure)
            if (player.interactionManager.getGameMode() != GameMode.ADVENTURE) {
                player.changeGameMode(GameMode.ADVENTURE);
                setGhostModeAttributes(player, true);
                player.sendMessage(MessageUtil.getNoPrefix("You are a ghost!"), false);
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

            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(uuid);
                if (data == null) return; // Should not happen if they joined

                if (data.isInGracePeriod(ConfigManager.parseGracePeriod(ConfigManager.getConfig().getGracePeriod()))) {
                    return; // Grace period protects from life loss
                }

                int remaining = data.decrementLife();
                db.savePlayer(data);

                player.server.execute(() -> handleDeathSync(player, data, remaining));
            });
        });
    }

    private void handleDeathSync(ServerPlayerEntity player, PlayerData data, int remaining) {
        if (data.isDead()) {
            if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
                player.sendMessage(MessageUtil.getNoPrefix("You have died! Sending to Limbo..."), false);
                ServerTransferUtil.sendToLimbo(player);
                return;
            }

            GhostState state = GhostState.getServerState(player.getServer());
            state.deathLocations.put(player.getUuid(), player.getBlockPos());
            state.markDirty();
            
            player.changeGameMode(GameMode.ADVENTURE);
            setGhostModeAttributes(player, true);
            GhostModeEvents.updateGhostStatus(player.getUuid(), true);
            player.sendMessage(MessageUtil.getNoPrefix("You have died! You are now a ghost."), false);
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
        if (ConfigManager.getConfig().sendToLimboOnDeath) {
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
