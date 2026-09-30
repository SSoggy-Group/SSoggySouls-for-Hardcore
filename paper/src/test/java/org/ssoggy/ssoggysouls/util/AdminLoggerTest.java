package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ssoggy.ssoggysouls.SSoggySouls;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
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
    void testLogCreatesDirectoryAndWritesLog() throws IOException {
        File pluginDir = new File(tempDir, "pluginData");
        when(plugin.getDataFolder()).thenReturn(pluginDir);

        AdminLogger.log(plugin, "AdminUser", "Executed command /pban");

        assertTrue(pluginDir.exists(), "Plugin data directory should be created if missing");

        File logFile = new File(pluginDir, AdminLogger.LOG_FILE_NAME);
        assertTrue(logFile.exists(), "admin.log file should exist");

        List<String> lines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("ADMIN ACTION - AdminUser: Executed command /pban"));

        verify(logger).log(
                eq(Level.INFO),
                eq("[Admin Log] {0}: {1}"),
                argThat((Object[] params) -> params != null && params.length == 2 && "AdminUser".equals(params[0]) && "Executed command /pban".equals(params[1]))
        );
    }

    @Test
    void testLogSanitizesInputAndAppends() throws IOException {
        File pluginDir = new File(tempDir, "pluginData");
        when(plugin.getDataFolder()).thenReturn(pluginDir);

        AdminLogger.log(plugin, null, "line1\nline2\rline3");
        AdminLogger.log(plugin, "User\nName", null);

        File logFile = new File(pluginDir, AdminLogger.LOG_FILE_NAME);
        List<String> lines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);

        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("ADMIN ACTION - null: line1_line2_line3"));
        assertTrue(lines.get(1).contains("ADMIN ACTION - User_Name: null"));
    }

    @Test
    void testLogIOExceptionHandledGracefully() throws IOException {
        File invalidDataDir = new File(tempDir, "fileAsFolder");
        assertTrue(invalidDataDir.createNewFile(), "Created file where directory was expected");
        when(plugin.getDataFolder()).thenReturn(invalidDataDir);

        AdminLogger.log(plugin, "Admin", "Action");

        verify(logger).log(
                eq(Level.SEVERE),
                eq("Failed to write to admin log file"),
                org.mockito.ArgumentMatchers.any(IOException.class)
        );
    }
}
