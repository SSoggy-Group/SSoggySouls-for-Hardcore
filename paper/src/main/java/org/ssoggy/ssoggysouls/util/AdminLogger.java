package org.ssoggy.ssoggysouls.util;

import org.ssoggy.ssoggysouls.SSoggySouls;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class AdminLogger {

    public static final String LOG_FILE_NAME = "admin.log";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Object WRITE_LOCK = new Object();

    private AdminLogger() {
    }

    public static void log(SSoggySouls plugin, String sender, String action) {
        String safeSender = sender != null ? sender.replace('\n', '_').replace('\r', '_').replace(':', '_') : "Unknown";
        String safeAction = action != null ? action.replace('\n', '_').replace('\r', '_') : "Unknown";
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File logFile = new File(dataFolder, LOG_FILE_NAME);
        synchronized (WRITE_LOCK) {
            try (FileWriter fw = new FileWriter(logFile, java.nio.charset.StandardCharsets.UTF_8, true);
                    PrintWriter pw = new PrintWriter(fw)) {

                String timestamp = DATE_FORMAT.format(LocalDateTime.now());
                String logEntry = String.format("[%s] ADMIN ACTION - %s: %s", timestamp, safeSender, safeAction);
                pw.println(logEntry);
                plugin.getLogger().log(Level.INFO, "[Admin Log] {0}: {1}", new Object[] { safeSender, safeAction });

            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to write to admin log file", e);
            }
        }
    }
}
