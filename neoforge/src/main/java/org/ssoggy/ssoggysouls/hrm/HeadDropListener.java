package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.util.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HeadDropListener {

    private HeadDropListener() {
        // Utility class
    }

    private static final Map<UUID, List<GlobalPos>> headBlockLocations = new ConcurrentHashMap<>();

    public static void register() {
        // Head drop is triggered from ServerLifecycleListener
    }

    public static void triggerHeadDrop(ServerPlayer player) {
        dropHead(player);
    }

    private static void dropHead(ServerPlayer player) {
        if (player.isCreative() && !ConfigManager.getConfig().isCreativePlayersDropHeads()) {
            return;
        }

        Level world = player.level();
        BlockPos pos = player.blockPosition();

        if (ConfigManager.getConfig().isHeadPlaceAsBlock() || !ConfigManager.getConfig().isHeadBurnsInLava()) {
            BlockPos headPos = findSafeBlockPos(world, pos);
            world.setBlock(headPos, Blocks.PLAYER_HEAD.defaultBlockState(), 3);
            BlockEntity be = world.getBlockEntity(headPos);
            if (be instanceof SkullBlockEntity skull) {
                skull.setOwner(new ResolvableProfile(player.getGameProfile()));
                skull.setChanged();
            }

            headBlockLocations
                    .computeIfAbsent(player.getUUID(), k -> new ArrayList<>())
                    .add(GlobalPos.of(world.dimension(), headPos));
            SSoggySoulsMod.LOGGER.info("Placed {}'s head at {} {} {}", player.getScoreboardName(), headPos.getX(), headPos.getY(), headPos.getZ());
        } else {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
            head.set(DataComponents.CUSTOM_NAME,
                    Component.literal(player.getScoreboardName() + "'s Head")
                    .withStyle(net.minecraft.ChatFormatting.YELLOW));

            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, head);

            if (ConfigManager.getConfig().isHeadFireproof()) {
                itemEntity.setInvulnerable(true);
            }
            if (ConfigManager.getConfig().isHeadNoDespawn()) {
                itemEntity.setUnlimitedLifetime();
            }

            world.addFreshEntity(itemEntity);
            SSoggySoulsMod.LOGGER.info("Dropped {}'s head at {} {} {}", player.getScoreboardName(), pos.getX(), pos.getY(), pos.getZ());
        }
    }

    private static BlockPos findSafeBlockPos(Level world, BlockPos origin) {
        BlockPos.MutableBlockPos mutable = origin.mutable();
        for (int i = 0; i < 10; i++) {
            if (world.getBlockState(mutable).isAir()) return mutable.immutable();
            mutable.move(0, 1, 0);
        }
        return origin;
    }

    public static void removeDroppedHeads(UUID ownerUuid, MinecraftServer server) {
        List<GlobalPos> knownLocations = headBlockLocations.remove(ownerUuid);
        if (knownLocations != null) {
            removeTrackedHeadBlocks(ownerUuid, server, knownLocations);
        }
        scanWorldForItemEntities(ownerUuid, server);
    }

    private static void removeTrackedHeadBlocks(UUID ownerUuid, MinecraftServer server, List<GlobalPos> knownLocations) {
        for (GlobalPos pos : knownLocations) {
            ServerLevel world = server.getLevel(pos.dimension());
            if (world == null) continue;

            BlockPos blockPos = pos.pos();
            if (!world.isLoaded(blockPos)) {
                continue;
            }

            if (world.getBlockState(blockPos).getBlock() == Blocks.PLAYER_HEAD ||
                world.getBlockState(blockPos).getBlock() == Blocks.PLAYER_WALL_HEAD) {
                BlockEntity be = world.getBlockEntity(blockPos);
                if (be instanceof SkullBlockEntity skull) {
                    ResolvableProfile ownerProfile = skull.getOwnerProfile();
                    if (ownerProfile != null && ownerProfile.id().isPresent() && ownerProfile.id().get().equals(ownerUuid)) {
                        world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static void scanWorldForItemEntities(UUID ownerUuid, MinecraftServer server) {
        for (ServerLevel world : server.getAllLevels()) {
            for (net.minecraft.world.entity.Entity entity : world.getAllEntities()) {
                if (entity instanceof ItemEntity itemEntity) {
                    ItemStack stack = itemEntity.getItem();
                    if (stack.is(Items.PLAYER_HEAD) && stack.has(DataComponents.PROFILE)) {
                        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
                        if (profile != null && profile.id().isPresent() && profile.id().get().equals(ownerUuid)) {
                            itemEntity.discard();
                        }
                    }
                }
            }
        }
    }
}
