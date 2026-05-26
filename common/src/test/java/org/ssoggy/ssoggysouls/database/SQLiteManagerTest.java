package org.ssoggy.ssoggysouls.database;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLiteManagerTest {

    private static final String TEST_USER = "TestUser";

    private final UUID testUuid = UUID.randomUUID();

    @TempDir
    File tempDir;

    private SQLiteManager sqliteManager;

    @BeforeEach
    void setUp() throws Exception {
        PluginContext mockPlugin = mock(PluginContext.class);
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.OFF);

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
        if (file == null || !file.exists()) {
            return;
        }

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
        PlayerData data = new PlayerData(testUuid, TEST_USER, 3, false, 1000L, 2000L, 3000L, 4000L);
        sqliteManager.savePlayer(data);

        PlayerData updatedData = new PlayerData(testUuid, "NewUser", 1, true, 9999L, 2500L, 3500L, 4500L);
        sqliteManager.savePlayer(updatedData);

        try (Connection conn = sqliteManager.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM hardcore_players WHERE uuid = ?")) {
            ps.setString(1, testUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Player should be found in database");
                assertEquals("NewUser", rs.getString("username"));
                assertEquals(1, rs.getInt("lives"));
                assertTrue(rs.getBoolean("is_dead"));
                assertEquals(1000L, rs.getLong("first_join"));
                assertEquals(2500L, rs.getLong("last_death"));
                assertEquals(3500L, rs.getLong("last_seen"));
                assertEquals(4500L, rs.getLong("grace_until"));
            }
        }
    }

    @Test
    void testShutdownClosesDataSource() throws Exception {
        Logger logger = mock(Logger.class);
        HikariDataSource dataSource = mock(HikariDataSource.class);
        SQLiteManager shutdownTestManager = createShutdownTestManager(dataSource, logger);
        when(dataSource.isClosed()).thenReturn(false);

        shutdownTestManager.shutdown();

        verify(dataSource).close();
        verify(logger).info("SQLite connection pool closed.");
    }

    @Test
    void testShutdownDoesNotCloseIfAlreadyClosed() throws Exception {
        Logger logger = mock(Logger.class);
        HikariDataSource dataSource = mock(HikariDataSource.class);
        SQLiteManager shutdownTestManager = createShutdownTestManager(dataSource, logger);
        when(dataSource.isClosed()).thenReturn(true);

        shutdownTestManager.shutdown();

        verify(dataSource, never()).close();
        verify(logger, never()).info(anyString());
    }

    @Test
    void testShutdownHandlesNullDataSource() throws Exception {
        Logger logger = mock(Logger.class);
        SQLiteManager shutdownTestManager = createShutdownTestManager(null, logger);

        assertDoesNotThrow(shutdownTestManager::shutdown);
        verify(logger, never()).info(anyString());
    }

    private SQLiteManager createShutdownTestManager(HikariDataSource dataSource, Logger logger) throws Exception {
        PluginContext plugin = mock(PluginContext.class);
        when(plugin.getLogger()).thenReturn(logger);

        SQLiteManager shutdownTestManager = mock(SQLiteManager.class, CALLS_REAL_METHODS);
        setField(AbstractDatabaseManager.class, shutdownTestManager, "plugin", plugin);
        setField(SQLiteManager.class, shutdownTestManager, "dataSource", dataSource);
        return shutdownTestManager;
    }

    private void setField(Class<?> type, Object target, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
