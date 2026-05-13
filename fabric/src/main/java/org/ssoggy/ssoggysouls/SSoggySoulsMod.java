package org.ssoggy.ssoggysouls;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssoggy.ssoggysouls.task.LimboCheckTask;
import org.ssoggy.ssoggysouls.task.MainReviveCheckTask;

import java.io.File;

import org.ssoggy.ssoggysouls.database.DatabaseInitializationException;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.database.MySQLManager;
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
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcServices;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;
import org.ssoggy.ssoggysouls.util.UpdateChecker;

/**
 * SSoggySouls Fabric Mod — main entrypoint.
 * <p>
 * This is the Fabric equivalent of the Bukkit {@code SSoggySouls extends JavaPlugin}.
 * It initializes on both dedicated servers and singleplayer (integrated server).
 */
public class SSoggySoulsMod implements ModInitializer, PluginContext {

    public static final String MOD_ID = "ssoggysouls";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final java.util.logging.Logger JUL_LOGGER = java.util.logging.Logger.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("SSoggySouls Fabric is loading...");

        // Phase 2: Load Configs
        ConfigManager.load();
        MessageUtil.loadMessages();

        // Phase 2: Initialize database based on config
        String dbType = ConfigManager.getConfig().getDatabaseType();
        DatabaseManager databaseManager;
        if ("mysql".equalsIgnoreCase(dbType)) {
            databaseManager = new MySQLManager(this);
        } else {
            databaseManager = new SQLiteManager(this);
        }
        try {
            databaseManager.initialize();
        } catch (DatabaseInitializationException e) {
            LOGGER.error("Failed to initialize database. Disabling features. Error: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize SSoggySouls database", e);
        }
        DlcServices.init(this);

        // Phase 3: Register commands (Brigadier)
        CommandRegistration.register(this, databaseManager);

        // Phase 6: Register Proxy Payloads
        ServerTransferUtil.registerPayloads();

        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            LOGGER.info("Singleplayer environment detected! Disabling multiplayer proxy routing.");
            ConfigManager.getConfig().setLimboServer(false);
            ConfigManager.getConfig().setSendToLimboOnDeath(false);
        }

        if (ConfigManager.getConfig().isLimboServer()) {
            LOGGER.info("Starting in LIMBO server mode...");
            LimboServerListener.register(databaseManager);
            LimboCheckTask limboCheckTask = new LimboCheckTask(this);
            ServerTickEvents.END_SERVER_TICK.register(limboCheckTask::tick);
        } else {
            LOGGER.info("Starting in MAIN server mode...");
            MainServerListener.register(databaseManager);
            MainReviveCheckTask mainReviveCheckTask = new MainReviveCheckTask(this);
            ServerTickEvents.END_SERVER_TICK.register(mainReviveCheckTask::tick);

            // Phase 4: Init Built-in Hardcore Revive Features
            HeadDropListener.register();
            RevivalStructureListener.register(databaseManager);
            ExtraLifeManager.register(databaseManager);
            ReviveSkullManager.register(databaseManager);
            HeadEffectsTask.register();

            // Phase 5: Initialize RevivalPlus DLC core
            GhostModeEvents.register(databaseManager);
            GhostBlockEvents.register(databaseManager);
        }

        if (ConfigManager.getConfig().isCheckForUpdates()) {
            // Intentional fire-and-forget: run a one-time startup update check; no retained instance needed.
            new UpdateChecker().checkForUpdates();
        }
        LOGGER.info("SSoggySouls Fabric loaded successfully!");
    }

    @Override
    public File getDataFolder() {
        File dir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    @Override
    public java.util.logging.Logger getLogger() {
        return JUL_LOGGER;
    }

    @Override
    public int getDefaultLives() {
        return ConfigManager.getConfig().getDefaultLives();
    }

    @Override
    public boolean isDebugMode() {
        return ConfigManager.getConfig().isDebug();
    }

    @Override
    public void debug(String message) {
        if (isDebugMode()) {
            LOGGER.info("[DEBUG] {}", message);
        }
    }

    @Override
    public String getConfigString(String path, String defaultValue) {
        // Delegate to ConfigManager for Fabric config values
        return ConfigManager.getConfig().getConfigString(path, defaultValue);
    }

    @Override
    public int getConfigInt(String path, int defaultValue) {
        return ConfigManager.getConfig().getConfigInt(path, defaultValue);
    }
}
