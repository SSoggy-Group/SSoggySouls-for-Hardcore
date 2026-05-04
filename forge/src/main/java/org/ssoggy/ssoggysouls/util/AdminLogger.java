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

    private static final String LOG_FILE_NAME = "admin_abuse.log";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Object WRITE_LOCK = new Object();

    private AdminLogger() {
    }

    public static void log(String sender, String action) {
        File dataFolder = FMLPaths.CONFIGDIR.get().resolve(SSoggySoulsMod.MODID).toFile();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File logFile = new File(dataFolder, LOG_FILE_NAME);
        synchronized (WRITE_LOCK) {
            try (FileWriter fw = new FileWriter(logFile, java.nio.charset.StandardCharsets.UTF_8, true);
                    PrintWriter pw = new PrintWriter(fw)) {

                String timestamp = DATE_FORMAT.format(LocalDateTime.now());
                String logEntry = String.format("[%s] ADMIN ACTION - %s: %s", timestamp, sender, action);
                pw.println(logEntry);
                SSoggySoulsMod.LOGGER.info("[Admin Log] {}: {}", sender, action);

            } catch (IOException e) {
                SSoggySoulsMod.LOGGER.error("Failed to write to admin log file", e);
            }
        }
    }
}
