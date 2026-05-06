package org.ssoggy.ssoggysouls.listener;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
public class LimboServerListener {

    private static DatabaseManager db;

    private LimboServerListener() {}

    public static void setDatabase(DatabaseManager database) {
        db = database;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (db == null || !(event.getEntity() instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();

        CompletableFuture.runAsync(() -> {
            PlayerData data = db.getPlayer(uuid);

            player.server.execute(() -> {
                if (data != null && data.isDead()) {
                    applyLimboState(player);
                } else {
                    player.setGameMode(GameType.SURVIVAL);
                    player.sendSystemMessage(MessageUtil.get("limbo-welcome-visitor"));
                }
            });
        });
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            event.setCanceled(true); // Cancel damage for ghosts/dead players
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

        // Teleport to specific limbo spawn location config
        ConfigManager.ModConfig cfg = ConfigManager.getConfig();
        ResourceLocation worldId = ResourceLocation.parse(cfg.getLimboSpawnWorld());
        ServerLevel world = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, worldId));
        if (world != null) {
            player.teleportTo(world, cfg.getLimboSpawnX(), cfg.getLimboSpawnY(), cfg.getLimboSpawnZ(), cfg.getLimboSpawnYaw(), cfg.getLimboSpawnPitch());
        }

        player.sendSystemMessage(MessageUtil.get("limbo-welcome-dead"));
    }
}
