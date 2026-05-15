package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.PluginContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AdminLoggerTest {

    private PluginContext plugin;
    private Logger logger;
    private File tempFolder;
    private File logFile;

    @BeforeEach
    void setUp() throws IOException {
        plugin = mock(PluginContext.class);
        logger = mock(Logger.class);

        tempFolder = Files.createTempDirectory("ssoggysouls-test").toFile();
        logFile = new File(tempFolder, AdminLogger.LOG_FILE_NAME);

        when(plugin.getDataFolder()).thenReturn(tempFolder);
        when(plugin.getLogger()).thenReturn(logger);
    }

    @AfterEach
    void tearDown() {
        if (logFile.exists()) {
            logFile.delete();
        }
        if (tempFolder.exists()) {
            tempFolder.delete();
        }
    }

    @Test
    void testLogNormalInput() throws IOException {
        AdminLogger.log(plugin, "Admin", "Set lives to 3");

        assertTrue(logFile.exists());
        List<String> lines = Files.readAllLines(logFile.toPath());
        assertTrue(lines.size() > 0);
        assertTrue(lines.get(lines.size() - 1).contains("ADMIN ACTION - Admin: Set lives to 3"));
    }

    @Test
    void testLogSanitizesInput() throws IOException {
        AdminLogger.log(plugin, "Admin\nEvil", "Set\rlives\nto 3");

        assertTrue(logFile.exists());
        List<String> lines = Files.readAllLines(logFile.toPath());
        assertTrue(lines.size() > 0);
        assertTrue(lines.get(lines.size() - 1).contains("ADMIN ACTION - Admin_Evil: Set_lives_to 3"));
    }

    @Test
    void testLogNullInput() throws IOException {
        AdminLogger.log(plugin, null, null);

        assertTrue(logFile.exists());
        List<String> lines = Files.readAllLines(logFile.toPath());
        assertTrue(lines.size() > 0);
        assertTrue(lines.get(lines.size() - 1).contains("ADMIN ACTION - null: null"));
    }
}
