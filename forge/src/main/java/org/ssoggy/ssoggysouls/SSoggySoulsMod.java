package org.ssoggy.ssoggysouls;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import net.minecraftforge.fml.loading.FMLPaths;
import org.ssoggy.ssoggysouls.util.ConfigManager;
import java.nio.file.Path;

import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.database.MySQLManager;
import org.ssoggy.ssoggysouls.database.SQLiteManager;
import org.ssoggy.ssoggysouls.command.CommandRegistration;
import org.ssoggy.ssoggysouls.listener.ServerLifecycleListener;
import org.ssoggy.ssoggysouls.util.MessageUtil;
import org.ssoggy.ssoggysouls.hrm.ExtraLifeManager;
import org.ssoggy.ssoggysouls.hrm.ReviveSkullManager;
import org.ssoggy.ssoggysouls.hrm.HeadEffectsTask;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostModeEvents;
import org.ssoggy.ssoggysouls.hrm.dlc.listener.GhostBlockEvents;

@Mod(SSoggySoulsMod.MODID)
public class SSoggySoulsMod {

    public static final String MODID = "ssoggysouls";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SSoggySoulsMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

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
        
        if (!databaseManager.initialize()) {
            LOGGER.error("Failed to initialize database. Disabling features.");
            return;
        }

        // Set Database instances
        CommandRegistration.setDatabase(databaseManager);
        ServerLifecycleListener.setDatabase(databaseManager);

        ExtraLifeManager.register(databaseManager);
        ReviveSkullManager.register(databaseManager);
        HeadEffectsTask.register();

        GhostModeEvents.register(databaseManager);
        GhostBlockEvents.register(databaseManager);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
    }

    public Path getDataFolder() {
        return FMLPaths.CONFIGDIR.get().resolve(MODID);
    }

    public int getDefaultLives() {
        return ConfigManager.getConfig().getDefaultLives();
    }

    public boolean isDebugMode() {
        return ConfigManager.getConfig().isDebug();
    }

    public void debug(String message) {
        if (isDebugMode()) {
            LOGGER.info("[DEBUG] {}", message);
        }
    }
}
