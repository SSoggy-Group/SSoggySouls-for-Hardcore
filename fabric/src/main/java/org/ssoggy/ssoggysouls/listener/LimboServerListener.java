package org.ssoggy.ssoggysouls.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.UUID;
import java.util.Set;
import java.util.Locale;
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
    }

    public static void register(DatabaseManager db) {
        LimboServerListener.db = db;
        new LimboServerListener();
    }

    private void registerJoinEvent() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID uuid = player.getUuid();

            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(uuid);
                server.execute(() -> handleJoinSync(player, data, server));
            });
        });
    }

    private void handleJoinSync(ServerPlayerEntity player, PlayerData data, MinecraftServer server) {
        UUID uuid = player.getUuid();
        if (server.getPlayerManager().getPlayer(uuid) == null) {
            return;
        }
        if (data != null && data.isDead()) {
            applyLimboState(player);
        } else {
            player.changeGameMode(GameMode.SURVIVAL);
            player.sendMessage(MessageUtil.get("limbo-welcome-visitor"), false);
        }
    }

    private void registerCancelDamageEvent() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, ignoredSource, ignoredAmount) ->
            !(entity instanceof ServerPlayerEntity player && player.interactionManager.getGameMode() == GameMode.ADVENTURE)
        );
    }

    private void registerWorldChangeEvent() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, ignoredOrigin, destination) -> {
            if (db == null || !db.isPlayerDead(player.getUuid())) {
                return;
            }

            ConfigManager.ModConfig cfg = ConfigManager.getConfig();
            Identifier worldId = Identifier.tryParse(cfg.getLimboSpawnWorld());
            if (worldId == null) {
                return;
            }
            RegistryKey<World> limboWorldKey = RegistryKey.of(RegistryKeys.WORLD, worldId);
            if (destination.getRegistryKey().equals(limboWorldKey)) {
                return;
            }
            ServerWorld limboWorld = player.getServer().getWorld(limboWorldKey);
            if (limboWorld != null) {
                player.teleport(limboWorld, cfg.getLimboSpawnX(), cfg.getLimboSpawnY(), cfg.getLimboSpawnZ(), cfg.getLimboSpawnYaw(), cfg.getLimboSpawnPitch());
                player.sendMessage(MessageUtil.get(LIMBO_CANNOT_LEAVE_MESSAGE), false);
            }
        });
    }

    private static boolean isWhitelistedCommand(String message) {
        String trimmed = message.trim();
        int spaceIdx = trimmed.indexOf(' ');
        String command = (spaceIdx == -1 ? trimmed : trimmed.substring(0, spaceIdx)).toLowerCase(Locale.ROOT);
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }

    public static boolean shouldBlockCommand(ServerPlayerEntity player, String command) {
        if (db == null) return false;
        
        String fullCmd = "/" + command;
        if (db.isPlayerDead(player.getUuid()) && !isWhitelistedCommand(fullCmd)) {
            player.sendMessage(MessageUtil.get(LIMBO_CANNOT_LEAVE_MESSAGE), false);
            return true;
        }
        return false;
    }

    public static boolean shouldBlockPortal(ServerPlayerEntity player, ServerWorld destination) {
        if (db == null) return false;
        if (player.hasPermissionLevel(2)) return false; // Basic bypass logic placeholder

        // Allow travel to the Limbo dimension (prevents blocking the initial death teleport)
        ConfigManager.ModConfig cfg = ConfigManager.getConfig();
        Identifier worldId = Identifier.tryParse(cfg.getLimboSpawnWorld());
        if (worldId != null && destination.getRegistryKey().getValue().equals(worldId)) {
            return false;
        }

        // Only intercept portal-triggered travel to avoid blocking server-driven transfers.
        if (player.interactionManager.getGameMode() == GameMode.ADVENTURE && player.portalManager.isInPortal() && db.isPlayerDead(player.getUuid())) {
            player.sendMessage(MessageUtil.get(LIMBO_CANNOT_LEAVE_MESSAGE), false);
            return true;
        }
        return false;
    }

    private void applyLimboState(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.experienceLevel = 0;
        player.experienceProgress = 0;
        player.setHealth(player.getMaxHealth());
        player.getHungerManager().setFoodLevel(20);
                player.sendMessage(MessageUtil.get(LIMBO_CANNOT_LEAVE_MESSAGE), false);

        ConfigManager.ModConfig cfg = ConfigManager.getConfig();
        Identifier worldId = Identifier.tryParse(cfg.getLimboSpawnWorld());
        if (worldId != null) {
            ServerWorld world = player.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, worldId));
            if (world != null) {
                player.teleport(world, cfg.getLimboSpawnX(), cfg.getLimboSpawnY(), cfg.getLimboSpawnZ(), cfg.getLimboSpawnYaw(), cfg.getLimboSpawnPitch());
            }
        }

        player.sendMessage(MessageUtil.get("limbo-welcome-dead"), false);
    }
}
