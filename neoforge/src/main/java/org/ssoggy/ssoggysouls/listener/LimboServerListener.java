package org.ssoggy.ssoggysouls.listener;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LimboServerListener {

    private static DatabaseManager db;

    private static final java.util.Set<String> WHITELISTED_COMMANDS = java.util.Set.of(
            "/msg", "/tell", "/r", "/reply", "/help", "/list",
            "/pstatus", "/psadmin", "/psa", "/revive", "/psetlives"
    );

    private LimboServerListener() {}

    public static void setDatabase(DatabaseManager database) {
        db = database;
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

    private static void setCachedDead(UUID uuid, boolean dead) {
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
                    PlayerData data = db.getPlayer(uuid);
                    // null = missing record or DB error: keep the last known status
                    if (data != null) {
                        setCachedDead(uuid, data.isDead());
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

    @SubscribeEvent
    public static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        onLimboTick(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        DEAD_PLAYERS.remove(event.getEntity().getUUID());
    }

    private static boolean isWhitelistedCommand(String fullCommand) {
        String clean = fullCommand.trim().toLowerCase(java.util.Locale.ROOT);
        String[] tokens = clean.split("\\s+");
        String command = tokens.length > 0 ? tokens[0] : "";
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (db == null || !(event.getEntity() instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();

        CompletableFuture.runAsync(() -> {
            // getPlayer (not isPlayerDead): a missing record means a first-time visitor, not a dead player
            PlayerData data = db.getPlayer(uuid);
            boolean isDead = data != null && data.isDead();
            setCachedDead(uuid, isDead);

            player.level().getServer().execute(() -> {
                if (isDead) {
                    applyLimboState(player);
                } else {
                    player.setGameMode(GameType.SURVIVAL);
                    player.sendSystemMessage(MessageUtil.get("limbo-welcome-visitor"));
                }
            });
        });
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        if (db == null) return;

        net.minecraft.commands.CommandSourceStack source = event.getParseResults().getContext().getSource();
        if (source.getEntity() instanceof ServerPlayer player) {
            String fullCommand = event.getParseResults().getReader().getString();

            if (isCachedDead(player.getUUID())) {
                String cmdToCheck = fullCommand.startsWith("/") ? fullCommand : "/" + fullCommand;
                if (!isWhitelistedCommand(cmdToCheck)) {
                    event.setCanceled(true);
                    player.sendSystemMessage(MessageUtil.get("limbo-cannot-leave"));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            event.setNewDamage(0.0F); // Prevent damage for ghosts/dead players
        }
    }

    @SubscribeEvent
    public static void onEntityTravel(EntityTravelToDimensionEvent event) {
        if (db == null) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            // Allow travel to the Limbo dimension (prevents blocking the initial death teleport)
            ConfigManager.ModConfig cfg = ConfigManager.getConfig();
            Identifier limboId = Identifier.tryParse(cfg.getLimboSpawnWorld());
            if (limboId != null && event.getDimension().identifier().equals(limboId)) return;

            // Check for bypass permission (parity with Fabric)
            if (player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) return;

            if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && isCachedDead(player.getUUID())) {
                event.setCanceled(true);
                player.sendSystemMessage(MessageUtil.get("limbo-cannot-leave"));
            }
        }
    }

    private static void applyLimboState(ServerPlayer player) {
        player.setGameMode(GameType.ADVENTURE);
        player.getInventory().clearContent();
        player.experienceLevel = 0;
        player.experienceProgress = 0;
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20f);

        ConfigManager.ModConfig cfg = ConfigManager.getConfig();
        Identifier worldId = Identifier.parse(cfg.getLimboSpawnWorld());
        ServerLevel world = player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, worldId));
        if (world != null) {
            player.teleportTo(world, cfg.getLimboSpawnX(), cfg.getLimboSpawnY(), cfg.getLimboSpawnZ(), java.util.Set.of(), cfg.getLimboSpawnYaw(), cfg.getLimboSpawnPitch(), true);
        }

        player.sendSystemMessage(MessageUtil.get("limbo-welcome-dead"));
    }
}
