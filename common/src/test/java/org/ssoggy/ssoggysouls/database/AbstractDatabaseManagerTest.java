package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AbstractDatabaseManagerTest {

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private Connection connection;
    private TestDatabaseManager dbManager;
    private PluginContext plugin;

    @BeforeEach
    void setup() throws Exception {
        plugin = mock(PluginContext.class);
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.OFF);
        when(plugin.getLogger()).thenReturn(logger);

        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        Statement statement = mock(Statement.class);
        resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(statement);

        DataSource dataSource = new SimpleTestDataSource(connection);
        dbManager = new TestDatabaseManager(plugin, dataSource);
    }

    private static class SimpleTestDataSource implements DataSource {
        private final Connection connection;

        SimpleTestDataSource(Connection connection) {
            this.connection = connection;
        }

        @Override public Connection getConnection() { return connection; }
        @Override public Connection getConnection(String username, String password) { return connection; }
        @Override public PrintWriter getLogWriter() { return null; }
        @Override public void setLogWriter(PrintWriter out) { throw new UnsupportedOperationException("Not implemented"); }
        @Override public void setLoginTimeout(int seconds) { throw new UnsupportedOperationException("Not implemented"); }
        @Override public int getLoginTimeout() { return 0; }
        @Override public Logger getParentLogger() { return Logger.getAnonymousLogger(); }
        @Override public <T> T unwrap(Class<T> iface) { return null; }
        @Override public boolean isWrapperFor(Class<?> iface) { return false; }
    }

    private static class TestDatabaseManager extends AbstractDatabaseManager {
        private final DataSource dataSource;
        private int batchSize = 2; // small batch size for testing

        TestDatabaseManager(PluginContext plugin, DataSource dataSource) {
            super(plugin);
            this.dataSource = dataSource;
            this.tableName = "test_table";
        }

        @Override
        protected DataSource getDataSource() {
            return dataSource;
        }

        @Override
        protected String metadataTableDdl(String metaTable) {
            return "CREATE TABLE IF NOT EXISTS " + metaTable + " (key_ VARCHAR(255), version VARCHAR(255))";
        }

        @Override
        public void initialize() throws DatabaseInitializationException {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void shutdown() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void savePlayer(PlayerData data) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void savePluginVersion(String key, String version) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        protected int getBatchSize() {
            return batchSize;
        }

        // Expose protected method for testing
        public void testCreateMetadataTableIfNeeded(Connection conn, String metaTable) throws SQLException {
            super.createMetadataTableIfNeeded(conn, metaTable);
        }

        public PlayerData testMapResultSet(ResultSet rs) throws SQLException {
            return super.mapResultSet(rs);
        }
    }

    @Test
    void testArePlayersDeadEmpty() {
        Map<UUID, Boolean> result = dbManager.arePlayersDead(null);
        assertTrue(result.isEmpty());

        result = dbManager.arePlayersDead(new HashSet<>());
        assertTrue(result.isEmpty());
    }

    @Test
    void testArePlayersDeadCacheHit() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        dbManager.deathStatusCache.put(uuid1, true);
        dbManager.deathStatusCache.put(uuid2, false);

        Set<UUID> uuids = new HashSet<>();
        uuids.add(uuid1);
        uuids.add(uuid2);

        Map<UUID, Boolean> result = dbManager.arePlayersDead(uuids);

        assertEquals(2, result.size());
        assertTrue(result.get(uuid1));
        assertFalse(result.get(uuid2));

        verify(connection, never()).prepareStatement(anyString());
    }

    @Test
    void testArePlayersDeadPartialCacheMiss() throws SQLException {
        UUID uuid1 = UUID.randomUUID(); // in cache
        UUID uuid2 = UUID.randomUUID(); // not in cache

        dbManager.deathStatusCache.put(uuid1, false);

        Set<UUID> uuids = new HashSet<>();
        uuids.add(uuid1);
        uuids.add(uuid2);

        // Mock result set for fetchDeathStatusBatch
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString("uuid")).thenReturn(uuid2.toString());
        when(resultSet.getBoolean("is_dead")).thenReturn(false);

        Map<UUID, Boolean> result = dbManager.arePlayersDead(uuids);

        assertEquals(2, result.size());
        assertFalse(result.get(uuid1)); // From cache
        assertFalse(result.get(uuid2)); // From SQL (overwriting fail-safe true)

        verify(preparedStatement).setString(1, uuid2.toString());
    }

    @Test
    void testArePlayersDeadFallback() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        Set<UUID> uuids = new HashSet<>();
        uuids.add(uuid1);

        // Mock result set returning empty (no matching row found)
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Map<UUID, Boolean> result = dbManager.arePlayersDead(uuids);

        assertEquals(1, result.size());
        assertTrue(result.get(uuid1)); // Default fail-safe fallback should be true (dead)
    }

    @Test
    void testFetchDeathStatusBatchMultipleIterations() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();
        List<UUID> toFetch = new ArrayList<>();
        toFetch.add(uuid1);
        toFetch.add(uuid2);
        toFetch.add(uuid3);

        Map<UUID, Boolean> result = new java.util.HashMap<>();

        // First batch query returns 2 results, second returns 1
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next())
            // Batch 1 (uuid1, uuid2)
            .thenReturn(true).thenReturn(true).thenReturn(false)
            // Batch 2 (uuid3)
            .thenReturn(true).thenReturn(false);

        when(resultSet.getString("uuid"))
            .thenReturn(uuid1.toString()).thenReturn(uuid2.toString())
            .thenReturn(uuid3.toString());

        when(resultSet.getBoolean("is_dead"))
            .thenReturn(false).thenReturn(true)
            .thenReturn(false);

        dbManager.fetchDeathStatusBatch(toFetch, result);

        assertEquals(3, result.size());
        assertFalse(result.get(uuid1));
        assertTrue(result.get(uuid2));
        assertFalse(result.get(uuid3));

        // It should call executeQuery twice due to batch size of 2
        verify(preparedStatement, times(2)).executeQuery();
    }

    @Test
    void testFetchDeathStatusBatchSQLException() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        List<UUID> toFetch = new ArrayList<>();
        toFetch.add(uuid1);

        Map<UUID, Boolean> result = new java.util.HashMap<>();

        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Batch Exception"));

        assertDoesNotThrow(() -> dbManager.fetchDeathStatusBatch(toFetch, result));

        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateMetadataTableIfNeeded() throws SQLException {
        String metaTable = "test_meta";
        dbManager.testCreateMetadataTableIfNeeded(connection, metaTable);

        String expectedSql = "CREATE TABLE IF NOT EXISTS test_meta (key_ VARCHAR(255), version VARCHAR(255))";
        verify(connection).prepareStatement(expectedSql);
        verify(preparedStatement).execute();
    }

    @Test
    void testMapResultSet() throws SQLException {
        UUID testUuid = UUID.randomUUID();
        when(resultSet.getString("uuid")).thenReturn(testUuid.toString());
        when(resultSet.getString("username")).thenReturn("TestPlayer");
        when(resultSet.getInt("lives")).thenReturn(3);
        when(resultSet.getBoolean("is_dead")).thenReturn(false);
        when(resultSet.getLong("first_join")).thenReturn(100L);
        when(resultSet.getLong("last_death")).thenReturn(200L);
        when(resultSet.getLong("last_seen")).thenReturn(300L);
        when(resultSet.getLong("grace_until")).thenReturn(400L);

        PlayerData data = dbManager.testMapResultSet(resultSet);

        assertNotNull(data);
        assertEquals(testUuid, data.getUuid());
        assertEquals("TestPlayer", data.getUsername());
        assertEquals(3, data.getLives());
        assertFalse(data.isDead());
        assertEquals(100L, data.getFirstJoin());
        assertEquals(200L, data.getLastDeath());
        assertEquals(300L, data.getLastSeen());
        assertEquals(400L, data.getGraceUntil());
    }
}
