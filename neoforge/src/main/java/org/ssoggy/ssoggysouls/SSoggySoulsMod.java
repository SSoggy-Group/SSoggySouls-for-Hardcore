package org.ssoggy.ssoggysouls;

import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import net.neoforged.fml.loading.FMLPaths;
import org.ssoggy.ssoggysouls.util.ConfigManager;

import org.ssoggy.ssoggysouls.database.DatabaseInitializationException;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.database.MySQLManager;
import org.ssoggy.ssoggysouls.database.SQLiteManager;
import org.ssoggy.ssoggysouls.command.CommandRegistration;
import org.ssoggy.ssoggysouls.listener.ServerLifecycleListener;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.UpdateChecker;
import org.ssoggy.ssoggysouls.hrm.ExtraLifeManager;
import org.ssoggy.ssoggysouls.hrm.ReviveSkullManager;
import org.ssoggy.ssoggysouls.hrm.HeadEffectsTask;
import org.ssoggy.ssoggysouls.hrm.RevivalStructureListener;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostModeEvents;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostBlockEvents;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcServices;
import org.ssoggy.ssoggysouls.listener.LimboServerListener;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;
import org.ssoggy.ssoggysouls.util.SchedulerManager;

@Mod(SSoggySoulsMod.MODID)
public class SSoggySoulsMod implements PluginContext {

    public static final String MODID = "ssoggysouls";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final java.util.logging.Logger JUL_LOGGER = java.util.logging.Logger.getLogger(MODID);

    public SSoggySoulsMod(IEventBus modEventBus) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(CommandRegistration.class);
        NeoForge.EVENT_BUS.register(SchedulerManager.class);

        LOGGER.info("SSoggySouls NeoForge is loading...");

        // Load Configs
        ConfigManager.load();
        MessageUtil.loadMessages();

        // Initialize database
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

        // Set Database instances
        CommandRegistration.setDatabase(databaseManager);
        
        if (ConfigManager.getConfig().isLimboServer()) {
            NeoForge.EVENT_BUS.register(LimboServerListener.class);
            LimboServerListener.setDatabase(databaseManager);
        } else {
            NeoForge.EVENT_BUS.register(ServerLifecycleListener.class);
            ServerLifecycleListener.setDatabase(databaseManager);

            NeoForge.EVENT_BUS.register(ExtraLifeManager.class);
            ExtraLifeManager.register(databaseManager);

            NeoForge.EVENT_BUS.register(ReviveSkullManager.class);
            ReviveSkullManager.register(databaseManager);

            NeoForge.EVENT_BUS.register(HeadEffectsTask.class);
            HeadEffectsTask.register();

            NeoForge.EVENT_BUS.register(RevivalStructureListener.class);
            RevivalStructureListener.register(databaseManager);

            NeoForge.EVENT_BUS.register(GhostModeEvents.class);
            GhostModeEvents.register(databaseManager);

            NeoForge.EVENT_BUS.register(GhostBlockEvents.class);
            GhostBlockEvents.register(databaseManager);
        }

        if (ConfigManager.getConfig().isCheckForUpdates()) {
            new UpdateChecker().checkForUpdates();
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ServerTransferUtil::register);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
    }

    @Override
    public java.io.File getDataFolder() {
        java.io.File dir = FMLPaths.CONFIGDIR.get().resolve(MODID).toFile();
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
        return ConfigManager.getConfig().getConfigString(path, defaultValue);
    }

    @Override
    public int getConfigInt(String path, int defaultValue) {
        return ConfigManager.getConfig().getConfigInt(path, defaultValue);
    }
}
