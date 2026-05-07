package org.ssoggy.ssoggysouls.hrm;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.ssoggy.ssoggysouls.util.ConfigManager;

import java.util.UUID;

public class HeadDespawnListener {

    private static DatabaseManager db;

    public static void register(DatabaseManager database) {
        db = database;
    }

    // This gets called from a mixin to prevent despawning
    public static boolean shouldCancelDespawn(ItemEntity itemEntity) {
        if (!ConfigManager.getConfig().isDropHeads() || ConfigManager.getConfig().isHeadPlaceAsBlock() || !ConfigManager.getConfig().isHeadNoDespawn()) {
            return false;
        }

        ItemStack stack = itemEntity.getStack();
        if (!stack.isOf(Items.PLAYER_HEAD)) return false;

        ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
        if (profile == null || profile.id().isEmpty()) return false;

        UUID ownerUuid = profile.id().get();

        if (db != null) {
            PlayerData data = db.getPlayer(ownerUuid);
            if (data != null && data.isDead()) {
                return true;
            }
        }
        return false;
    }
}
