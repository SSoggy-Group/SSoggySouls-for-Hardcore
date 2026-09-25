package org.ssoggy.ssoggysouls.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.GlobalPos;
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
    }

    public static void register(DatabaseManager db) {
        LimboServerListener.db = db;
        new LimboServerListener();
    }

    private void registerJoinEvent() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            UUID uuid = player.getUUID();

            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(uuid);
                server.execute(() -> handleJoinSync(player, data, server));
            });
        });
    }

    private void handleJoinSync(ServerPlayer player, PlayerData data, MinecraftServer server) {
        UUID uuid = player.getUUID();
        if (server.getPlayerList().getPlayer(uuid) == null) {
            return;
        }
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

    private static boolean isWhitelistedCommand(String message) {
        String[] tokens = message.trim().split("\\s+");
        String command = tokens.length > 0 ? tokens[0].toLowerCase(Locale.ROOT) : "";
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }

    public static boolean shouldBlockCommand(ServerPlayer player, String command) {
        if (db == null) return false;
        
        String fullCmd = "/" + command;
        if (db.isPlayerDead(player.getUUID()) && !isWhitelistedCommand(fullCmd)) {
            player.sendSystemMessage(MessageUtil.get(LIMBO_CANNOT_LEAVE_MESSAGE));
            return true;
        }
        return false;
    }

    public static boolean shouldBlockPortal(ServerPlayer player, ServerLevel destination) {
        if (db == null) return false;
        if (player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) return false;

        ConfigManager.ModConfig cfg = ConfigManager.getConfig();
        Identifier worldId = Identifier.tryParse(cfg.getLimboSpawnWorld());
        if (worldId != null && destination.dimension().identifier().equals(worldId)) {
            return false;
        }

        if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && db.isPlayerDead(player.getUUID())) {
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
