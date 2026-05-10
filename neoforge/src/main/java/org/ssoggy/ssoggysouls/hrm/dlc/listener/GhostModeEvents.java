package org.ssoggy.ssoggysouls.hrm.dlc.listener;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
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
            player.server.execute(() -> {
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
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleCancelableEvent(event, event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        handleCancelableEvent(event, event.getEntity());
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        handleCancelableEvent(event, event.getEntity());
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        handleCancelableEvent(event, event.getPlayer());
    }

    private static void handleCancelableEvent(ICancellableEvent event, Player player) {
        if (!ConfigManager.getConfig().isHrmEnabled()) return;

        if (isGhost(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (!ConfigManager.getConfig().isHrmEnabled()) return;

        if (isGhost(event.getEntity())) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                serverPlayer.setCamera(event.getTarget());
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ConfigManager.getConfig().isHrmEnabled()) return;

        for (UUID uuid : GHOST_CACHE) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                enforceGhostRestrictions(player);
            }
        }
    }

    private static void enforceGhostRestrictions(ServerPlayer player) {
        UUID uuid = player.getUUID();
        GhostState state = GhostState.getServerState(player.server);

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
        // Port of Paper's onPlayerMove teleport feedback (sound + particles).
        player.teleportTo(player.serverLevel(), deathPos.getX() + 0.5, deathPos.getY(), deathPos.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.serverLevel().playSound(null, deathPos, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        if (ConfigManager.getConfig().isGhostModeParticles()) {
            player.serverLevel().sendParticles(ParticleTypes.DRAGON_BREATH,
                    deathPos.getX() + 0.5, deathPos.getY(), deathPos.getZ() + 0.5,
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
