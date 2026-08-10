package org.ssoggy.ssoggysouls.listener;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;

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

    // ⚡ Bolt: Avoid regex .split("\\s+") to prevent redundant string array allocations and GC overhead on every command.
    private static boolean isWhitelistedCommand(String fullCommand) {
        int len = fullCommand.length();
        int start = 0;
        while (start < len && Character.isWhitespace(fullCommand.charAt(start))) {
            start++;
        }
        int spaceIdx = start;
        while (spaceIdx < len && !Character.isWhitespace(fullCommand.charAt(spaceIdx))) {
            spaceIdx++;
        }
        String command = start < spaceIdx ? fullCommand.substring(start, spaceIdx).toLowerCase(java.util.Locale.ROOT) : "";
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (db == null || !(event.getEntity() instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();

        CompletableFuture.runAsync(() -> {
            boolean isDead = db.isPlayerDead(uuid);

            player.server.execute(() -> {
                if (isDead) {
                    applyLimboState(player);
                } else {
                    player.setGameMode(GameType.SURVIVAL);
                    player.sendSystemMessage(MessageUtil.get("limbo-welcome-visitor"), false);
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

            if (db.isPlayerDead(player.getUUID())) {
                String cmdToCheck = fullCommand.startsWith("/") ? fullCommand : "/" + fullCommand;
                if (!isWhitelistedCommand(cmdToCheck)) {
                    event.setCanceled(true);
                    player.sendSystemMessage(MessageUtil.get("limbo-cannot-leave"), false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            event.setCanceled(true); // LivingDamageEvent usually still uses setCanceled in Forge if it implements ICancelable
        }
    }

    @SubscribeEvent
    public static void onEntityTravel(EntityTravelToDimensionEvent event) {
        if (db == null) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            // Allow travel to the Limbo dimension (prevents blocking the initial death teleport)
            ConfigManager.ModConfig cfg = ConfigManager.getConfig();
            ResourceLocation limboId = ResourceLocation.tryParse(cfg.getLimboSpawnWorld());
            if (limboId != null && event.getDimension().toString().contains(limboId.toString())) return;

            // Check for bypass permission (parity with Fabric)
            if (player.hasPermissions(2)) return;

            if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && db.isPlayerDead(player.getUUID())) {
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
        ResourceLocation worldId = ResourceLocation.parse(cfg.getLimboSpawnWorld());
        net.minecraft.server.level.ServerLevel world = player.server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, worldId));
        if (world != null) {
            player.teleportTo(world, cfg.getLimboSpawnX(), cfg.getLimboSpawnY(), cfg.getLimboSpawnZ(), java.util.Set.of(), cfg.getLimboSpawnYaw(), cfg.getLimboSpawnPitch());
        }

        player.sendSystemMessage(MessageUtil.get("limbo-welcome-dead"));
    }
}
