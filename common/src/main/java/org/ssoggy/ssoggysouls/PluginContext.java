package org.ssoggy.ssoggysouls;

import java.io.File;
import java.util.logging.Logger;

/**
 * Platform-agnostic contract for the plugin context.
 * <p>
 * Each platform (Paper/Bukkit, Fabric, Forge) provides its own implementation,
 * allowing the common database managers and utilities to remain free of any
 * platform-specific imports.
 */
public interface PluginContext {

    /** Returns the plugin logger (java.util.logging). */
    Logger getLogger();

    /** Returns the plugin data folder for file-based storage (SQLite, configs). */
    File getDataFolder();

    /** Returns the default number of lives for new players. */
    int getDefaultLives();

    /** Returns whether debug mode is active. */
    boolean isDebugMode();

    /** Logs a debug message if debug mode is active. */
    void debug(String message);

    // ── Database configuration accessors ──

    String getConfigString(String path, String defaultValue);

    int getConfigInt(String path, int defaultValue);
}
