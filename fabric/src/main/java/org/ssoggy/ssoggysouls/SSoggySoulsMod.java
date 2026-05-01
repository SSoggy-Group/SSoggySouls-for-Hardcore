package org.ssoggy.ssoggysouls;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSoggySouls Fabric Mod — main entrypoint.
 * <p>
 * This is the Fabric equivalent of the Bukkit {@code SSoggySouls extends JavaPlugin}.
 * It initializes on both dedicated servers and singleplayer (integrated server).
 */
public class SSoggySoulsMod implements ModInitializer {

    public static final String MOD_ID = "ssoggysouls";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("SSoggySouls Fabric is loading...");

        // TODO Phase 2: Load config
        // TODO Phase 2: Initialize database (SQLite/MySQL)
        // TODO Phase 3: Register commands (Brigadier)
        // TODO Phase 3: Register event callbacks (deaths, joins, etc.)
        // TODO Phase 4: Register HRM features (recipes, head drops, structures)
        // TODO Phase 5: Initialize RevivalPlus DLC
        // TODO Phase 6: Set up cross-server (Velocity) support

        LOGGER.info("SSoggySouls Fabric loaded successfully!");
    }
}
