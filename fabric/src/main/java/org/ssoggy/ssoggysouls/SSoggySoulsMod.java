package org.ssoggy.ssoggysouls;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * SSoggySouls Fabric Mod — main entrypoint.
 * <p>
 * This is the Fabric equivalent of the Bukkit {@code SSoggySouls extends JavaPlugin}.
 * It initializes on both dedicated servers and singleplayer (integrated server).
 */
public class SSoggySoulsMod implements ModInitializer {

    public static final String MOD_ID = "ssoggysouls";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static SSoggySoulsMod instance;

    public SSoggySoulsMod() {
        instance = this;
    }

    public static SSoggySoulsMod getInstance() {
        return instance;
    }

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

    public Path getDataFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    }

    public int getDefaultLives() {
        return 3; // TODO: read from config
    }

    public boolean isDebugMode() {
        return false; // TODO: read from config
    }

    public void debug(String message) {
        if (isDebugMode()) {
            LOGGER.info("[DEBUG] " + message);
        }
    }
    
    // Temporary helper until we implement a real config class
    public String getConfigString(String path, String def) {
        if (path.equals("database.table-name")) return "hardcore_players";
        if (path.equals("database.host")) return "localhost";
        if (path.equals("database.name")) return "minecraft";
        if (path.equals("database.username")) return "minecraft";
        if (path.equals("database.password")) return "changeme";
        return def;
    }
    
    public int getConfigInt(String path, int def) {
        if (path.equals("database.port")) return 3306;
        if (path.equals("database.pool-size")) return 5;
        return def;
    }
}
