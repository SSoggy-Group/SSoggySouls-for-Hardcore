package org.ssoggy.ssoggysouls.hrm;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
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
import org.ssoggy.ssoggysouls.model.PlayerData;

import org.ssoggy.ssoggysouls.util.ConfigManager;

import java.util.concurrent.CompletableFuture;

public class HeadDropListener {

    public static void register(SSoggySoulsMod plugin, DatabaseManager db) {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            
            // Check db asynchronously to avoid blocking death
            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(player.getUuid());
                if (data == null || !data.isDead() || data.isInGracePeriod(ConfigManager.parseGracePeriod(ConfigManager.getConfig().gracePeriod))) {
                    return; // Don't drop head if not dead or in grace
                }

                if (!ConfigManager.getConfig().dropHeads) {
                    return;
                }

                // Drop head on the main thread
                    // Handle placing as block vs dropping as item
                    if (ConfigManager.getConfig().headPlaceAsBlock) {
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
                });
            });
        });
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
