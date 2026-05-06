package org.ssoggy.ssoggysouls.listener;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
public class ServerLifecycleListener {

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

            final PlayerData finalData = data;
            player.server.execute(() -> handleJoinSync(player, finalData));
        });
    }

    private static void handleJoinSync(ServerPlayer player, PlayerData data) {
        GlobalPos pending = org.ssoggy.ssoggysouls.hrm.RevivalStructureListener.consumePendingRevival(player.getUUID());
        if (pending != null) {
            setGhostModeAttributes(player, false);
            ServerLevel targetWorld = player.server.getLevel(pending.dimension());
            org.ssoggy.ssoggysouls.hrm.RevivalStructureListener.restoreAtStructure(player, targetWorld != null ? targetWorld : player.serverLevel(), pending.pos());
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
                player.sendSystemMessage(MessageUtil.get("ghost-mode-active"));
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

        CompletableFuture.runAsync(() -> {
            PlayerData data = db.getPlayer(uuid);
            if (data == null) return;

            if (data.isInGracePeriod(ConfigManager.parseGracePeriod(ConfigManager.getConfig().getGracePeriod()))) {
                return;
            }

            int remaining = data.decrementLife();
            db.savePlayer(data);

            player.server.execute(() -> handleDeathSync(player, data, remaining));
        });
    }

    private static void handleDeathSync(ServerPlayer player, PlayerData data, int remaining) {
        if (data.isDead()) {
            if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
                player.sendSystemMessage(MessageUtil.get("death-sending-to-limbo"));
                ServerTransferUtil.sendToLimbo(player);
                return;
            }

            player.setGameMode(GameType.ADVENTURE);
            setGhostModeAttributes(player, true);
            player.sendSystemMessage(MessageUtil.get("death-now-ghost"));

            // Head drops triggered here (Ported in Phase 4)
        } else {
            player.sendSystemMessage(MessageUtil.get("death-life-lost", "lives", remaining));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (db == null || !(event.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID uuid = player.getUUID();

        CompletableFuture.runAsync(() -> {
            PlayerData data = db.getPlayer(uuid);
            if (data != null && data.isDead()) {
                player.server.execute(() -> handleRespawnSync(player));
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
                    net.minecraft.world.effect.MobEffects.DARKNESS, 60, 0, false, false));
        }
    }
}
