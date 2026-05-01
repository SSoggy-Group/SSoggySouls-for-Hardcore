package org.ssoggy.ssoggysouls.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
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
import java.util.concurrent.CompletableFuture;

public class LimboServerListener {

    private final DatabaseManager db;

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
                        applyLimboState(player);
                    } else {
                        player.changeGameMode(GameMode.SURVIVAL);
                        player.sendMessage(MessageUtil.getNoPrefix("Welcome to Limbo as a visitor!"), false);
                    }
                });
            });
        });

        // Cancel Damage
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player && player.interactionManager.getGameMode() == GameMode.ADVENTURE) {
                return false;
            }
            return true;
        });
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
        
        player.sendMessage(MessageUtil.getNoPrefix("Welcome to Limbo. You are dead!"), false);
    }
}
