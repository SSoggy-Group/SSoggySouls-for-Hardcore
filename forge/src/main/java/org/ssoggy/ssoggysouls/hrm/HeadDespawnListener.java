package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = SSoggySoulsMod.MODID)
public class HeadDespawnListener {

    private static DatabaseManager db;

    public static void register(DatabaseManager database) {
        db = database;
    }

    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent event) {
        if (!ConfigManager.getConfig().isDropHeads() || ConfigManager.getConfig().isHeadPlaceAsBlock() || !ConfigManager.getConfig().isHeadNoDespawn()) {
            return;
        }

        ItemEntity itemEntity = event.getEntity();
        ItemStack stack = itemEntity.getItem();

        if (stack.getItem() != Items.PLAYER_HEAD) return;

        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        if (profile == null || profile.id().isEmpty()) return;

        UUID ownerUuid = profile.id().get();

        if (db != null) {
            PlayerData data = db.getPlayer(ownerUuid);
            if (data != null && data.isDead()) {
                event.setCanceled(true);
                // Also reset the age so it doesn't try to expire again immediately on next tick.
                itemEntity.setUnlimitedLifetime();
            }
        }
    }
}
