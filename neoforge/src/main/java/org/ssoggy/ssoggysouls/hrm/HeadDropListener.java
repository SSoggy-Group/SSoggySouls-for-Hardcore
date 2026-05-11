package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

public class HeadDropListener {

    private HeadDropListener() {
        // Utility class
    }

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
}
