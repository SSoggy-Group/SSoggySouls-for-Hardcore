package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.ssoggy.ssoggysouls.PluginContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @ParameterizedTest
    @CsvSource(value = {
            "Admin, Set lives to 3, ADMIN ACTION - Admin: Set lives to 3",
            "Admin\\nEvil, Set\\rlives\\nto 3, ADMIN ACTION - Admin_Evil: Set_lives_to 3",
            "NULL_PLACEHOLDER, NULL_PLACEHOLDER, ADMIN ACTION - null: null"
    })
    void testLogInputs(String sender, String action, String expectedLogEntry) throws IOException {
        String actualSender = "NULL_PLACEHOLDER".equals(sender) ? null : sender.replace("\\n", "\n").replace("\\r", "\r");
        String actualAction = "NULL_PLACEHOLDER".equals(action) ? null : action.replace("\\n", "\n").replace("\\r", "\r");

        AdminLogger.log(plugin, actualSender, actualAction);

        assertTrue(logFile.exists());
        List<String> lines = Files.readAllLines(logFile.toPath());
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(lines.size() - 1).contains(expectedLogEntry));
    }
}
