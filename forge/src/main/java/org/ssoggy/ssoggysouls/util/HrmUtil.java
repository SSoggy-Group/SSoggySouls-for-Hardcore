package org.ssoggy.ssoggysouls.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.ssoggy.ssoggysouls.database.DatabaseManager;

/**
 * Utility class for Hardcore Revival Mechanic (HRM) helpers to reduce code duplication.
 */
public final class HrmUtil {

    private HrmUtil() {
        // Utility class
    }

    /**
     * Common validation helper for HRM (Hardcore Revival Mechanic) events to avoid code duplication.
     * Checks if HRM is enabled, if the database manager is registered, if we are on the server side,
     * and if the event entity is a ServerPlayer.
     *
     * @param event The player interact event
     * @param db    The database manager
     * @return The ServerPlayer instance if all checks pass, otherwise null
     */
    public static ServerPlayer getValidServerPlayer(PlayerInteractEvent event, DatabaseManager db) {
        if (!ConfigManager.getConfig().isHrmEnabled()) {
            return null;
        }

        if (db == null
                || event == null
                || event.getLevel() == null
                || event.getLevel().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return null;
        }

        return serverPlayer;
    }
}
