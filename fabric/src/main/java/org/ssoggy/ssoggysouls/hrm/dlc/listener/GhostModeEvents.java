package org.ssoggy.ssoggysouls.hrm.dlc.listener;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GhostModeEvents {

    // In-memory death tracker. In production, this should be serialized to a JSON file.
    public static final Map<UUID, BlockPos> DEATH_LOCATIONS = new HashMap<>();

    public static void register(SSoggySoulsMod plugin, DatabaseManager db) {
        
        // Prevent block interaction
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (isGhost(player, db)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        // Prevent item usage
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (isGhost(player, db)) {
                return TypedActionResult.fail(player.getStackInHand(hand));
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });

        // Prevent attacking entities
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (isGhost(player, db)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        // Prevent interacting with entities
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (isGhost(player, db)) {
                // DLC logic: start spectating the entity if right clicked
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.setCameraEntity(entity);
                    // Send message (optional)
                }
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        
        // Handle movement restriction via ticking (since Fabric lacks a PlayerMoveEvent)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (isGhost(player, db)) {
                    enforceGhostRestrictions(player);
                }
            }
        });
    }

    private static void enforceGhostRestrictions(ServerPlayerEntity player) {
        // Prevent dropping items
        if (player.currentScreenHandler != null && !player.currentScreenHandler.getCursorStack().isEmpty()) {
            // Note: Preventing physical drops natively often requires mixins into ServerPlayNetworkHandler.
            // As a quick workaround, we can clear dropped items immediately or clear the ghost's inventory.
        }

        UUID uuid = player.getUuid();
        if (DEATH_LOCATIONS.containsKey(uuid)) {
            BlockPos deathPos = DEATH_LOCATIONS.get(uuid);
            BlockPos currentPos = player.getBlockPos();
            
            double distanceSq = currentPos.getSquaredDistance(deathPos);
            double maxDistance = 16.0; // 16 block radius
            
            if (distanceSq > (maxDistance * maxDistance)) {
                player.teleport(player.getServerWorld(), deathPos.getX() + 0.5, deathPos.getY(), deathPos.getZ() + 0.5, player.getYaw(), player.getPitch());
                player.sendMessage(net.minecraft.text.Text.literal("You may not travel that far away from your death location").styled(s -> s.withColor(net.minecraft.util.Formatting.GRAY)), true);
            }
        }
    }

    /**
     * Checks if a player is considered a "Ghost" (dead in hardcore).
     * Since DB fetches are synchronous here, we should ideally cache the ghost status in the player entity,
     * but for now we do a quick lookup (SQLite is fast, but cache is better).
     */
    private static boolean isGhost(PlayerEntity player, DatabaseManager db) {
        if (player.getWorld().isClient) return false; // Only check server side
        
        // Simple cache or direct DB call.
        // In a production environment with Velocity, this should be cached locally upon join.
        PlayerData data = db.getPlayer(player.getUuid());
        return data != null && data.isDead();
    }
}
