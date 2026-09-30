package org.ssoggy.ssoggysouls.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
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

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles core lifecycle events: joining, quitting, and dying.
 */
public class MainServerListener {

    // Deaths whose DB outcome is still being resolved. Respawns during that window are
    // left to finishDeath, which applies ghost state to whichever entity is current.
    private static final Set<UUID> DEATH_PENDING = ConcurrentHashMap.newKeySet();

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    public static final ExecutorService DB_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "ssoggysouls-db-" + THREAD_COUNTER.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    });

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
        ServerPlayConnectionEvents.JOIN.register((handler, ignoredSender, server) -> {
            ServerPlayer player = handler.getPlayer();
            UUID uuid = player.getUUID();
            String name = player.getScoreboardName();

            CompletableFuture.runAsync(() -> loadOnJoin(server, uuid, name), DB_EXECUTOR);
        });
    }

    private static final long JOIN_RETRY_SECONDS = 5L;

    private void loadOnJoin(MinecraftServer server, UUID uuid, String name) {
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
    private void pauseGracePeriodForOffline(PlayerData data, UUID uuid) {
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

    private void handleJoinSync(MinecraftServer server, UUID uuid, PlayerData data) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player == null) return;

        GlobalPos pending = RevivalStructureListener.consumePendingRevival(uuid);
        if (pending != null) {
            setGhostModeAttributes(player, false);
            ServerLevel targetWorld = server.getLevel(pending.dimension());
            RevivalStructureListener.restoreAtStructure(player, targetWorld != null ? targetWorld : player.level(), pending.pos());
            return;
        }

        // Set synchronously from the loaded data so interaction guards that read the
        // ghost cache can't race the separate async cache fill on join.
        GhostModeEvents.updateGhostStatus(uuid, data.isDead());
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

    private void registerQuitEvent() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, ignoredServer) -> {
            UUID uuid = handler.getPlayer().getUUID();
            long now = System.currentTimeMillis();
            CompletableFuture.runAsync(() -> db.setLastSeen(uuid, now), DB_EXECUTOR);
        });
    }

    private void registerDeathEvent() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }

            UUID uuid = player.getUUID();
            MinecraftServer server = player.level().getServer();
            ServerPlayer killer = damageSource.getEntity() instanceof ServerPlayer sp && sp != player ? sp : null;
            // Snapshot now: by the time the DB answers the player may have respawned elsewhere
            GlobalPos deathPos = GhostState.reachableDeathPos(player.level(), player.blockPosition());
            DEATH_PENDING.add(uuid);

            CompletableFuture.runAsync(() -> resolveDeath(server, player, deathPos, killer), DB_EXECUTOR);
        });
    }

    private static final long DEATH_RETRY_SECONDS = 5L;

    /**
     * Async part of the death flow. A failed DB read is retried (the death stays pending,
     * so an early respawn keeps waiting) rather than being treated as "no record", which
     * would silently drop the death.
     */
    private void resolveDeath(MinecraftServer server, ServerPlayer player, GlobalPos deathPos, ServerPlayer killer) {
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
    private void finishDeath(MinecraftServer server, ServerPlayer deadEntity, GlobalPos deathPos,
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

        GhostModeEvents.updateGhostStatus(uuid, true);
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

    private void registerRespawnEvent() {
        net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            UUID uuid = newPlayer.getUUID();
            // finishDeath will ghost the new entity once the outcome is known
            if (DEATH_PENDING.contains(uuid)) return;

            MinecraftServer server = newPlayer.level().getServer();
            CompletableFuture.runAsync(() -> {
                if (db.isPlayerDead(uuid)) {
                    server.execute(() -> {
                        ServerPlayer current = server.getPlayerList().getPlayer(uuid);
                        if (current != null) handleRespawnSync(current);
                    });
                }
            }, DB_EXECUTOR);
        });
    }

    private void handleRespawnSync(ServerPlayer player) {
        if (ConfigManager.getConfig().isSendToLimboOnDeath()) {
            ServerTransferUtil.sendToLimbo(player);
            return;
        }

        player.setGameMode(GameType.ADVENTURE);
        setGhostModeAttributes(player, true);
    }

    public static void setGhostModeAttributes(ServerPlayer player, boolean isGhost) {
        player.setInvisible(isGhost);
        player.setPermanentlyInvulnerable(isGhost);
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();

        if (isGhost) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
        }
    }
}
