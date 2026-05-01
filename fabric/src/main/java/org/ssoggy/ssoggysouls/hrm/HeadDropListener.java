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

import java.util.concurrent.CompletableFuture;

public class HeadDropListener {

    public static void register(SSoggySoulsMod plugin, DatabaseManager db) {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            
            // Check db asynchronously to avoid blocking death
            CompletableFuture.runAsync(() -> {
                PlayerData data = db.getPlayer(player.getUuid());
                if (data == null || !data.isDead() || data.isInGracePeriod(24 * 60 * 60 * 1000L)) {
                    return; // Don't drop head if not dead or in grace
                }

                // Drop head on the main thread
                player.server.execute(() -> {
                    World world = player.getServerWorld();
                    BlockPos pos = player.getBlockPos();

                    // Create player head item
                    ItemStack head = new ItemStack(Items.PLAYER_HEAD);
                    
                    // Set player profile (texture)
                    head.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
                    
                    // Set custom name
                    head.set(DataComponentTypes.CUSTOM_NAME, 
                            Text.literal(player.getName().getString() + "'s Head")
                            .styled(style -> style.withColor(Formatting.YELLOW)));

                    // Spawn item entity
                    ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, head);
                    // Make fireproof
                    itemEntity.setInvulnerable(true); 
                    
                    world.spawnEntity(itemEntity);
                    SSoggySoulsMod.LOGGER.info("Dropped {}'s head at {} {} {}", player.getName().getString(), pos.getX(), pos.getY(), pos.getZ());
                });
            });
        });
    }
}
