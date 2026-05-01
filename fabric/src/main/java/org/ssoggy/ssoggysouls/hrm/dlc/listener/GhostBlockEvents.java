package org.ssoggy.ssoggysouls.hrm.dlc.listener;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.hrm.dlc.util.GhostState;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.util.UUID;

public class GhostBlockEvents {

    public static void register(SSoggySoulsMod plugin, DatabaseManager db) {
        
        // 1. Detect when a player breaks a player's head block
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) return;

            if ((state.isOf(Blocks.PLAYER_HEAD) || state.isOf(Blocks.PLAYER_WALL_HEAD)) && blockEntity instanceof SkullBlockEntity skull) {
                ProfileComponent profile = skull.getOwner();
                if (profile != null && profile.id().isPresent()) {
                    UUID ownerUuid = profile.id().get();
                    
                    PlayerData data = db.getPlayer(ownerUuid);
                    if (data != null && data.isDead()) {
                        GhostState ghostState = GhostState.getServerState(world.getServer());
                        
                        // The owner is a ghost! The breaker becomes the "Death Holder"
                        ghostState.deathLocations.remove(ownerUuid);
                        ghostState.deathHolders.put(ownerUuid, serverPlayer.getUuid());
                        ghostState.markDirty();

                        ServerPlayerEntity ghost = world.getServer().getPlayerManager().getPlayer(ownerUuid);
                        if (ghost != null) {
                            // Put ghost into spectator mode to follow the holder
                            ghost.changeGameMode(GameMode.SPECTATOR);
                            ghost.setCameraEntity(serverPlayer);
                            ghost.sendMessage(Text.literal("Started spectating " + serverPlayer.getName().getString()).styled(s -> s.withColor(Formatting.GRAY)), false);
                            ghost.sendMessage(Text.literal(serverPlayer.getName().getString() + " is currently carrying your playerhead...").styled(s -> s.withColor(Formatting.YELLOW)), true);
                        }
                    }
                }
            }
        });

        // 2. Detect when a player places a player's head block
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isOf(Items.PLAYER_HEAD)) return ActionResult.PASS;

            ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
            if (profile == null || profile.id().isEmpty()) return ActionResult.PASS;

            UUID ownerUuid = profile.id().get();
            BlockPos targetPos = hitResult.getBlockPos().offset(hitResult.getSide());

            // Schedule a check next tick to see if it was successfully placed
            world.getServer().execute(() -> {
                BlockState state = world.getBlockState(targetPos);
                if (state.isOf(Blocks.PLAYER_HEAD) || state.isOf(Blocks.PLAYER_WALL_HEAD)) {
                    BlockEntity be = world.getBlockEntity(targetPos);
                    if (be instanceof SkullBlockEntity skull && skull.getOwner() != null && skull.getOwner().id().isPresent()) {
                        if (skull.getOwner().id().get().equals(ownerUuid)) {
                            GhostState ghostState = GhostState.getServerState(world.getServer());
                            
                            // Block was placed! Update death location and remove holder
                            ghostState.deathHolders.remove(ownerUuid);
                            ghostState.deathLocations.put(ownerUuid, targetPos);
                            ghostState.markDirty();

                            PlayerData data = db.getPlayer(ownerUuid);
                            if (data != null && data.isDead()) {
                                ServerPlayerEntity ghost = world.getServer().getPlayerManager().getPlayer(ownerUuid);
                                if (ghost != null && ghost.interactionManager.getGameMode() == GameMode.SPECTATOR) {
                                    // Remove from spectator and put back into Ghost Mode restrictions
                                    ghost.changeGameMode(GameMode.ADVENTURE);
                                    org.ssoggy.ssoggysouls.listener.MainServerListener.setGhostModeAttributes(ghost, true);
                                    
                                    // Teleport to the newly placed head
                                    ghost.teleport(serverPlayer.getServerWorld(), targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, ghost.getYaw(), ghost.getPitch());
                                    ghost.sendMessage(Text.literal("Your head has been placed down.").styled(s -> s.withColor(Formatting.GRAY)), true);
                                }
                            }
                        }
                    }
                }
            });

            return ActionResult.PASS;
        });
        
        // 3. Detect when a player picks up a player head item
        net.fabricmc.fabric.api.entity.event.v1.EntityPickupItemCallback.EVENT.register((playerEntity, itemEntity) -> {
            if (playerEntity.getWorld().isClient || !(playerEntity instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ItemStack stack = itemEntity.getStack();
            if (!stack.isOf(Items.PLAYER_HEAD)) return ActionResult.PASS;

            ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
            if (profile == null || profile.id().isEmpty()) return ActionResult.PASS;

            UUID ownerUuid = profile.id().get();
            PlayerData data = db.getPlayer(ownerUuid);
            
            if (data != null && data.isDead()) {
                GhostState ghostState = GhostState.getServerState(serverPlayer.getServer());
                
                // The owner is a ghost! The picker-upper becomes the "Death Holder"
                ghostState.deathLocations.remove(ownerUuid);
                ghostState.deathHolders.put(ownerUuid, serverPlayer.getUuid());
                ghostState.markDirty();

                ServerPlayerEntity ghost = serverPlayer.getServer().getPlayerManager().getPlayer(ownerUuid);
                if (ghost != null) {
                    ghost.changeGameMode(GameMode.SPECTATOR);
                    ghost.setCameraEntity(serverPlayer);
                    ghost.sendMessage(Text.literal("Started spectating " + serverPlayer.getName().getString()).styled(s -> s.withColor(Formatting.GRAY)), false);
                    ghost.sendMessage(Text.literal(serverPlayer.getName().getString() + " is currently carrying your playerhead...").styled(s -> s.withColor(Formatting.YELLOW)), true);
                }
            }

            return ActionResult.PASS;
        });
    }
}
