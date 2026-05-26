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

            List<GlobalPos> playerHeadLocations = headBlockLocations.get(player.getUuid());
            if (playerHeadLocations == null) {
                playerHeadLocations = new ArrayList<>();
                headBlockLocations.put(player.getUuid(), playerHeadLocations);
            }
            playerHeadLocations.add(GlobalPos.create(world.getRegistryKey(), headPos));
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
        List<GlobalPos> knownLocations = headBlockLocations.get(ownerUuid);
        if (knownLocations != null) {
            List<GlobalPos> remainingLocations = new ArrayList<>();
            for (GlobalPos pos : knownLocations) {
                ServerWorld world = server.getWorld(pos.dimension());
                if (world == null) {
                    remainingLocations.add(pos);
                    continue;
                }

                BlockPos blockPos = pos.pos();
                if (!world.isChunkLoaded(blockPos)) {
                    // We skip if not loaded to prevent loading tons of chunks.
                    remainingLocations.add(pos);
                    continue;
                }

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

            if (remainingLocations.isEmpty()) {
                headBlockLocations.remove(ownerUuid);
            } else {
                headBlockLocations.put(ownerUuid, remainingLocations);
            }
        }

        for (ServerWorld world : server.getWorlds()) {
            for (net.minecraft.entity.Entity entity : world.iterateEntities()) {
                if (entity instanceof ItemEntity itemEntity) {
                    ItemStack stack = itemEntity.getStack();
                    if (stack.isOf(Items.PLAYER_HEAD) && stack.contains(DataComponentTypes.PROFILE)) {
                        ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
                        if (profile != null && profile.id().isPresent() && profile.id().get().equals(ownerUuid)) {
                            itemEntity.discard();
                        }
                    }
                }
            }
        }
    }
}
