package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.util.ConfigManager;

public class HeadDropListener {

    private HeadDropListener() {
        // Utility class
    }

    public static void register(DatabaseManager db) {
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
        World world = player.getServerWorld();
        BlockPos pos = player.getBlockPos();

        // Handle placing as block vs dropping as item
        if (ConfigManager.getConfig().isHeadPlaceAsBlock()) {
            BlockPos headPos = findSafeBlockPos(world, pos);
            world.setBlockState(headPos, net.minecraft.block.Blocks.PLAYER_HEAD.getDefaultState());
            net.minecraft.block.entity.BlockEntity be = world.getBlockEntity(headPos);
            if (be instanceof net.minecraft.block.entity.SkullBlockEntity skull) {
                skull.setOwner(new ProfileComponent(player.getGameProfile()));
                skull.markDirty();
            }
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
            itemEntity.setInvulnerable(true);
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
}
