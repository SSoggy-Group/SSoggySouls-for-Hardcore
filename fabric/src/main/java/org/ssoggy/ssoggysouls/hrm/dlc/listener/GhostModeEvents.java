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
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.dlc.util.GhostState;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class GhostModeEvents {

    private GhostModeEvents() {
        // Utility class
    }

    private static final Set<UUID> GHOST_CACHE = ConcurrentHashMap.newKeySet();

    public static void register(DatabaseManager db) {
        registerLifecycleEvents(db);
        registerInteractionEvents();
        registerTickEvents();
    }

    private static void registerLifecycleEvents(DatabaseManager db) {
        // Populate cache on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            UUID uuid = handler.getPlayer().getUuid();
            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(uuid);
                boolean isDead = data != null && data.isDead();
                server.execute(() -> {
                    if (isDead) {
                        GHOST_CACHE.add(uuid);
                    } else {
                        GHOST_CACHE.remove(uuid);
                    }
                });
            });
        });

        // Clean up cache on disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> GHOST_CACHE.remove(handler.getPlayer().getUuid()));
    }

    private static void registerInteractionEvents() {
        // Prevent block interaction
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (isGhost(player)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        // Prevent item usage
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (isGhost(player)) {
                return TypedActionResult.fail(player.getStackInHand(hand));
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });

        // Prevent attacking entities
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (isGhost(player)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        // Prevent interacting with entities
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (isGhost(player)) {
                // DLC logic: start spectating the entity if right clicked
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.setCameraEntity(entity);
                }
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    private static void registerTickEvents() {
        // Handle movement restriction via ticking (since Fabric lacks a PlayerMoveEvent)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (isGhost(player)) {
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
        GhostState state = GhostState.getServerState(player.getServer());

        // If someone is carrying their head, they are spectating them. Do not restrict distance.
        if (state.deathHolders.containsKey(uuid)) {
            return;
        }

        if (state.deathLocations.containsKey(uuid)) {
            BlockPos deathPos = state.deathLocations.get(uuid);
            BlockPos currentPos = player.getBlockPos();

            double distanceSq = currentPos.getSquaredDistance(deathPos);
            double maxDistance = ConfigManager.getConfig().getSpectatorHeadRestrictRadius();

            if (distanceSq > (maxDistance * maxDistance)) {
                player.teleport(player.getServerWorld(), deathPos.getX() + 0.5, deathPos.getY(), deathPos.getZ() + 0.5, player.getYaw(), player.getPitch());
                player.sendMessage(net.minecraft.text.Text.literal("You may not travel that far away from your death location").styled(s -> s.withColor(net.minecraft.util.Formatting.GRAY)), true);
            }
        }
    }

    public static void updateGhostStatus(UUID uuid, boolean isDead) {
        if (isDead) GHOST_CACHE.add(uuid);
        else GHOST_CACHE.remove(uuid);
    }

    private static boolean isGhost(PlayerEntity player) {
        return GHOST_CACHE.contains(player.getUuid());
    }
}
