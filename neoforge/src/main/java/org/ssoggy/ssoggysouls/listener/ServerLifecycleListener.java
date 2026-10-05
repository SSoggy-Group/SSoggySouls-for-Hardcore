package org.ssoggy.ssoggysouls.listener;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
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

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerLifecycleListener {

    private static final int GHOST_MODE_DARKNESS_DURATION_TICKS = 60;

    // Deaths whose DB outcome is still being resolved. Respawns during that window are
    // left to finishDeath, which applies ghost state to whichever entity is current.
    private static final Set<UUID> DEATH_PENDING = ConcurrentHashMap.newKeySet();

    private static final ExecutorService DB_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "SSoggySouls-DB-IO");
        thread.setDaemon(true);
        return thread;
    });

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
        String name = player.getScoreboardName();
        MinecraftServer server = player.level().getServer();

        CompletableFuture.runAsync(() -> loadOnJoin(server, uuid, name), DB_EXECUTOR);
    }

    private static final long JOIN_RETRY_SECONDS = 5L;

    private static void loadOnJoin(MinecraftServer server, UUID uuid, String name) {
        PlayerData data;
        try {
            data = db.getPlayerStrict(uuid);
        } catch (java.sql.SQLException e) {
            // A failed read is not a first join: creating a record would overwrite the real one
            org.ssoggy.ssoggysouls.SSoggySoulsMod.LOGGER.warn("Could not load {} on join; retrying in {}s", name, JOIN_RETRY_SECONDS, e);
            CompletableFuture.runAsync(() -> {
                if (server.getPlayerList().getPlayer(uuid) != null) loadOnJoin(server, uuid, name);
            }, CompletableFuture.delayedExecutor(JOIN_RETRY_SECONDS, java.util.concurrent.TimeUnit.SECONDS, DB_EXECUTOR));
            return;
        }

        if (data == null) {
            long graceMs = ConfigManager.parseGracePeriod(ConfigManager.getConfig().getGracePeriod());
            data = PlayerData.createNew(uuid, name,
                    ConfigManager.getConfig().getDefaultLives(), graceMs);
            db.savePlayer(data);
        } else {
            // Targeted updates only: a full-row save here could overwrite a revive
            // or extra life written by another server/thread in the meantime.
            if (!Objects.equals(data.getUsername(), name)) {
                db.setUsername(uuid, name);
            }
            pauseGracePeriodForOffline(data, uuid);
        }
        DlcNames.cache(uuid, name);

        final PlayerData finalData = data;
        server.execute(() -> handleJoinSync(server, uuid, finalData));
    }

    /** Grace period does not tick while offline (matches the Paper implementation). */
    private static void pauseGracePeriodForOffline(PlayerData data, UUID uuid) {
        long lastSeen = data.getLastSeen();
        if (lastSeen <= 0) return;
        long now = System.currentTimeMillis();
        if (data.getGraceUntil() > lastSeen && now > lastSeen) {
            long adjusted = data.getGraceUntil() + (now - lastSeen);
            data.setGraceUntil(adjusted);
            db.setGraceUntil(uuid, adjusted);
        }
        data.setLastSeen(0L);
        db.setLastSeen(uuid, 0L);
    }

    private static void handleJoinSync(MinecraftServer server, UUID uuid, PlayerData data) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player == null) return;

        GlobalPos pending = org.ssoggy.ssoggysouls.hrm.RevivalStructureListener.consumePendingRevival(uuid);
        if (pending != null) {
            setGhostModeAttributes(player, false);
            ServerLevel targetWorld = server.getLevel(pending.dimension());
            org.ssoggy.ssoggysouls.hrm.RevivalStructureListener.restoreAtStructure(player, targetWorld != null ? targetWorld : player.level(), pending.pos());
            return;
        }

        // Set synchronously from the loaded data so interaction guards that read the
        // ghost cache can't race the separate async cache fill on join.
        org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostModeEvents.updateGhostStatus(uuid, data.isDead());
        if (data.isDead()) {
            if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
                ServerTransferUtil.sendToLimbo(player);
                return;
            }

            boolean alreadyGhost = player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE;
            player.setGameMode(GameType.ADVENTURE);
            // Always re-apply: invisibility is not persisted, so a relogging ghost
            // (already in ADVENTURE) would otherwise become visible.
            setGhostModeAttributes(player, true);
            if (!alreadyGhost) {
                player.sendSystemMessage(MessageUtil.get("ghost-mode-active"));
            }
        } else if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && player.isPermanentlyInvulnerable()) {
            // Only undo ghost state (marked by our persisted permanent invulnerability;
            // isInvulnerable() also covers post-join spawn protection); leave
            // players an admin deliberately put in ADVENTURE alone.
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
        CompletableFuture.runAsync(() -> db.setLastSeen(uuid, now), DB_EXECUTOR);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (db == null || !(event.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID uuid = player.getUUID();
        MinecraftServer server = player.level().getServer();
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer serverPlayer && serverPlayer != player
                ? serverPlayer : null;
        // Snapshot now: by the time the DB answers the player may have respawned elsewhere
        GlobalPos deathPos = GhostState.reachableDeathPos(player.level(), player.blockPosition());
        DEATH_PENDING.add(uuid);

        CompletableFuture.runAsync(() -> resolveDeath(server, player, deathPos, killer), DB_EXECUTOR);
    }

    private static final long DEATH_RETRY_SECONDS = 5L;

    /**
     * Async part of the death flow. A failed DB read is retried (the death stays pending,
     * so an early respawn keeps waiting) rather than being treated as "no record", which
     * would silently drop the death.
     */
    private static void resolveDeath(MinecraftServer server, ServerPlayer player, GlobalPos deathPos, ServerPlayer killer) {
        UUID uuid = player.getUUID();
        PlayerData data;
        try {
            data = db.getPlayerStrict(uuid);
        } catch (java.sql.SQLException e) {
            org.ssoggy.ssoggysouls.SSoggySoulsMod.LOGGER.warn("Could not load {} to resolve their death; retrying in {}s",
                    player.getScoreboardName(), DEATH_RETRY_SECONDS, e);
            CompletableFuture.runAsync(() -> resolveDeath(server, player, deathPos, killer),
                    CompletableFuture.delayedExecutor(DEATH_RETRY_SECONDS, java.util.concurrent.TimeUnit.SECONDS, DB_EXECUTOR));
            return;
        }

        int remaining = 0;
        if (data == null
                || data.isInGracePeriod(ConfigManager.parseGracePeriod(ConfigManager.getConfig().getGracePeriod()))) {
            data = null;
        } else {
            remaining = data.decrementLife();
            db.savePlayer(data);
            new DlcStats(uuid).incrementStat(DlcStat.DEATHS, 1);
            if (killer != null) {
                DlcNames.cache(killer.getUUID(), killer.getScoreboardName());
                new DlcStats(killer.getUUID()).incrementStat(DlcStat.KILLS, 1);
            }
        }

        final PlayerData finalData = data;
        final int finalRemaining = remaining;
        server.execute(() -> finishDeath(server, player, deathPos, finalData, finalRemaining));
    }

    /**
     * Applies the death outcome on the server thread. {@code deadEntity} is only used for
     * identity (profile/name); state changes go to the player's current entity, which is a
     * new object if they already respawned.
     */
    private static void finishDeath(MinecraftServer server, ServerPlayer deadEntity, GlobalPos deathPos,
                                    PlayerData data, int remaining) {
        UUID uuid = deadEntity.getUUID();
        DEATH_PENDING.remove(uuid);
        if (data == null) return; // grace period / unknown player: no life lost

        ServerPlayer current = server.getPlayerList().getPlayer(uuid);
        if (!data.isDead()) {
            if (current != null) {
                current.sendSystemMessage(MessageUtil.get("death-life-lost", "lives", remaining));
            }
            return;
        }

        // Drop the head in every mode (Paper does too), otherwise players sent to Limbo
        // could never be revived by ritual.
        if (ConfigManager.getConfig().isDropHeads()) {
            ServerLevel deathLevel = server.getLevel(deathPos.dimension());
            HeadDropListener.triggerHeadDrop(deadEntity, deathLevel != null ? deathLevel : server.overworld(), deathPos.pos());
        }

        if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
            if (current != null) {
                current.sendSystemMessage(MessageUtil.get("death-sending-to-limbo"));
                ServerTransferUtil.sendToLimbo(current);
            }
            return;
        }

        org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostModeEvents.updateGhostStatus(uuid, true);
        GhostState state = GhostState.getServerState(server);
        state.setDeathLocation(uuid, deathPos);
        state.setDirty();
        DlcDeaths.recordDeath(
                uuid,
                deadEntity.getScoreboardName(),
                deathPos.dimension().identifier().toString(),
                deathPos.pos().getX(),
                deathPos.pos().getY(),
                deathPos.pos().getZ()
        );

        if (current != null) {
            current.setGameMode(GameType.ADVENTURE);
            setGhostModeAttributes(current, true);
            current.sendSystemMessage(MessageUtil.get("death-now-ghost"));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (db == null || !(event.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID uuid = player.getUUID();
        // finishDeath will ghost the new entity once the outcome is known
        if (DEATH_PENDING.contains(uuid)) return;

        MinecraftServer server = player.level().getServer();
        CompletableFuture.runAsync(() -> {
            if (db.isPlayerDead(uuid)) {
                server.execute(() -> {
                    ServerPlayer current = server.getPlayerList().getPlayer(uuid);
                    if (current != null) handleRespawnSync(current);
                });
            }
        }, DB_EXECUTOR);
    }

    private static void handleRespawnSync(ServerPlayer player) {
        if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
            ServerTransferUtil.sendToLimbo(player);
            return;
        }

        player.setGameMode(GameType.ADVENTURE);
        setGhostModeAttributes(player, true);
    }

    @SuppressWarnings("deprecation")
    public static void setGhostModeAttributes(ServerPlayer player, boolean isGhost) {
        player.setInvisible(isGhost);
        player.setPermanentlyInvulnerable(isGhost);
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();

        if (isGhost) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DARKNESS, GHOST_MODE_DARKNESS_DURATION_TICKS, 0, false, false));
        }
    }
}
