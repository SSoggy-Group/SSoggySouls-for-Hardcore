package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.hrm.dlc.util.GhostState;
import org.ssoggy.ssoggysouls.util.ConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HeadDropListener {

    private HeadDropListener() {
        // Utility class
    }

    private static final String HEAD_DROP_TAG = "ssoggysouls:death_head";

    private static final Map<UUID, List<UUID>> headItemEntityUuids = new HashMap<>();

    public static void register() {
        // Head drop is triggered from ServerLifecycleListener
    }

    /**
     * Drops/places the head at the recorded death position. {@code player} supplies only the
     * profile; its current position may already be the respawn point.
     */
    public static void triggerHeadDrop(ServerPlayer player, ServerLevel world, BlockPos pos) {
        dropHead(player, world, pos);
    }

    private static void dropHead(ServerPlayer player, Level world, BlockPos pos) {
        if (player.isCreative() && !ConfigManager.getConfig().isCreativePlayersDropHeads()) {
            return;
        }

        BlockPos headPos = ConfigManager.getConfig().isHeadPlaceAsBlock()
                ? findSafeBlockPos(world, pos) : null;
        if (headPos != null) {
            world.setBlock(headPos, Blocks.PLAYER_HEAD.defaultBlockState(), 3);
            BlockEntity be = world.getBlockEntity(headPos);
            if (be instanceof SkullBlockEntity skull) {
                ItemStack headItem = new ItemStack(Items.PLAYER_HEAD);
                headItem.set(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
                skull.applyComponentsFromItemStack(headItem);
                skull.setChanged();
            }

            GhostState.getServerState(player.level().getServer()).addHeadBlockLocation(player.getUUID(), GlobalPos.of(world.dimension(), headPos));
            SSoggySoulsMod.LOGGER.info("Placed {}'s head at {} {} {}", player.getScoreboardName(), headPos.getX(), headPos.getY(), headPos.getZ());
        } else {
            // Item drop (configured, or no free block to place into without destroying one)
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
            head.set(DataComponents.CUSTOM_NAME,
                    Component.literal(player.getScoreboardName() + "'s Head")
                    .withStyle(net.minecraft.ChatFormatting.YELLOW));

            // Persist provenance so revival cleanup can distinguish death drops after a restart.
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(HEAD_DROP_TAG, true);
            head.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, head);

            // Heads configured not to burn stay fireproof even when they fall back to an item
            if (ConfigManager.getConfig().isHeadFireproof() || !ConfigManager.getConfig().isHeadBurnsInLava()) {
                itemEntity.setPermanentlyInvulnerable(true);
            }
            if (ConfigManager.getConfig().isHeadNoDespawn()) {
                itemEntity.setUnlimitedLifetime();
            }

            world.addFreshEntity(itemEntity);
            headItemEntityUuids
                    .computeIfAbsent(player.getUUID(), k -> new ArrayList<>())
                    .add(itemEntity.getUUID());
            SSoggySoulsMod.LOGGER.info("Dropped {}'s head at {} {} {}", player.getScoreboardName(), pos.getX(), pos.getY(), pos.getZ());
        }
    }

    /**
     * First air/replaceable block at or above {@code origin}, or {@code null} if there is none
     * within range. Never returns an occupied block: overwriting it could delete a chest and
     * its contents (e.g. when a player suffocates inside blocks).
     */
    private static BlockPos findSafeBlockPos(Level world, BlockPos origin) {
        BlockPos.MutableBlockPos mutable = origin.mutable();
        for (int i = 0; i < 10; i++) {
            if (world.isOutsideBuildHeight(mutable)) return null;
            net.minecraft.world.level.block.state.BlockState state = world.getBlockState(mutable);
            if (state.isAir() || (state.canBeReplaced() && state.getFluidState().isEmpty())) {
                return mutable.immutable();
            }
            mutable.move(0, 1, 0);
        }
        return null;
    }

    public static void removeDroppedHeads(UUID ownerUuid, MinecraftServer server) {
        List<GlobalPos> knownLocations = GhostState.getServerState(server).consumeHeadBlockLocations(ownerUuid);
        if (knownLocations != null) {
            removeTrackedHeadBlocks(ownerUuid, server, knownLocations);
        }
        removeTrackedItemEntities(ownerUuid, server);
    }

    private static void removeTrackedHeadBlocks(UUID ownerUuid, MinecraftServer server, List<GlobalPos> knownLocations) {
        for (GlobalPos pos : knownLocations) {
            ServerLevel world = server.getLevel(pos.dimension());
            if (world == null) continue;

            BlockPos blockPos = pos.pos();
            world.getChunk(blockPos);

            if (world.getBlockState(blockPos).getBlock() == Blocks.PLAYER_HEAD ||
                world.getBlockState(blockPos).getBlock() == Blocks.PLAYER_WALL_HEAD) {
                BlockEntity be = world.getBlockEntity(blockPos);
                if (be instanceof SkullBlockEntity skull) {
                    ResolvableProfile ownerProfile = skull.getOwnerProfile();
                    if (ownerProfile != null && ownerProfile.partialProfile().id() != null && ownerProfile.partialProfile().id().equals(ownerUuid)) {
                        world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static void removeTrackedItemEntities(UUID ownerUuid, MinecraftServer server) {
        List<UUID> entityUuids = headItemEntityUuids.remove(ownerUuid);
        // Tracking is in-memory only; also sweep loaded item entities so heads dropped
        // before a restart are cleaned up too.
        for (ServerLevel world : server.getAllLevels()) {
            for (ItemEntity itemEntity : world.getEntities(
                    net.minecraft.world.level.entity.EntityTypeTest.forClass(ItemEntity.class),
                    e -> isOwnedHead(e.getItem(), ownerUuid))) {
                itemEntity.discard();
            }
        }
        if (entityUuids == null) {
            return;
        }
        for (UUID entityUuid : entityUuids) {
            for (ServerLevel world : server.getAllLevels()) {
                net.minecraft.world.entity.Entity entity = world.getEntity(entityUuid);
                if (entity instanceof ItemEntity itemEntity) {
                    itemEntity.discard();
                    break;
                }
            }
        }
    }

    private static boolean isOwnedHead(ItemStack stack, UUID ownerUuid) {
        if (!stack.is(Items.PLAYER_HEAD)) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.copyTag().getBoolean(HEAD_DROP_TAG).orElse(false)) return false;
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        return profile != null && ownerUuid.equals(profile.partialProfile().id());
    }
}
