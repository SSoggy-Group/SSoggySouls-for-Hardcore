package org.ssoggy.ssoggysouls.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LimboServerListener {

    private static DatabaseManager db;

    private static final String LIMBO_CANNOT_LEAVE_MESSAGE = "limbo-cannot-leave";

    private static final Set<String> WHITELISTED_COMMANDS = Set.of(
            "/msg", "/tell", "/r", "/reply", "/help", "/list",
            "/pstatus", "/psadmin", "/psa", "/revive", "/psetlives"
    );

    private LimboServerListener() {
        registerJoinEvent();
        registerCancelDamageEvent();
        registerWorldChangeEvent();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(LimboServerListener::onLimboTick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, ignoredServer) -> {
            DEAD_PLAYERS.remove(handler.getPlayer().getUUID());
            CACHE_WRITES.remove(handler.getPlayer().getUUID());
        });
    }

    // Death status cache. Command/portal/level-change handlers run on the server thread
    // and must not block on a DB round-trip; the cache is filled on join and refreshed
    // asynchronously every few seconds.
    private static final Set<UUID> DEAD_PLAYERS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final int DEAD_CACHE_REFRESH_TICKS = 100;
    private static final int VOID_CHECK_TICKS = 20;

    private static boolean isCachedDead(UUID uuid) {
        return DEAD_PLAYERS.contains(uuid);
    }

    // Per-player count of authoritative cache writes (join, /psetlives, /revive). A refresh
    // records it before its async read and only applies its result if no write happened
    // meanwhile, so a stale read can't overwrite a newer update.
    private static final java.util.Map<UUID, Long> CACHE_WRITES = new java.util.concurrent.ConcurrentHashMap<>();

    /** Also called by /psetlives and /revive so Limbo restrictions update without waiting for a refresh. */
    public static void setCachedDead(UUID uuid, boolean dead) {
        CACHE_WRITES.compute(uuid, (k, writes) -> {
            applyCachedDead(uuid, dead);
            return writes == null ? 1L : writes + 1;
        });
    }

    private static void applyRefreshedDead(UUID uuid, boolean dead, long writesSeen) {
        CACHE_WRITES.compute(uuid, (k, writes) -> {
            if ((writes == null ? 0L : writes) == writesSeen) {
                applyCachedDead(uuid, dead);
            }
            return writes;
        });
    }

    private static void applyCachedDead(UUID uuid, boolean dead) {
        if (dead) {
            DEAD_PLAYERS.add(uuid);
        } else {
            DEAD_PLAYERS.remove(uuid);
        }
    }

    private static void onLimboTick(MinecraftServer server) {
        int tick = server.getTickCount();
        if (tick % VOID_CHECK_TICKS == 0) {
            rescueFromVoid(server);
        }
        if (db != null && tick % DEAD_CACHE_REFRESH_TICKS == 0) {
            java.util.List<UUID> online = server.getPlayerList().getPlayers().stream().map(ServerPlayer::getUUID).toList();
            if (online.isEmpty()) return;
            CompletableFuture.runAsync(() -> {
                for (UUID uuid : online) {
                    long writesSeen = CACHE_WRITES.getOrDefault(uuid, 0L);
                    try {
                        // A successful read with no record is a visitor; only a failed read keeps
                        // the last known status (otherwise a fail-closed join would stick forever)
                        PlayerData data = db.getPlayerStrict(uuid);
                        applyRefreshedDead(uuid, data != null && data.isDead(), writesSeen);
                    } catch (java.sql.SQLException e) {
                        // keep the last known status
                    }
                }
            });
        }
    }

    /**
     * Limbo cancels all damage for ADVENTURE players, including void damage, so a ghost
     * that falls out of the world would fall forever. Bring them back to the limbo spawn.
     */
    private static void rescueFromVoid(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE
                    && player.getY() < player.level().getMinY()) {
                ConfigManager.ModConfig cfg = ConfigManager.getConfig();
                Identifier worldId = Identifier.tryParse(cfg.getLimboSpawnWorld());
                ServerLevel limbo = worldId == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, worldId));
                if (limbo != null) {
                    player.teleportTo(limbo, cfg.getLimboSpawnX(), cfg.getLimboSpawnY(), cfg.getLimboSpawnZ(), Set.of(), cfg.getLimboSpawnYaw(), cfg.getLimboSpawnPitch(), true);
                } else {
                    net.minecraft.core.BlockPos spawn = server.getRespawnData().pos();
                    player.teleportTo(server.overworld(), spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), true);
                }
                player.resetFallDistance();
            }
        }
    }

    public static void register(DatabaseManager db) {
        LimboServerListener.db = db;
        new LimboServerListener();
    }

    private void registerJoinEvent() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            UUID uuid = player.getUUID();
            // Restricted until the lookup answers, so the join window can't be used to escape
            DEAD_PLAYERS.add(uuid);

            CompletableFuture.runAsync(() -> {
                PlayerData data;
                try {
                    data = db.getPlayerStrict(uuid);
                } catch (java.sql.SQLException e) {
                    // A failed read keeps the player restricted (as the old isPlayerDead did)
                    org.ssoggy.ssoggysouls.SSoggySoulsMod.LOGGER.warn("Could not load {} on Limbo join; treating as dead", player.getScoreboardName(), e);
                    data = new PlayerData(uuid, player.getScoreboardName(), 0, true, 0L, 0L, 0L, 0L);
                }
                final PlayerData finalData = data;
                server.execute(() -> handleJoinSync(player, finalData, server));
            });
        });
    }

    private void handleJoinSync(ServerPlayer player, PlayerData data, MinecraftServer server) {
        UUID uuid = player.getUUID();
        if (server.getPlayerList().getPlayer(uuid) == null) {
            return;
        }
        setCachedDead(uuid, data != null && data.isDead());
        if (data != null && data.isDead()) {
            applyLimboState(player);
        } else {
            player.setGameMode(GameType.SURVIVAL);
            player.sendSystemMessage(MessageUtil.get("limbo-welcome-visitor"));
        }
    }

    private void registerCancelDamageEvent() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, ignoredSource, ignoredAmount) ->
            !(entity instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
        );
    }

    private void registerWorldChangeEvent() {
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, ignoredOrigin, destination) -> {
            if (db == null || !isCachedDead(player.getUUID())) {
                return;
            }

            ConfigManager.ModConfig cfg = ConfigManager.getConfig();
            Identifier worldId = Identifier.tryParse(cfg.getLimboSpawnWorld());
            if (worldId == null) {
                return;
            }
            ResourceKey<Level> limboWorldKey = ResourceKey.create(Registries.DIMENSION, worldId);
            if (destination.dimension().equals(limboWorldKey)) {
                return;
            }
            ServerLevel limboWorld = destination.getServer().getLevel(limboWorldKey);
            if (limboWorld != null) {
                player.teleportTo(limboWorld, cfg.getLimboSpawnX(), cfg.getLimboSpawnY(), cfg.getLimboSpawnZ(), Set.of(), cfg.getLimboSpawnYaw(), cfg.getLimboSpawnPitch(), true);
                player.sendSystemMessage(MessageUtil.get(LIMBO_CANNOT_LEAVE_MESSAGE));
            }
        });
    }

    private static boolean isWhitelistedCommand(String message) {
        String[] tokens = message.trim().split("\\s+");
        String command = tokens.length > 0 ? tokens[0].toLowerCase(Locale.ROOT) : "";
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }

    public static boolean shouldBlockCommand(ServerPlayer player, String command) {
        if (db == null) return false;
        
        String fullCmd = "/" + command;
        if (isCachedDead(player.getUUID()) && !isWhitelistedCommand(fullCmd)) {
            player.sendSystemMessage(MessageUtil.get(LIMBO_CANNOT_LEAVE_MESSAGE));
            return true;
        }
        return false;
    }

    public static boolean shouldBlockPortal(ServerPlayer player, ServerLevel destination) {
        if (db == null) return false;
        // Only portal-driven dimension changes are blocked; same-level teleports (commands, revival) pass through
        if (destination == player.level() || player.portalProcess == null || !player.portalProcess.isInsidePortalThisTick()) return false;
        if (player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) return false;

        ConfigManager.ModConfig cfg = ConfigManager.getConfig();
        Identifier worldId = Identifier.tryParse(cfg.getLimboSpawnWorld());
        if (worldId != null && destination.dimension().identifier().equals(worldId)) {
            return false;
        }

        if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && isCachedDead(player.getUUID())) {
            player.sendSystemMessage(MessageUtil.get(LIMBO_CANNOT_LEAVE_MESSAGE));
            return true;
        }
        return false;
    }

    private void applyLimboState(ServerPlayer player) {
        player.setGameMode(GameType.ADVENTURE);
        player.getInventory().clearContent();
        player.experienceLevel = 0;
        player.experienceProgress = 0;
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);

        ConfigManager.ModConfig cfg = ConfigManager.getConfig();
        Identifier worldId = Identifier.tryParse(cfg.getLimboSpawnWorld());
        if (worldId != null) {
            ServerLevel world = player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, worldId));
            if (world != null) {
                player.teleportTo(world, cfg.getLimboSpawnX(), cfg.getLimboSpawnY(), cfg.getLimboSpawnZ(), java.util.Set.of(), cfg.getLimboSpawnYaw(), cfg.getLimboSpawnPitch(), true);
            }
        }

        player.sendSystemMessage(MessageUtil.get("limbo-welcome-dead"));
    }
}
