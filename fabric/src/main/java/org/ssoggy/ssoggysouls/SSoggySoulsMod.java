package org.ssoggy.ssoggysouls;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.database.SQLiteManager;
import org.ssoggy.ssoggysouls.listener.LimboServerListener;
import org.ssoggy.ssoggysouls.listener.MainServerListener;
import org.ssoggy.ssoggysouls.command.CommandRegistration;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.hrm.HeadDropListener;
import org.ssoggy.ssoggysouls.hrm.RevivalStructureListener;
import org.ssoggy.ssoggysouls.hrm.ExtraLifeManager;
import org.ssoggy.ssoggysouls.hrm.ReviveSkullManager;
import org.ssoggy.ssoggysouls.hrm.HeadEffectsTask;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostModeEvents;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostBlockEvents;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;
import org.ssoggy.ssoggysouls.util.UpdateChecker;

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
    private DatabaseManager databaseManager;

    public SSoggySoulsMod() {
        // Entrypoint
    }

    public static SSoggySoulsMod getInstance() {
        return instance == null ? instance = new SSoggySoulsMod() : instance;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("SSoggySouls Fabric is loading...");

        // Phase 2: Load config (MessageUtil temp)
        MessageUtil.loadMessages();

        // Phase 2: Initialize database (SQLite default for now)
        databaseManager = new SQLiteManager(this);
        if (!databaseManager.initialize()) {
            LOGGER.error("Failed to initialize database. Disabling features.");
            return;
        }

        // Phase 3: Register commands (Brigadier)
        CommandRegistration.register(this, databaseManager);

        // Phase 6: Load Configs & Register Proxy Payloads
        ConfigManager.load();

        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            LOGGER.info("Singleplayer environment detected! Disabling multiplayer proxy routing.");
            ConfigManager.getConfig().isLimboServer = false;
            ConfigManager.getConfig().sendToLimboOnDeath = false;
        }

        ServerTransferUtil.registerPayloads();

        if (ConfigManager.getConfig().isLimboServer) {
            LOGGER.info("Starting in LIMBO server mode...");
            new LimboServerListener(this, databaseManager);
        } else {
            LOGGER.info("Starting in MAIN server mode...");
            new MainServerListener(this, databaseManager);
            
            // Phase 4: Init Built-in Hardcore Revive Features
            HeadDropListener.register(databaseManager);
            RevivalStructureListener.register(databaseManager);
            ExtraLifeManager.register(databaseManager);
            ReviveSkullManager.register(databaseManager);
            HeadEffectsTask.register();
            
            // Phase 5: Initialize RevivalPlus DLC core
            GhostModeEvents.register(databaseManager);
            GhostBlockEvents.register(databaseManager);
        }

        new UpdateChecker().checkForUpdates();
        LOGGER.info("SSoggySouls Fabric loaded successfully!");
    }

    public Path getDataFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    }

    public int getDefaultLives() {
        return ConfigManager.getConfig().defaultLives;
    }

    public boolean isDebugMode() {
        return ConfigManager.getConfig().debug;
    }

    public void debug(String message) {
        if (isDebugMode()) {
            LOGGER.info("[DEBUG] {}", message);
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
