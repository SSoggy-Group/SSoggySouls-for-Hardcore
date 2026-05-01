package org.ssoggy.ssoggysouls.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {

    private ConfigManager() {
        // Utility class
    }

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

    public static long parseGracePeriod(String input) {
        if (input == null || input.equals("0") || input.isEmpty()) return 0;
        try {
            long totalMs = 0;
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)([hms])").matcher(input.toLowerCase());
            while (matcher.find()) {
                long value = Long.parseLong(matcher.group(1));
                char unit = matcher.group(2).charAt(0);
                switch (unit) {
                    case 'h' -> totalMs += value * 3600000;
                    case 'm' -> totalMs += value * 60000;
                    case 's' -> totalMs += value * 1000;
                    default -> { /* ignore */ }
                }
            }
            return totalMs;
        } catch (Exception e) {
            return 0;
        }
    }

    public static class ModConfig {
        // --- Core Lives System ---
        private int defaultLives = 3;
        private int onReviveLives = 1;
        private int maxLives = 5;
        private String gracePeriod = "0"; // e.g., "24h"
        private int reviveCooldownSeconds = 30;

        // --- Database & Proxy (Multiplayer) ---
        private boolean isLimboServer = false;
        private String mainServerName = "main";
        private String limboServerName = "limbo";
        private boolean sendToLimboOnDeath = false;
        private String limboSpawnWorld = "minecraft:overworld";
        private double limboSpawnX = 0;
        private double limboSpawnY = 100;
        private double limboSpawnZ = 0;
        private float limboSpawnYaw = 0;
        private float limboSpawnPitch = 0;

        // --- HRM (Revival) Features ---
        private boolean hrmEnabled = true;
        private boolean dropHeads = true;
        private boolean headPlaceAsBlock = true;
        private boolean headWearingEffects = true;
        private boolean ritualLightningStrike = true;
        private boolean ritualTotemEffect = true;
        private boolean leaveStructureBase = true;

        // --- DLC / Ghost Mode ---
        private boolean loseInventory = false;
        private boolean ghostModeParticles = false;
        private int spectatorHeadrestrictRadius = 16;
        private boolean restrictMenuAccess = true;

        // --- Messages ---
        private String messagePrefix = "§8[§4☠§8] §r";
        private java.util.Map<String, String> messages = new java.util.HashMap<>();

        // --- Debug ---
        private boolean debug = false;

        public ModConfig() {
            // Default messages
            messages.put("death-life-lost", "§cYou lost a life! §7Remaining: §e%lives%");
            messages.put("death-last-life", "§c§l⚠ FINAL WARNING! §cYou are on your last life. Be careful!");
            messages.put("revive-success", "§a§l✦ REVIVED! §aReturning to the world of the living...");
            messages.put("grace-remaining", "§eYou are protected for §a%time_remaining%");
        }

        // Getters
        public int getDefaultLives() { return defaultLives; }
        public int getOnReviveLives() { return onReviveLives; }
        public int getMaxLives() { return maxLives; }
        public String getGracePeriod() { return gracePeriod; }
        public int getReviveCooldownSeconds() { return reviveCooldownSeconds; }
        public boolean isLimboServer() { return isLimboServer; }
        public String getMainServerName() { return mainServerName; }
        public String getLimboServerName() { return limboServerName; }
        public boolean isSendToLimboOnDeath() { return sendToLimboOnDeath; }
        public String getLimboSpawnWorld() { return limboSpawnWorld; }
        public double getLimboSpawnX() { return limboSpawnX; }
        public double getLimboSpawnY() { return limboSpawnY; }
        public double getLimboSpawnZ() { return limboSpawnZ; }
        public float getLimboSpawnYaw() { return limboSpawnYaw; }
        public float getLimboSpawnPitch() { return limboSpawnPitch; }
        public boolean isHrmEnabled() { return hrmEnabled; }
        public boolean isDropHeads() { return dropHeads; }
        public boolean isHeadPlaceAsBlock() { return headPlaceAsBlock; }
        public boolean isHeadWearingEffects() { return headWearingEffects; }
        public boolean isRitualLightningStrike() { return ritualLightningStrike; }
        public boolean isRitualTotemEffect() { return ritualTotemEffect; }
        public boolean isLeaveStructureBase() { return leaveStructureBase; }
        public boolean isLoseInventory() { return loseInventory; }
        public boolean isGhostModeParticles() { return ghostModeParticles; }
        public int getSpectatorHeadrestrictRadius() { return spectatorHeadrestrictRadius; }
        public boolean isRestrictMenuAccess() { return restrictMenuAccess; }
        public String getMessagePrefix() { return messagePrefix; }
        public java.util.Map<String, String> getMessages() { return messages; }
        public boolean isDebug() { return debug; }

        // Setters for ModMenu / Initialization
        public void setLimboServer(boolean limboServer) { isLimboServer = limboServer; }
        public void setSendToLimboOnDeath(boolean send) { sendToLimboOnDeath = send; }
        public void setDefaultLives(int lives) { defaultLives = lives; }
        public void setOnReviveLives(int lives) { onReviveLives = lives; }
        public void setMaxLives(int max) { maxLives = max; }
        public void setGracePeriod(String period) { gracePeriod = period; }
        public void setReviveCooldownSeconds(int seconds) { reviveCooldownSeconds = seconds; }
        public void setMainServerName(String name) { mainServerName = name; }
        public void setLimboServerName(String name) { limboServerName = name; }
        public void setLimboSpawnWorld(String world) { limboSpawnWorld = world; }
        public void setLimboSpawnX(double x) { limboSpawnX = x; }
        public void setLimboSpawnY(double y) { limboSpawnY = y; }
        public void setLimboSpawnZ(double z) { limboSpawnZ = z; }
        public void setLimboSpawnYaw(float yaw) { limboSpawnYaw = yaw; }
        public void setLimboSpawnPitch(float pitch) { limboSpawnPitch = pitch; }
        public void setHrmEnabled(boolean enabled) { hrmEnabled = enabled; }
        public void setDropHeads(boolean drop) { dropHeads = drop; }
        public void setHeadPlaceAsBlock(boolean place) { headPlaceAsBlock = place; }
        public void setHeadWearingEffects(boolean effects) { headWearingEffects = effects; }
        public void setRitualLightningStrike(boolean strike) { ritualLightningStrike = strike; }
        public void setRitualTotemEffect(boolean effect) { ritualTotemEffect = effect; }
        public void setLeaveStructureBase(boolean leave) { leaveStructureBase = leave; }
        public void setLoseInventory(boolean lose) { loseInventory = lose; }
        public void setGhostModeParticles(boolean particles) { ghostModeParticles = particles; }
        public void setSpectatorHeadrestrictRadius(int radius) { spectatorHeadrestrictRadius = radius; }
        public void setRestrictMenuAccess(boolean restrict) { restrictMenuAccess = restrict; }
        public void setDebug(boolean d) { debug = d; }
    }
}
