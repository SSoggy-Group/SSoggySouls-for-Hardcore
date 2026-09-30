package org.ssoggy.ssoggysouls;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.ssoggy.ssoggysouls.command.CommandRegistration;
import org.ssoggy.ssoggysouls.database.DatabaseInitializationException;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.database.MySQLManager;
import org.ssoggy.ssoggysouls.database.SQLiteManager;
import org.ssoggy.ssoggysouls.hrm.ExtraLifeManager;
import org.ssoggy.ssoggysouls.hrm.HeadEffectsTask;
import org.ssoggy.ssoggysouls.hrm.RevivalStructureListener;
import org.ssoggy.ssoggysouls.hrm.ReviveSkullManager;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostBlockEvents;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostModeEvents;
import org.ssoggy.ssoggysouls.hrm.dlc.shared.DlcServices;
import org.ssoggy.ssoggysouls.listener.LimboServerListener;
import org.ssoggy.ssoggysouls.listener.ServerLifecycleListener;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.util.SchedulerManager;
import org.ssoggy.ssoggysouls.util.ServerTransferUtil;
import org.ssoggy.ssoggysouls.util.UpdateChecker;

@Mod(SSoggySoulsMod.MODID)
public class SSoggySoulsMod implements PluginContext {

    public static final String MODID = "ssoggysouls";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final java.util.logging.Logger JUL_LOGGER = java.util.logging.Logger.getLogger(MODID);

    public SSoggySoulsMod() {
        // EventBus 7 rejects register() on classes with a single @SubscribeEvent method,
        // so single-listener classes are wired directly to their event's BUS.
        ServerStartingEvent.BUS.addListener(this::onServerStarting);
        RegisterCommandsEvent.BUS.addListener(CommandRegistration::onRegisterCommands);
        TickEvent.ServerTickEvent.Post.BUS.addListener(SchedulerManager::onServerTick);

        LOGGER.info("SSoggySouls Forge is loading...");

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
            LOGGER.error("Failed to initialize database. Disabling features.", e);
            throw new IllegalStateException("Failed to initialize SSoggySouls database", e);
        }
        DlcServices.init(this);

        // Set Database instances
        CommandRegistration.setDatabase(databaseManager);

        if (ConfigManager.getConfig().isLimboServer()) {
            MinecraftForge.EVENT_BUS.register(LimboServerListener.class);
            LimboServerListener.setDatabase(databaseManager);
        } else {
            MinecraftForge.EVENT_BUS.register(ServerLifecycleListener.class);
            ServerLifecycleListener.setDatabase(databaseManager);

            PlayerInteractEvent.RightClickItem.BUS.addListener(ExtraLifeManager::onItemRightClick);
            ExtraLifeManager.register(databaseManager);

            PlayerInteractEvent.RightClickItem.BUS.addListener(ReviveSkullManager::onItemRightClick);
            ReviveSkullManager.register(databaseManager);

            TickEvent.ServerTickEvent.Post.BUS.addListener(HeadEffectsTask::onServerTick);
            HeadEffectsTask.register();

            PlayerInteractEvent.RightClickBlock.BUS.addListener(RevivalStructureListener::onBlockPlace);
            RevivalStructureListener.register(databaseManager);

            MinecraftForge.EVENT_BUS.register(GhostModeEvents.class);
            GhostModeEvents.register(databaseManager);

            MinecraftForge.EVENT_BUS.register(GhostBlockEvents.class);
            GhostBlockEvents.register(databaseManager);
        }

        // Initialize network channel
        ServerTransferUtil.register();

        if (ConfigManager.getConfig().isCheckForUpdates()) {
            new UpdateChecker().checkForUpdates();
        }
    }

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
