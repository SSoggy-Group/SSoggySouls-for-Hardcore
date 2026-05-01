package org.ssoggy.ssoggysouls.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig config;
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "ssoggysouls.json");

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config = new ModConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ModConfig getConfig() {
        if (config == null) load();
        return config;
    }

    public static class ModConfig {
        // --- Core Lives System ---
        public int defaultLives = 3;
        public int onReviveLives = 1;
        public int maxLives = 5;
        public String gracePeriod = "0"; // e.g., "24h"
        public int reviveCooldownSeconds = 30;

        // --- Database & Proxy (Multiplayer) ---
        public boolean isLimboServer = false;
        public String mainServerName = "main";
        public String limboServerName = "limbo";
        public boolean sendToLimboOnDeath = false; // Default false for local/singleplayer

        // --- HRM (Revival) Features ---
        public boolean hrmEnabled = true;
        public boolean dropHeads = true;
        public boolean headPlaceAsBlock = true;
        public boolean headWearingEffects = true;
        public boolean ritualLightningStrike = true;
        public boolean ritualTotemEffect = true;

        // --- DLC / Ghost Mode ---
        public boolean loseInventory = false;
        public boolean ghostModeParticles = false;
        public int spectatorHeadrestrictRadius = 16;
        public boolean restrictMenuAccess = true;

        // --- Debug ---
        public boolean debug = false;
    }
}
