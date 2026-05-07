package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.ssoggy.ssoggysouls.PluginContext;

import java.io.File;
import java.util.logging.Logger;

public final class DlcServices {
    private DlcServices() {
    }

    private static boolean initialized;
    private static Logger logger;
    private static DlcStorage statsStorage;
    private static DlcStorage socialStorage;
    private static DlcStorage usernameStorage;
    private static DlcStorage deathStorage;

    public static synchronized void init(PluginContext context) {
        logger = context.getLogger();
        File dataFolder = new File(context.getDataFolder(), "revivalplus");
        statsStorage = new DlcStorage(dataFolder, "stats.properties", logger);
        socialStorage = new DlcStorage(dataFolder, "social.properties", logger);
        usernameStorage = new DlcStorage(dataFolder, "usernamecache.properties", logger);
        deathStorage = new DlcStorage(dataFolder, "deaths.properties", logger);
        initialized = true;
        DlcDeaths.reloadFromStorage();
    }

    static DlcStorage statsStorage() {
        ensureInitialized();
        return statsStorage;
    }

    static DlcStorage socialStorage() {
        ensureInitialized();
        return socialStorage;
    }

    static DlcStorage usernameStorage() {
        ensureInitialized();
        return usernameStorage;
    }

    static DlcStorage deathStorage() {
        ensureInitialized();
        return deathStorage;
    }

    static Logger logger() {
        ensureInitialized();
        return logger;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("RevivalPlus DLC services have not been initialized");
        }
    }
}
