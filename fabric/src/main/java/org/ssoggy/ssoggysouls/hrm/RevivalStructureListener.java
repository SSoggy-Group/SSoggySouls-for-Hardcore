package org.ssoggy.ssoggysouls.hrm;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RevivalStructureListener {

    private RevivalStructureListener() {
        // Utility class
    }

    public static void register(DatabaseManager db) {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            return handleStructureInteraction(serverPlayer, world, hand, hitResult, db);
        });
    }

    private static ActionResult handleStructureInteraction(ServerPlayerEntity serverPlayer, World world, Hand hand, net.minecraft.util.hit.BlockHitResult hitResult, DatabaseManager db) {
        ItemStack stack = serverPlayer.getStackInHand(hand);
        if (!stack.isOf(Items.PLAYER_HEAD)) {
            return ActionResult.PASS;
        }

        ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
        Optional<UUID> ownerId = profile.id();
        if (!ownerId.isPresent()) {
            return ActionResult.PASS;
        }

        UUID ownerUuid = ownerId.get();
        BlockPos placedPos = hitResult.getBlockPos().offset(hitResult.getSide());

        // Check if ritual structure is valid underneath where the head is about to be placed
        if (!isRitualStructure(world, placedPos)) {
            if (checkIncompleteStructure(world, placedPos)) {
                serverPlayer.sendMessage(MessageUtil.getNoPrefix("The revival structure is incomplete!"), false);
                world.playSound(null, placedPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.4f, 2f);
            }
            return ActionResult.PASS; // Let them place the head normally if not doing ritual
        }

        // The structure is valid. Prevent block placement, consume head, and trigger async DB check.
        stack.decrement(1);
        triggerRevival(serverPlayer, world, placedPos, ownerUuid, db);

        // Return SUCCESS to indicate we handled the interaction (preventing standard block placement)
        return ActionResult.SUCCESS;
    }

    private static void triggerRevival(ServerPlayerEntity serverPlayer, World world, BlockPos placedPos, UUID ownerUuid, DatabaseManager db) {
        CompletableFuture.runAsync(() -> {
            PlayerData data = db.getPlayer(ownerUuid);
            if (data == null) {
                serverPlayer.server.execute(() -> sendError(serverPlayer, "Unknown player."));
                return;
            }

            if (!data.isDead()) {
                serverPlayer.server.execute(() -> {
                    sendError(serverPlayer, data.getUsername() + " is not dead!");
                    world.playSound(null, placedPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.4f, 2f);
                });
                return;
            }

            boolean success = db.revivePlayer(ownerUuid, ConfigManager.getConfig().getOnReviveLives());
            if (!success) {
                serverPlayer.server.execute(() -> sendError(serverPlayer, "Failed to revive. Check console."));
                return;
            }

            SSoggySoulsMod.LOGGER.info("{} revived {} via ritual structure!", serverPlayer.getName().getString(), data.getUsername());

            serverPlayer.server.execute(() -> performRevival(world, placedPos, serverPlayer, ownerUuid, data.getUsername()));
        });
    }

    private static void performRevival(World world, BlockPos placedPos, ServerPlayerEntity summoner, UUID revivedUuid, String revivedName) {
        breakStructure(world, placedPos);

        // Strike lightning
        if (ConfigManager.getConfig().isRitualLightningStrike()) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(placedPos.toCenterPos());
                world.spawnEntity(lightning);
            }
        }

        summoner.sendMessage(MessageUtil.get("admin-revive-success", "player", revivedName), false);

        ServerPlayerEntity revived = summoner.server.getPlayerManager().getPlayer(revivedUuid);
        if (revived != null) {
            restoreAtStructure(revived, placedPos);
        } else {
            summoner.sendMessage(MessageUtil.get("revive-success", "player", revivedName), false);
        }
    }

    private static void restoreAtStructure(ServerPlayerEntity revived, BlockPos spawnPos) {
        revived.teleport(revived.getServerWorld(), spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        revived.changeGameMode(GameMode.SURVIVAL);
        revived.sendMessage(MessageUtil.get("revive-success"), false);

        revived.clearStatusEffects();
        revived.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 4, false, true));
        revived.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0, false, true));

        // Totem effect
        if (ConfigManager.getConfig().isRitualTotemEffect()) {
            revived.getWorld().sendEntityStatus(revived, (byte) 35); // Status 35 is totem effect
        }
    }

    private static boolean isRitualStructure(World world, BlockPos headPos) {
        int hx = headPos.getX();
        int hy = headPos.getY();
        int hz = headPos.getZ();

        // fence below da head
        if (!world.getBlockState(new BlockPos(hx, hy - 1, hz)).isIn(BlockTags.FENCES)) return false;

        // ore block below the fence
        if (!world.getBlockState(new BlockPos(hx, hy - 2, hz)).isIn(BlockTags.BEACON_BASE_BLOCKS)) return false;

        int by = hy - 2;
        // soul sand corners
        if (!isSoulSand(world, hx - 1, by, hz - 1)) return false;
        if (!isSoulSand(world, hx + 1, by, hz - 1)) return false;
        if (!isSoulSand(world, hx - 1, by, hz + 1)) return false;
        if (!isSoulSand(world, hx + 1, by, hz + 1)) return false;

        // stair edges
        if (!isStair(world, hx, by, hz - 1)) return false;
        if (!isStair(world, hx - 1, by, hz)) return false;
        if (!isStair(world, hx + 1, by, hz)) return false;
        if (!isStair(world, hx, by, hz + 1)) return false;

        // wither roses on the corners on the soul sand
        int my = hy - 1;
        if (!isWitherRose(world, hx - 1, my, hz - 1)) return false;
        if (!isWitherRose(world, hx + 1, my, hz - 1)) return false;
        if (!isWitherRose(world, hx - 1, my, hz + 1)) return false;
        return isWitherRose(world, hx + 1, my, hz + 1);
    }

    private static boolean checkIncompleteStructure(World world, BlockPos headPos) {
        int hx = headPos.getX();
        int hy = headPos.getY();
        int hz = headPos.getZ();
        BlockState fence = world.getBlockState(new BlockPos(hx, hy - 1, hz));
        BlockState ore = world.getBlockState(new BlockPos(hx, hy - 2, hz));

        return fence.isIn(BlockTags.FENCES) && ore.isIn(BlockTags.BEACON_BASE_BLOCKS);
    }

    private static boolean isSoulSand(World world, int x, int y, int z) {
        BlockState state = world.getBlockState(new BlockPos(x, y, z));
        return state.isOf(Blocks.SOUL_SAND) || state.isOf(Blocks.SOUL_SOIL);
    }

    private static boolean isStair(World world, int x, int y, int z) {
        return world.getBlockState(new BlockPos(x, y, z)).isIn(BlockTags.STAIRS);
    }

    private static boolean isWitherRose(World world, int x, int y, int z) {
        return world.getBlockState(new BlockPos(x, y, z)).isOf(Blocks.WITHER_ROSE);
    }

    private static void breakStructure(World world, BlockPos headPos) {
        int hx = headPos.getX();
        int hy = headPos.getY();
        int hz = headPos.getZ();

        // fence + 4 roses
        setAir(world, hx, hy - 1, hz);
        setAir(world, hx - 1, hy - 1, hz - 1);
        setAir(world, hx + 1, hy - 1, hz - 1);
        setAir(world, hx - 1, hy - 1, hz + 1);
        setAir(world, hx + 1, hy - 1, hz + 1);

        if (!ConfigManager.getConfig().isLeaveStructureBase()) {
            // base
            int by = hy - 2;
            setAir(world, hx, by, hz);
            setAir(world, hx - 1, by, hz - 1);
            setAir(world, hx + 1, by, hz - 1);
            setAir(world, hx - 1, by, hz + 1);
            setAir(world, hx + 1, by, hz + 1);
            setAir(world, hx, by, hz - 1);
            setAir(world, hx - 1, by, hz);
            setAir(world, hx + 1, by, hz);
            setAir(world, hx, by, hz + 1);
        }
    }

    private static void setAir(World world, int x, int y, int z) {
        world.breakBlock(new BlockPos(x, y, z), false);
    }

    private static void sendError(ServerPlayerEntity player, String message) {
        player.sendMessage(MessageUtil.getNoPrefix(message), false);
    }
}
