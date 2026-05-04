package org.ssoggy.ssoggysouls.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.ModList;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {
    private static final String GITHUB_API = "https://api.github.com/repos/SSoggy-Group/SSoggySouls-for-Hardcore/releases/latest";
    private static final String DOWNLOAD_PAGE = "https://modrinth.com/project/Pb03qu6T";
    private static final String BORDER_EMPTY = "║                                                           ║";
    private static final String BORDER_TOP = "╔═══════════════════════════════════════════════════════════╗";
    private static final String BORDER_BOTTOM = "╚═══════════════════════════════════════════════════════════╝";

    private final String currentVersion;

    public UpdateChecker() {
        this.currentVersion = ModList.get().getModContainerById(SSoggySoulsMod.MODID)
                .map(mod -> mod.getModInfo().getVersion().toString())
                .orElse("0.0.0");
    }

    public void checkForUpdates() {
        CompletableFuture.runAsync(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) URI.create(GITHUB_API).toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("User-Agent", "SSoggySouls-UpdateChecker");

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    SSoggySoulsMod.LOGGER.warn("Failed to check for updates. HTTP response code: {}", responseCode);
                    return;
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                String latestVersion = json.get("tag_name").getAsString();

                if (latestVersion.startsWith("v")) {
                    latestVersion = latestVersion.substring(1);
                }

                if (isNewerVersion(latestVersion, currentVersion)) {
                    showUpdateNotification(latestVersion);
                } else {
                    SSoggySoulsMod.LOGGER.info("You are running the latest version!");
                }

            } catch (IOException e) {
                SSoggySoulsMod.LOGGER.warn("Failed to check for updates: {}", e.getMessage());
            }
        });
    }

    private boolean isNewerVersion(String latest, String current) {
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        int maxLength = Math.max(latestParts.length, currentParts.length);

        for (int i = 0; i < maxLength; i++) {
            int latestPart = i < latestParts.length ? parseVersionPart(latestParts[i]) : 0;
            int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;

            if (latestPart > currentPart) {
                return true;
            } else if (latestPart < currentPart) {
                return false;
            }
        }

        return false;
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showUpdateNotification(String latestVersion) {
        if (SSoggySoulsMod.LOGGER.isInfoEnabled()) {
            SSoggySoulsMod.LOGGER.info("");
            SSoggySoulsMod.LOGGER.info(BORDER_TOP);
            SSoggySoulsMod.LOGGER.info(BORDER_EMPTY);
            SSoggySoulsMod.LOGGER.info("║           ⚡ UPDATE AVAILABLE ⚡                          ║");
            SSoggySoulsMod.LOGGER.info(BORDER_EMPTY);
            SSoggySoulsMod.LOGGER.info("║   Current version: {}║", String.format("%-35s", currentVersion));
            SSoggySoulsMod.LOGGER.info("║   Latest version:  {}║", String.format("%-35s", latestVersion));
            SSoggySoulsMod.LOGGER.info(BORDER_EMPTY);
            SSoggySoulsMod.LOGGER.info("║   Download: {}║", String.format("%-43s", DOWNLOAD_PAGE));
            SSoggySoulsMod.LOGGER.info(BORDER_EMPTY);
            SSoggySoulsMod.LOGGER.info(BORDER_BOTTOM);
            SSoggySoulsMod.LOGGER.info("");
        }
    }
}
