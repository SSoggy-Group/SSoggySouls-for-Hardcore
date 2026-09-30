package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLoggerTest {

    @Mock
    private SSoggySouls plugin;

    @Mock
    private Logger logger;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(plugin.getDataFolder()).thenReturn(tempDir);
        when(plugin.getLogger()).thenReturn(logger);
    }

    @Test
    void testLogCreatesDirectoryAndWritesFile() throws IOException {
        File subDataFolder = new File(tempDir, "subfolder");
        when(plugin.getDataFolder()).thenReturn(subDataFolder);

        AdminLogger.log(plugin, "AdminUser", "Banned PlayerX");

        assertTrue(subDataFolder.exists());
        File logFile = new File(subDataFolder, AdminLogger.LOG_FILE_NAME);
        assertTrue(logFile.exists());

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("ADMIN ACTION - AdminUser: Banned PlayerX"));

        verify(logger).log(eq(Level.INFO), eq("[Admin Log] {0}: {1}"), eq(new Object[]{"AdminUser", "Banned PlayerX"}));
    }

    @Test
    void testLogSanitizesInputWithNewlines() throws IOException {
        AdminLogger.log(plugin, "Admin\nUser\r", "Action\nWith\rNewlines");

        File logFile = new File(tempDir, AdminLogger.LOG_FILE_NAME);
        assertTrue(logFile.exists());

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("ADMIN ACTION - Admin_User_: Action_With_Newlines"));

        verify(logger).log(eq(Level.INFO), eq("[Admin Log] {0}: {1}"), eq(new Object[]{"Admin_User_", "Action_With_Newlines"}));
    }

    @Test
    void testLogHandlesNullInput() throws IOException {
        AdminLogger.log(plugin, null, null);

        File logFile = new File(tempDir, AdminLogger.LOG_FILE_NAME);
        assertTrue(logFile.exists());

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("ADMIN ACTION - null: null"));

        verify(logger).log(eq(Level.INFO), eq("[Admin Log] {0}: {1}"), eq(new Object[]{"null", "null"}));
    }

    @Test
    void testLogIOExceptionHandledGracefully() {
        File logFileAsDir = new File(tempDir, AdminLogger.LOG_FILE_NAME);
        assertTrue(logFileAsDir.mkdirs());

        AdminLogger.log(plugin, "Admin", "Action");

        verify(logger).log(eq(Level.SEVERE), eq("Failed to write to admin log file"), any(IOException.class));
    }
}
