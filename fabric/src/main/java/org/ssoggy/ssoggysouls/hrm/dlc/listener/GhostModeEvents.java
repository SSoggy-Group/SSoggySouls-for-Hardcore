package org.ssoggy.ssoggysouls.hrm.dlc.listener;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.GhostRestrictionLogic;
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
        ServerPlayConnectionEvents.JOIN.register((handler, ignoredSender, server) -> {
            UUID uuid = handler.getPlayer().getUUID();
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

        ServerPlayConnectionEvents.DISCONNECT.register((handler, ignoredServer) -> GHOST_CACHE.remove(handler.getPlayer().getUUID()));
    }

    private static void registerInteractionEvents() {
        UseBlockCallback.EVENT.register((player, ignoredWorld, ignoredHand, ignoredHitResult) -> {
            if (isGhost(player)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, ignoredWorld, ignoredHand) -> {
            if (isGhost(player)) {
                return net.minecraft.world.InteractionResult.FAIL;
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, ignoredWorld, ignoredHand, ignoredEntity, ignoredHitResult) -> {
            if (isGhost(player)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, ignoredWorld, ignoredHand, entity, ignoredHitResult) -> {
            if (isGhost(player)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.setCamera(entity);
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }

    private static void registerTickEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!ConfigManager.getConfig().isHrmEnabled()) {
                return;
            }

            for (UUID uuid : GHOST_CACHE) {
                ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                if (player != null) {
                    enforceGhostRestrictions(player);
                }
            }
        });
    }

    private static void enforceGhostRestrictions(ServerPlayer player) {
        UUID uuid = player.getUUID();
        GhostState state = GhostState.getServerState(player.level().getServer());

        if (state.getDeathHolder(uuid) != null) {
            return;
        }

        BlockPos deathPos = state.getDeathLocation(uuid);
        if (deathPos == null) {
            return;
        }

        BlockPos currentPos = player.blockPosition();
        double maxDistance = ConfigManager.getConfig().getSpectatorHeadRestrictRadius();

        if (GhostRestrictionLogic.isOutOfBounds(deathPos.getX(), deathPos.getY(), deathPos.getZ(),
                currentPos.getX(), currentPos.getY(), currentPos.getZ(), maxDistance)) {
            applyTeleportFeedback(player, deathPos);
        }
    }

    private static void applyTeleportFeedback(ServerPlayer player, BlockPos deathPos) {
        player.teleportTo((ServerLevel) player.level(), deathPos.getX() + 0.5, deathPos.getY(), deathPos.getZ() + 0.5, java.util.Set.of(), player.getYRot(), player.getXRot(), true);

        player.connection.send(new ClientboundSoundPacket(
                net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.CHORUS_FRUIT_TELEPORT), SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(),
                1.0f, 1.0f, player.level().getRandom().nextLong()
        ));

        if (ConfigManager.getConfig().isGhostModeParticles()) {
            ((ServerLevel) player.level()).sendParticles(player,
                    net.minecraft.core.particles.PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0f),
                    true, false,
                    deathPos.getX() + 0.5, (double) deathPos.getY(), deathPos.getZ() + 0.5,
                    50, 0.0, 1.0, 0.0, 0.2);
        }

        player.sendSystemMessage(Component.literal(GhostRestrictionLogic.RESTRICTION_MESSAGE)
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    public static void updateGhostStatus(UUID uuid, boolean isDead) {
        if (isDead) {
            GHOST_CACHE.add(uuid);
        } else {
            GHOST_CACHE.remove(uuid);
        }
    }

    private static boolean isGhost(Player player) {
        return GHOST_CACHE.contains(player.getUUID());
    }
}
