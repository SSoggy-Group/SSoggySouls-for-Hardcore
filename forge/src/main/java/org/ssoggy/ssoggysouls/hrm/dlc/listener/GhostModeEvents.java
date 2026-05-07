package org.ssoggy.ssoggysouls.hrm.dlc.listener;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.dlc.util.GhostState;
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
        if (event.getEntity() instanceof ServerPlayer player) {
            GHOST_CACHE.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (isGhost(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (isGhost(event.getEntity())) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                serverPlayer.setCamera(event.getTarget());
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (isGhost(player)) {
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
        if (deathPos != null) {
            BlockPos currentPos = player.blockPosition();

            double distanceSq = currentPos.distSqr(deathPos);
            double maxDistance = ConfigManager.getConfig().getSpectatorHeadrestrictRadius();

            if (distanceSq > (maxDistance * maxDistance)) {
                player.teleportTo(player.serverLevel(), deathPos.getX() + 0.5, deathPos.getY(), deathPos.getZ() + 0.5, player.getYRot(), player.getXRot());
                player.sendSystemMessage(Component.literal("You may not travel that far away from your death location").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
        }
    }

    public static void updateGhostStatus(UUID uuid, boolean isDead) {
        if (isDead) GHOST_CACHE.add(uuid);
        else GHOST_CACHE.remove(uuid);
    }

    private static boolean isGhost(Player player) {
        return GHOST_CACHE.contains(player.getUUID());
    }
}
