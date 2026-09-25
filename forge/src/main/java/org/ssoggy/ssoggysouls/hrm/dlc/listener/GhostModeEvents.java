package org.ssoggy.ssoggysouls.hrm.dlc.listener;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.dlc.util.GhostState;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.GhostRestrictionLogic;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class GhostModeEvents {

    private static final Set<UUID> GHOST_CACHE = ConcurrentHashMap.newKeySet();
    private static DatabaseManager db;

    private GhostModeEvents() {}

    public static void register(DatabaseManager database) {
        db = database;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ConfigManager.getConfig().isHrmEnabled()) return;

        if (db == null || !(event.getEntity() instanceof ServerPlayer player)) return;
        UUID uuid = player.getUUID();
        
        CompletableFuture.runAsync(() -> {
            PlayerData data = db.getPlayer(uuid);
            boolean isDead = data != null && data.isDead();
            player.level().getServer().execute(() -> {
                if (isDead) GHOST_CACHE.add(uuid);
                else GHOST_CACHE.remove(uuid);
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!ConfigManager.getConfig().isHrmEnabled()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            GHOST_CACHE.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static boolean onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        return isGhost(event.getEntity());
    }

    @SubscribeEvent
    public static boolean onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        return isGhost(event.getEntity());
    }

    @SubscribeEvent
    public static boolean onAttackEntity(AttackEntityEvent event) {
        return isGhost(event.getEntity());
    }

    @SubscribeEvent
    public static boolean onItemToss(ItemTossEvent event) {
        return isGhost(event.getPlayer());
    }

    @SubscribeEvent
    public static boolean onInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!ConfigManager.getConfig().isHrmEnabled()) return false;

        if (isGhost(event.getEntity())) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                serverPlayer.setCamera(event.getTarget());
            }
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent.Post event) {
        if (!ConfigManager.getConfig().isHrmEnabled()) return;

        for (UUID uuid : GHOST_CACHE) {
            ServerPlayer player = event.server().getPlayerList().getPlayer(uuid);
            if (player != null) {
                enforceGhostRestrictions(player);
            }
        }
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
        if (isDead) GHOST_CACHE.add(uuid);
        else GHOST_CACHE.remove(uuid);
    }

    private static boolean isGhost(Player player) {
        return GHOST_CACHE.contains(player.getUUID());
    }
}
