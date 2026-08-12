package org.ssoggy.ssoggysouls.util;

import net.minecraftforge.fml.loading.FMLPaths;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminLogger {

    public static final String LOG_FILE_NAME = "admin.log";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Object WRITE_LOCK = new Object();

    private AdminLogger() {
    }

    private static String sanitize(String input) {
        if (input == null) return "null";
        return input.replace('\n', '_').replace('\r', '_').replace('&', '＆');
    }

    public static void log(String sender, String action) {
        String safeSender = sanitize(sender);
        String safeAction = sanitize(action);

        File dataFolder = FMLPaths.CONFIGDIR.get().resolve(SSoggySoulsMod.MODID).toFile();
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
                SSoggySoulsMod.LOGGER.info("[Admin Log] {}: {}", safeSender, safeAction);

            } catch (IOException e) {
                SSoggySoulsMod.LOGGER.error("Failed to write to admin log file", e);
            }
        }
    }
}
