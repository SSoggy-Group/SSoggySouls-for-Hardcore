package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SQLiteManagerTest {

    private SQLiteManager sqliteManager;
    private PluginContext mockPlugin;
    private File tempDir;
    private final UUID testUuid = UUID.randomUUID();
    private static final String TEST_USER = "TestUser";

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("sqlite-test").toFile();
        mockPlugin = mock(PluginContext.class);

        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(java.util.logging.Level.OFF);
        when(mockPlugin.getLogger()).thenReturn(logger);

        when(mockPlugin.getConfigString("database.table-name", "hardcore_players"))
                .thenReturn("hardcore_players");
        when(mockPlugin.getConfigInt("database.max-pool-size", 1)).thenReturn(1);
        when(mockPlugin.getDataFolder()).thenReturn(tempDir);
        when(mockPlugin.getDefaultLives()).thenReturn(3);

        sqliteManager = new SQLiteManager(mockPlugin);
        sqliteManager.initialize();
    }

    @AfterEach
    void tearDown() {
        if (sqliteManager != null) {
            sqliteManager.shutdown();
        }
        deleteDirectory(tempDir);
    }

    private void deleteDirectory(File file) {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        file.delete();
    }

    @Test
    void testSavePlayerInsert() throws SQLException {
        PlayerData data = new PlayerData(testUuid, TEST_USER, 3, false, 1000L, 2000L, 3000L, 4000L);
        sqliteManager.savePlayer(data);

        // Verify insertion
        try (Connection conn = sqliteManager.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM hardcore_players WHERE uuid = ?")) {
            ps.setString(1, testUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Player should be found in database");
                assertEquals(TEST_USER, rs.getString("username"));
                assertEquals(3, rs.getInt("lives"));
                assertFalse(rs.getBoolean("is_dead"));
                assertEquals(1000L, rs.getLong("first_join"));
                assertEquals(2000L, rs.getLong("last_death"));
                assertEquals(3000L, rs.getLong("last_seen"));
                assertEquals(4000L, rs.getLong("grace_until"));
            }
        }
    }

    @Test
    void testSavePlayerUpsert() throws SQLException {
        // Insert initial data
        PlayerData data = new PlayerData(testUuid, TEST_USER, 3, false, 1000L, 2000L, 3000L, 4000L);
        sqliteManager.savePlayer(data);

        // Update data
        PlayerData updatedData = new PlayerData(testUuid, "NewUser", 1, true, 1000L, 2500L, 3500L, 4500L);
        sqliteManager.savePlayer(updatedData);

        // Verify upsert
        try (Connection conn = sqliteManager.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM hardcore_players WHERE uuid = ?")) {
            ps.setString(1, testUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Player should be found in database");
                assertEquals("NewUser", rs.getString("username"));
                assertEquals(1, rs.getInt("lives"));
                assertTrue(rs.getBoolean("is_dead"));
                assertEquals(1000L, rs.getLong("first_join")); // ON CONFLICT DO UPDATE shouldn't update first_join (not in SET clause)
                assertEquals(2500L, rs.getLong("last_death"));
                assertEquals(3500L, rs.getLong("last_seen"));
                assertEquals(4500L, rs.getLong("grace_until"));
            }
        }
    }
}
