package org.ssoggy.ssoggysouls.listener;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.HeadDropListener;
import org.ssoggy.ssoggysouls.hrm.dlc.util.GhostState;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcDeaths;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcNames;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcStat;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcStats;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServerLifecycleListener {

    private static final int GHOST_MODE_DARKNESS_DURATION_TICKS = 60;
    
    private ServerLifecycleListener() {
        // Utility class
    }

    private static DatabaseManager db;

    public static void setDatabase(DatabaseManager database) {
        db = database;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (db == null || !(event.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID uuid = player.getUUID();

        CompletableFuture.runAsync(() -> {
            PlayerData data = db.getPlayer(uuid);
            if (data == null) {
                long graceMs = ConfigManager.parseGracePeriod(ConfigManager.getConfig().getGracePeriod());
                data = PlayerData.createNew(uuid, player.getScoreboardName(),
                        ConfigManager.getConfig().getDefaultLives(), graceMs);
                db.savePlayer(data);
            } else {
                data.setUsername(player.getScoreboardName());
                db.savePlayer(data);
            }
            DlcNames.cache(uuid, player.getScoreboardName());

            final PlayerData finalData = data;
            player.getServer().execute(() -> handleJoinSync(player, finalData));
        });
    }

    private static void handleJoinSync(ServerPlayer player, PlayerData data) {
        GlobalPos pending = org.ssoggy.ssoggysouls.hrm.RevivalStructureListener.consumePendingRevival(player.getUUID());
        if (pending != null) {
            setGhostModeAttributes(player, false);
            ServerLevel targetWorld = player.getServer().getLevel(pending.dimension());
            org.ssoggy.ssoggysouls.hrm.RevivalStructureListener.restoreAtStructure(player, targetWorld != null ? targetWorld : (ServerLevel) player.level(), pending.pos());
            return;
        }

        if (data.isDead()) {
            if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
                ServerTransferUtil.sendToLimbo(player);
                return;
            }

            if (player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) {
                player.setGameMode(GameType.ADVENTURE);
                setGhostModeAttributes(player, true);
                player.sendSystemMessage(MessageUtil.get("ghost-mode-active"), false);
            }
        } else if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            player.setGameMode(GameType.SURVIVAL);
            setGhostModeAttributes(player, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (db == null || !(event.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID uuid = player.getUUID();
        long now = System.currentTimeMillis();
        CompletableFuture.runAsync(() -> db.setLastSeen(uuid, now));
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (db == null || !(event.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID uuid = player.getUUID();
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer serverPlayer ? serverPlayer : null;

        CompletableFuture.runAsync(() -> {
            PlayerData data = db.getPlayer(uuid);
            if (data == null) return;

            if (data.isInGracePeriod(ConfigManager.parseGracePeriod(ConfigManager.getConfig().getGracePeriod()))) {
                return;
            }

            int remaining = data.decrementLife();
            db.savePlayer(data);
            new DlcStats(uuid).incrementStat(DlcStat.DEATHS, 1);
            if (killer != null) {
                DlcNames.cache(killer.getUUID(), killer.getScoreboardName());
                new DlcStats(killer.getUUID()).incrementStat(DlcStat.KILLS, 1);
            }

            player.getServer().execute(() -> handleDeathSync(player, data, remaining));
        });
    }

    private static void handleDeathSync(ServerPlayer player, PlayerData data, int remaining) {
        if (data.isDead()) {
            if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
                player.sendSystemMessage(MessageUtil.get("death-sending-to-limbo"), false);
                ServerTransferUtil.sendToLimbo(player);
                return;
            }

            player.setGameMode(GameType.ADVENTURE);
            setGhostModeAttributes(player, true);
            player.sendSystemMessage(MessageUtil.get("death-now-ghost"), false);
            org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostModeEvents.updateGhostStatus(player.getUUID(), true);
            GhostState state = GhostState.getServerState(player.getServer());
            state.setDeathLocation(player.getUUID(), player.blockPosition());
            state.setDirty();
            DlcDeaths.recordDeath(
                    player.getUUID(),
                    player.getScoreboardName(),
                    ((ServerLevel) player.level()).dimension().location().toString(),
                    player.blockPosition().getX(),
                    player.blockPosition().getY(),
                    player.blockPosition().getZ()
            );

            if (ConfigManager.getConfig().isDropHeads()) {
                HeadDropListener.triggerHeadDrop(player);
            }
        } else {
            player.sendSystemMessage(MessageUtil.get("death-life-lost", "lives", remaining), false);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (db == null || !(event.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID uuid = player.getUUID();

        CompletableFuture.runAsync(() -> {
            if (db.isPlayerDead(uuid)) {
                player.getServer().execute(() -> handleRespawnSync(player));
            }
        });
    }

    private static void handleRespawnSync(ServerPlayer player) {
        if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
            ServerTransferUtil.sendToLimbo(player);
            return;
        }

        player.setGameMode(GameType.ADVENTURE);
        setGhostModeAttributes(player, true);
    }

    public static void setGhostModeAttributes(ServerPlayer player, boolean isGhost) {
        player.setInvisible(isGhost);
        player.setInvulnerable(isGhost);
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();

        if (isGhost) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DARKNESS, GHOST_MODE_DARKNESS_DURATION_TICKS, 0, false, false));
        }
    }
}
