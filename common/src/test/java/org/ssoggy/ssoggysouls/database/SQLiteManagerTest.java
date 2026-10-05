package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ssoggy.ssoggysouls.PluginContext;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SQLiteManagerTest {

    private PluginContext plugin;
    private Logger logger;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        plugin = mock(PluginContext.class);
        logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);
        File dataFolder = tempDir.toFile();
        when(plugin.getDataFolder()).thenReturn(dataFolder);
        when(plugin.getDefaultLives()).thenReturn(3);
    }

    @Test
    void testInitializeWithValidTableName() {
        when(plugin.getConfigString("database.table-name", "hardcore_players")).thenReturn("valid_table_123");
        when(plugin.getConfigInt("database.max-pool-size", 1)).thenReturn(1);

        SQLiteManager sqliteManager = new SQLiteManager(plugin);
        assertDoesNotThrow(sqliteManager::initialize);
        assertEquals("valid_table_123", sqliteManager.getTableName());

        sqliteManager.shutdown();
    }

    @Test
    void testInitializeWithInvalidTableNameInjectedSql() {
        when(plugin.getConfigString("database.table-name", "hardcore_players"))
                .thenReturn("players; DROP TABLE players--");

        SQLiteManager sqliteManager = new SQLiteManager(plugin);
        assertThrows(DatabaseInitializationException.class, sqliteManager::initialize);
    }

    @Test
    void testGetTableNameRejectsInvalidIdentifier() {
        when(plugin.getConfigString("database.table-name", "hardcore_players")).thenReturn("valid_table");
        when(plugin.getConfigInt("database.max-pool-size", 1)).thenReturn(1);

        SQLiteManager sqliteManager = new SQLiteManager(plugin);
        assertDoesNotThrow(sqliteManager::initialize);

        // Manually alter internal tableName field to an invalid value
        sqliteManager.tableName = "invalid_table; SELECT * FROM users";
        assertThrows(IllegalArgumentException.class, sqliteManager::getTableName);

        sqliteManager.shutdown();
    }
}
