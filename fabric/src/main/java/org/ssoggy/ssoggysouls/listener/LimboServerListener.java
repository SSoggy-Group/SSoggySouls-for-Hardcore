package org.ssoggy.ssoggysouls.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
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

import java.util.UUID;
import java.util.Set;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class LimboServerListener {

    private final DatabaseManager db;
    private static final Set<UUID> limboDeadPlayers = ConcurrentHashMap.newKeySet();

    private static final Set<String> WHITELISTED_COMMANDS = Set.of(
            "/msg", "/tell", "/r", "/reply", "/help", "/list",
            "/pstatus", "/psadmin", "/psa", "/revive", "/psetlives"
    );

    public LimboServerListener(DatabaseManager db) {
        this.db = db;
        registerJoinEvent();
        registerDisconnectEvent();
        registerCommandRestrictionEvent();
        registerCancelDamageEvent();
        registerWorldChangeEvent();
    }

    public static void updateLimboStatus(UUID uuid, boolean isDead) {
        if (isDead) {
            limboDeadPlayers.add(uuid);
        } else {
            limboDeadPlayers.remove(uuid);
        }
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
            limboDeadPlayers.add(uuid);
            applyLimboState(player);
        } else {
            limboDeadPlayers.remove(uuid);
            player.changeGameMode(GameMode.SURVIVAL);
            player.sendMessage(MessageUtil.get("limbo-welcome-visitor"), false);
        }
    }

    private void registerDisconnectEvent() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> limboDeadPlayers.remove(handler.getPlayer().getUuid()));
    }

    private void registerCommandRestrictionEvent() {
        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, unusedParams) -> {
            if (source.getEntity() instanceof ServerPlayerEntity player
                    && limboDeadPlayers.contains(player.getUuid())
                    && !isWhitelistedCommand(message.getContent().getString())) {
                player.sendMessage(MessageUtil.get("limbo-cannot-leave"), false);
                return false;
            }
            return true;
        });
    }

    private void registerCancelDamageEvent() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
            !(entity instanceof ServerPlayerEntity player && player.interactionManager.getGameMode() == GameMode.ADVENTURE)
        );
    }

    private void registerWorldChangeEvent() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (!limboDeadPlayers.contains(player.getUuid())) {
                return;
            }

            ConfigManager.ModConfig cfg = ConfigManager.getConfig();
            Identifier worldId = Identifier.tryParse(cfg.getLimboSpawnWorld());
            if (worldId == null) {
                return;
            }
            RegistryKey<World> limboWorldKey = RegistryKey.of(RegistryKeys.WORLD, worldId);
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
        String[] tokens = message.trim().split("\\s+");
        String command = tokens.length > 0 ? tokens[0].toLowerCase(Locale.ROOT) : "";
        return WHITELISTED_COMMANDS.contains(command);
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
