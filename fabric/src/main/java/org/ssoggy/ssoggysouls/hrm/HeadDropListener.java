package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
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

    private static final Map<UUID, List<UUID>> headItemEntityUuids = new HashMap<>();

    public static void register() {
        // Head drop is now triggered from MainServerListener.handleDeathSync
        // when isDead becomes true, avoiding the race condition with async DB state.
    }

    /**
     * Triggers a head drop for the given player.
     * Must be called from the server thread.
     */
    public static void triggerHeadDrop(ServerPlayerEntity player) {
        dropHead(player);
    }

    private static void dropHead(ServerPlayerEntity player) {
        if (player.isCreative() && !ConfigManager.getConfig().isCreativePlayersDropHeads()) {
            return;
        }

        World world = player.getServerWorld();
        BlockPos pos = player.getBlockPos();

        // Handle placing as block vs dropping as item
        if (ConfigManager.getConfig().isHeadPlaceAsBlock() || !ConfigManager.getConfig().isHeadBurnsInLava()) {
            BlockPos headPos = findSafeBlockPos(world, pos);
            world.setBlockState(headPos, net.minecraft.block.Blocks.PLAYER_HEAD.getDefaultState());
            net.minecraft.block.entity.BlockEntity be = world.getBlockEntity(headPos);
            if (be instanceof net.minecraft.block.entity.SkullBlockEntity skull) {
                skull.setOwner(new ProfileComponent(player.getGameProfile()));
                skull.markDirty();
            }

            GhostState.getServerState(player.server).addHeadBlockLocation(player.getUuid(), GlobalPos.create(world.getRegistryKey(), headPos));
            SSoggySoulsMod.LOGGER.info("Placed {}'s head at {} {} {}", player.getName().getString(), headPos.getX(), headPos.getY(), headPos.getZ());
        } else {
            // Create player head item
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
            head.set(DataComponentTypes.CUSTOM_NAME,
                    Text.literal(player.getName().getString() + "'s Head")
                    .styled(style -> style.withColor(Formatting.YELLOW)));

            // Spawn item entity
            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, head);

            if (ConfigManager.getConfig().isHeadFireproof()) {
                itemEntity.setInvulnerable(true);
            }
            if (ConfigManager.getConfig().isHeadNoDespawn()) {
                itemEntity.setNeverDespawn();
            }

            world.spawnEntity(itemEntity);
            headItemEntityUuids
                    .computeIfAbsent(player.getUuid(), k -> new ArrayList<>())
                    .add(itemEntity.getUuid());
            SSoggySoulsMod.LOGGER.info("Dropped {}'s head at {} {} {}", player.getName().getString(), pos.getX(), pos.getY(), pos.getZ());
        }
    }

    private static BlockPos findSafeBlockPos(World world, BlockPos origin) {
        BlockPos.Mutable mutable = origin.mutableCopy();
        // Search up to 10 blocks up for air
        for (int i = 0; i < 10; i++) {
            if (world.getBlockState(mutable).isAir()) return mutable.toImmutable();
            mutable.move(0, 1, 0);
        }
        return origin;
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
            ServerWorld world = server.getWorld(pos.dimension());
            if (world == null) continue;

            BlockPos blockPos = pos.pos();
            world.getChunk(blockPos);

            if (world.getBlockState(blockPos).getBlock() == net.minecraft.block.Blocks.PLAYER_HEAD ||
                world.getBlockState(blockPos).getBlock() == net.minecraft.block.Blocks.PLAYER_WALL_HEAD) {
                net.minecraft.block.entity.BlockEntity be = world.getBlockEntity(blockPos);
                if (be instanceof net.minecraft.block.entity.SkullBlockEntity skull) {
                    ProfileComponent ownerProfile = skull.getOwner();
                    if (ownerProfile != null && ownerProfile.id().isPresent() && ownerProfile.id().get().equals(ownerUuid)) {
                        world.setBlockState(blockPos, net.minecraft.block.Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    private static void removeTrackedItemEntities(UUID ownerUuid, MinecraftServer server) {
        List<UUID> entityUuids = headItemEntityUuids.remove(ownerUuid);
        if (entityUuids == null) {
            return;
        }
        for (UUID entityUuid : entityUuids) {
            for (ServerWorld world : server.getWorlds()) {
                net.minecraft.entity.Entity entity = world.getEntity(entityUuid);
                if (entity instanceof ItemEntity itemEntity) {
                    itemEntity.discard();
                    break;
                }
            }
        }
    }
}
