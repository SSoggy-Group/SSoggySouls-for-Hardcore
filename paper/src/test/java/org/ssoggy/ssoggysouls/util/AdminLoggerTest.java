package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ssoggy.ssoggysouls.SSoggySouls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLoggerTest {

    @TempDir
    File tempDir;

    private SSoggySouls plugin;
    private Logger logger;

    @BeforeEach
    void setUp() {
        plugin = mock(SSoggySouls.class);
        logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);
    }

    @Test
    void testLogSuccess() throws IOException {
        File dataFolder = new File(tempDir, "data");
        when(plugin.getDataFolder()).thenReturn(dataFolder);

        AdminLogger.log(plugin, "AdminUser", "Ban PlayerX");

        File logFile = new File(dataFolder, AdminLogger.LOG_FILE_NAME);
        assertTrue(logFile.exists(), "Log file should be created");

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("ADMIN ACTION - AdminUser: Ban PlayerX"));

        verify(logger).log(eq(Level.INFO), eq("[Admin Log] {0}: {1}"), eq(new Object[]{"AdminUser", "Ban PlayerX"}));
    }

    @Test
    void testLogSanitizesInput() throws IOException {
        File dataFolder = new File(tempDir, "data_sanitize");
        when(plugin.getDataFolder()).thenReturn(dataFolder);

        AdminLogger.log(plugin, "Admin\nUser\r", null);

        File logFile = new File(dataFolder, AdminLogger.LOG_FILE_NAME);
        assertTrue(logFile.exists());

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("ADMIN ACTION - Admin_User_: null"));

        verify(logger).log(eq(Level.INFO), eq("[Admin Log] {0}: {1}"), eq(new Object[]{"Admin_User_", "null"}));
    }

    @Test
    void testLogIOExceptionHandling() throws IOException {
        File invalidDataFolder = new File(tempDir, "invalid_file");
        assertTrue(invalidDataFolder.createNewFile(), "Should create dummy file");

        when(plugin.getDataFolder()).thenReturn(invalidDataFolder);

        AdminLogger.log(plugin, "AdminUser", "Action");

        verify(logger).log(eq(Level.SEVERE), eq("Failed to write to admin log file"), any(IOException.class));
    }
}
