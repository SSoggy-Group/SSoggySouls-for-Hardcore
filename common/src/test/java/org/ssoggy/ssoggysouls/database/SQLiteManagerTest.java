package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SQLiteManagerTest {

    @TempDir
    Path tempDir;

    private PluginContext plugin;
    private Logger logger;
    private SQLiteManager sqliteManager;

    @BeforeEach
    void setup() {
        plugin = mock(PluginContext.class);
        logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getConfigString(eq("database.table-name"), anyString())).thenReturn("hardcore_players");
        when(plugin.getConfigInt(eq("database.max-pool-size"), anyInt())).thenReturn(1);
        when(plugin.getDefaultLives()).thenReturn(3);

        sqliteManager = new SQLiteManager(plugin);
    }

    @Test
    void testInitializeSuccessAndGetters() throws Exception {
        sqliteManager.initialize();

        assertNotNull(sqliteManager.getDataSource());
        assertEquals(900, sqliteManager.getBatchSize());

        File dbFile = new File(tempDir.toFile(), "database.db");
        assertTrue(dbFile.exists());

        // Verify table schema was created in SQLite DB
        try (Connection conn = sqliteManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='hardcore_players'");
            assertTrue(rs.next());
            assertEquals("hardcore_players", rs.getString("name"));
        }

        sqliteManager.shutdown();
    }

    @Test
    void testInitializeInvalidTableNameThrowsException() {
        when(plugin.getConfigString(eq("database.table-name"), anyString())).thenReturn("invalid-table-name!");

        DatabaseInitializationException exception = assertThrows(
                DatabaseInitializationException.class,
                () -> sqliteManager.initialize()
        );

        assertTrue(exception.getMessage().contains("Invalid database.table-name"));
        verify(logger).log(eq(Level.SEVERE), contains("Invalid database.table-name"), eq("invalid-table-name!"));
    }

    @Test
    void testInitializeCreatesDataFolderIfNotExists() throws Exception {
        File subDir = new File(tempDir.toFile(), "subfolder");
        when(plugin.getDataFolder()).thenReturn(subDir);
        assertFalse(subDir.exists());

        sqliteManager.initialize();

        assertTrue(subDir.exists());
        sqliteManager.shutdown();
    }

    @Test
    void testInitializeDatabaseErrorThrowsInitializationException() {
        // Passing an uncreatable directory path to trigger JDBC / Hikari error
        File fileAsFolder = new File(tempDir.toFile(), "file.txt");
        try {
            assertTrue(fileAsFolder.createNewFile());
        } catch (Exception e) {
            fail("Failed to setup test file");
        }
        File invalidDataFolder = new File(fileAsFolder, "child_folder");
        when(plugin.getDataFolder()).thenReturn(invalidDataFolder);

        assertThrows(DatabaseInitializationException.class, () -> sqliteManager.initialize());
        verify(logger).log(eq(Level.SEVERE), eq("SQLite connection pool error:"), any(Throwable.class));
    }

    @Test
    void testShutdownWhenDataSourceNullOrClosed() {
        // Null datasource - should not throw
        assertDoesNotThrow(() -> sqliteManager.shutdown());

        // Initialized datasource - should close cleanly
        try {
            sqliteManager.initialize();
            sqliteManager.shutdown();
            verify(logger).info("SQLite connection pool closed.");

            // Repeated shutdown when already closed - should be safe/no-op
            assertDoesNotThrow(() -> sqliteManager.shutdown());
        } catch (Exception e) {
            fail("Shutdown test threw exception: " + e.getMessage());
        }
    }

    @Test
    void testSavePlayerAndLoad() throws Exception {
        sqliteManager.initialize();

        UUID uuid = UUID.randomUUID();
        PlayerData player = new PlayerData(uuid, "TestUser", 5, false, 1000L, 2000L, 3000L, 4000L);

        sqliteManager.savePlayer(player);

        PlayerData retrieved = sqliteManager.getPlayer(uuid);
        assertNotNull(retrieved);
        assertEquals(uuid, retrieved.getUuid());
        assertEquals("TestUser", retrieved.getUsername());
        assertEquals(5, retrieved.getLives());
        assertFalse(retrieved.isDead());
        assertEquals(1000L, retrieved.getFirstJoin());
        assertEquals(2000L, retrieved.getLastDeath());
        assertEquals(3000L, retrieved.getLastSeen());
        assertEquals(4000L, retrieved.getGraceUntil());

        // Save update (UPSERT test)
        PlayerData updatedPlayer = new PlayerData(uuid, "TestUser", 4, true, 1000L, 5000L, 6000L, 7000L);
        sqliteManager.savePlayer(updatedPlayer);

        PlayerData retrievedUpdated = sqliteManager.getPlayer(uuid);
        assertNotNull(retrievedUpdated);
        assertEquals(4, retrievedUpdated.getLives());
        assertTrue(retrievedUpdated.isDead());
        assertEquals(5000L, retrievedUpdated.getLastDeath());

        sqliteManager.shutdown();
    }

    @Test
    void testSavePlayerSQLExceptionSwallowed() throws Exception {
        sqliteManager.initialize();

        // Close underlying connection source artificially or drop table to cause error
        try (Connection conn = sqliteManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE hardcore_players;");
        }

        UUID uuid = UUID.randomUUID();
        PlayerData player = new PlayerData(uuid, "TestUser", 3, false, 1000L, 0L, 0L, 0L);

        assertDoesNotThrow(() -> sqliteManager.savePlayer(player));
        verify(logger).log(eq(Level.WARNING), any(Throwable.class), any());

        sqliteManager.shutdown();
    }

    @Test
    void testSaveAndGetPluginVersion() throws Exception {
        sqliteManager.initialize();

        sqliteManager.savePluginVersion("plugin_version", "1.2.3");
        String version = sqliteManager.getPluginVersion("plugin_version");
        assertEquals("1.2.3", version);

        // Test upsert on meta table
        sqliteManager.savePluginVersion("plugin_version", "1.2.4");
        String updatedVersion = sqliteManager.getPluginVersion("plugin_version");
        assertEquals("1.2.4", updatedVersion);

        sqliteManager.shutdown();
    }

    @Test
    void testSavePluginVersionSQLExceptionSwallowed() throws Exception {
        sqliteManager.initialize();

        // Drop metadata table if it exists or trigger table creation failure
        try (Connection conn = sqliteManager.getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE ssoggysouls_meta (invalid_schema INT);");
        }

        assertDoesNotThrow(() -> sqliteManager.savePluginVersion("key", "1.0.0"));
        verify(logger).log(eq(Level.WARNING), any(Throwable.class), any());

        sqliteManager.shutdown();
    }

    @Test
    void testMetadataTableDdl() {
        String ddl = sqliteManager.metadataTableDdl("test_meta");
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS test_meta"));
        assertTrue(ddl.contains("key_ VARCHAR(50) PRIMARY KEY"));
    }
}
