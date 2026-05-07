package org.ssoggy.ssoggysouls.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;
import java.util.Set;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class LimboServerListener {

    private final DatabaseManager db;
    private final Set<UUID> limboDeadPlayers = ConcurrentHashMap.newKeySet();

    public LimboServerListener(DatabaseManager db) {
        this.db = db;
        registerEvents();
    }

    private void registerEvents() {
        // Player Join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID uuid = player.getUuid();

            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(uuid);

                server.execute(() -> {
                    if (data != null && data.isDead()) {
                        limboDeadPlayers.add(uuid);
                        applyLimboState(player);
                    } else {
                        limboDeadPlayers.remove(uuid);
                        player.changeGameMode(GameMode.SURVIVAL);
                        player.sendMessage(MessageUtil.get("limbo-welcome-visitor"), false);
                    }
                });
            });
        });

        // Cleanup tracking on disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, _server) ->
            limboDeadPlayers.remove(handler.getPlayer().getUuid())
        );

        // Restrict commands for dead players in Limbo mode
        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source) -> {
            if (source.getEntity() instanceof ServerPlayerEntity player
                    && limboDeadPlayers.contains(player.getUuid())
                    && !isWhitelistedCommand(message)) {
                player.sendMessage(MessageUtil.get("limbo-cannot-leave"), false);
                return false;
            }
            return true;
        });

        // Cancel Damage
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
            !(entity instanceof ServerPlayerEntity player && player.interactionManager.getGameMode() == GameMode.ADVENTURE)
        );

        // Prevent dead players from escaping Limbo through portals/dimension changes
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (!limboDeadPlayers.contains(player.getUuid())) {
                return;
            }

            ConfigManager.ModConfig cfg = ConfigManager.getConfig();
            Identifier worldId = Identifier.of(cfg.getLimboSpawnWorld());
            RegistryKey<ServerWorld> limboWorldKey = RegistryKey.of(RegistryKeys.WORLD, worldId);
            if (!destination.getRegistryKey().equals(limboWorldKey)) {
                ServerWorld limboWorld = player.getServer().getWorld(limboWorldKey);
                if (limboWorld != null) {
                    player.teleport(limboWorld, cfg.getLimboSpawnX(), cfg.getLimboSpawnY(), cfg.getLimboSpawnZ(), cfg.getLimboSpawnYaw(), cfg.getLimboSpawnPitch());
                    player.sendMessage(MessageUtil.get("limbo-cannot-leave"), false);
                }
            }
        });
    }

    private static boolean isWhitelistedCommand(String message) {
        String[] tokens = message.trim().toLowerCase(Locale.ROOT).split("\\s+");
        String command = tokens.length > 0 ? tokens[0] : "";
        return "/msg".equals(command) || "/tell".equals(command)
                || "/r".equals(command) || "/reply".equals(command)
                || "/help".equals(command) || "/list".equals(command)
                || "/pstatus".equals(command)
                || "/psadmin".equals(command) || "/psa".equals(command)
                || "/revive".equals(command) || "/psetlives".equals(command);
    }

    private void applyLimboState(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.experienceLevel = 0;
        player.experienceProgress = 0;
        player.setHealth(player.getMaxHealth());
        player.getHungerManager().setFoodLevel(20);
        player.getHungerManager().setSaturationLevel(20f);

        // Teleport to specific limbo spawn location config
        ConfigManager.ModConfig cfg = ConfigManager.getConfig();
        Identifier worldId = Identifier.of(cfg.getLimboSpawnWorld());
        ServerWorld world = player.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, worldId));
        if (world != null) {
            player.teleport(world, cfg.getLimboSpawnX(), cfg.getLimboSpawnY(), cfg.getLimboSpawnZ(), cfg.getLimboSpawnYaw(), cfg.getLimboSpawnPitch());
        }

        player.sendMessage(MessageUtil.get("limbo-welcome-dead"), false);
    }
}
