package org.ssoggy.ssoggysouls.hrm.dlc.listener;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcDeaths;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcNames;
import org.ssoggy.ssoggysouls.hrm.dlc.util.GhostState;
import org.ssoggy.ssoggysouls.listener.MainServerListener;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GhostBlockEvents {

    private GhostBlockEvents() {
        // Utility class
    }

    public static void register(DatabaseManager db) {
        registerHeadBreak(db);
    }

    private static void registerHeadBreak(DatabaseManager db) {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer))
                return;

            if ((state.is(Blocks.PLAYER_HEAD) || state.is(Blocks.PLAYER_WALL_HEAD))
                    && blockEntity instanceof SkullBlockEntity skull) {
                handleHeadBreak(world, serverPlayer, skull, db);
            }
        });
    }

    private static void handleHeadBreak(Level world, ServerPlayer serverPlayer,
            SkullBlockEntity skull, DatabaseManager db) {
        ResolvableProfile profile = skull.getOwnerProfile();
        if (profile != null && profile.partialProfile().id() != null) {
            UUID ownerUuid = profile.partialProfile().id();
            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(ownerUuid);
                if (data != null && data.isDead()) {
                    world.getServer().execute(() -> {
                        GhostState ghostState = GhostState.getServerState(world.getServer());

                        ghostState.removeDeathLocation(ownerUuid);
                        ghostState.setDeathHolder(ownerUuid, serverPlayer.getUUID());
                        ghostState.setDirty();
                        DlcDeaths.setHolder(ownerUuid, serverPlayer.getUUID());
                        DlcNames.cache(serverPlayer.getUUID(), serverPlayer.getScoreboardName());

                        ServerPlayer ghost = world.getServer().getPlayerList().getPlayer(ownerUuid);
                        if (ghost != null) {
                            ghost.setGameMode(GameType.SPECTATOR);
                            ghost.setCamera(serverPlayer);
                            ghost.sendSystemMessage(Component.literal("Started spectating " + serverPlayer.getScoreboardName()).withStyle(net.minecraft.ChatFormatting.GRAY));
                            ghost.sendSystemMessage(Component.literal(serverPlayer.getScoreboardName() + " is currently carrying your playerhead...").withStyle(net.minecraft.ChatFormatting.YELLOW));
                        }
                    });
                }
            });
        }
    }

    public static void handleHeadPlace(Level world, UUID ownerUuid, BlockPos targetPos, DatabaseManager db) {
        BlockState state = world.getBlockState(targetPos);
        if (state.is(Blocks.PLAYER_HEAD) || state.is(Blocks.PLAYER_WALL_HEAD)) {
            BlockEntity be = world.getBlockEntity(targetPos);
            if (be instanceof SkullBlockEntity skull) {
                ResolvableProfile profile = skull.getOwnerProfile();
                if (profile != null && profile.partialProfile().id() != null && profile.partialProfile().id().equals(ownerUuid)) {
                    updateGhostStateOnPlace(world, ownerUuid, targetPos, db);
                }
            }
        }
    }

    private static void updateGhostStateOnPlace(Level world, UUID ownerUuid, BlockPos targetPos, DatabaseManager db) {
        GhostState ghostState = GhostState.getServerState(world.getServer());

        ghostState.removeDeathHolder(ownerUuid);
        ghostState.setDeathLocation(ownerUuid, targetPos);
        ghostState.setDirty();
        DlcDeaths.setHolder(ownerUuid, null);
        DlcDeaths.recordDeath(
                ownerUuid,
                DlcNames.getOrDefault(ownerUuid, ownerUuid.toString()),
                world.dimension().identifier().toString(),
                targetPos.getX(),
                targetPos.getY(),
                targetPos.getZ()
        );

        CompletableFuture.runAsync(() -> {
            PlayerData data = db.getPlayer(ownerUuid);
            if (data != null && data.isDead()) {
                world.getServer().execute(() -> {
                    ServerPlayer ghost = world.getServer().getPlayerList().getPlayer(ownerUuid);
                    if (ghost != null && ghost.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                        ghost.setGameMode(GameType.ADVENTURE);
                        MainServerListener.setGhostModeAttributes(ghost, true);

                        ghost.teleportTo((ServerLevel) world, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, java.util.Set.of(), ghost.getYRot(), ghost.getXRot(), true);
                        ghost.sendSystemMessage(Component.literal("Your head has been placed down.").withStyle(net.minecraft.ChatFormatting.GRAY));
                    }
                });
            }
        });
    }
}
