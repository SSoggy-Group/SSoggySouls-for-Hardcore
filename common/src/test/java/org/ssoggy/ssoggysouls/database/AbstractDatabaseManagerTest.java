package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AbstractDatabaseManagerTest {

    private PluginContext plugin;
    private Logger logger;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private TestDatabaseManager dbManager;
    private final UUID testUuid = UUID.randomUUID();

    @BeforeEach
    void setup() throws SQLException {
        plugin = mock(PluginContext.class);
        logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        dbManager = new TestDatabaseManager(plugin, dataSource);
        dbManager.tableName = "test_table";
    }

    private static class TestDatabaseManager extends AbstractDatabaseManager {
        private final DataSource dataSource;

        TestDatabaseManager(PluginContext plugin, DataSource dataSource) {
            super(plugin);
            this.dataSource = dataSource;
        }

        @Override
        protected DataSource getDataSource() {
            return dataSource;
        }

        @Override
        public void initialize() { /* no-op for tests */ }

        @Override
        public void shutdown() { /* no-op for tests */ }

        @Override
        public void savePlayer(PlayerData data) { /* no-op for tests */ }

        @Override
        public void savePluginVersion(String key, String version) { /* no-op for tests */ }

        @Override
        protected String metadataTableDdl(String metaTable) { return ""; }
    }

    @Test
    void testGetPlayerWithNullUuid() {
        assertNull(dbManager.getPlayer(null));
    }

    @Test
    void testGetPlayerFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("uuid")).thenReturn(testUuid.toString());
        when(resultSet.getString("username")).thenReturn("TestUser");
        when(resultSet.getInt("lives")).thenReturn(3);
        when(resultSet.getBoolean("is_dead")).thenReturn(false);
        when(resultSet.getLong("first_join")).thenReturn(1000L);
        when(resultSet.getLong("last_death")).thenReturn(2000L);
        when(resultSet.getLong("last_seen")).thenReturn(3000L);
        when(resultSet.getLong("grace_until")).thenReturn(4000L);

        PlayerData data = dbManager.getPlayer(testUuid);

        assertNotNull(data);
        assertEquals(testUuid, data.getUuid());
        assertEquals("TestUser", data.getUsername());
        verify(preparedStatement).setString(1, testUuid.toString());
    }

    @Test
    void testGetPlayerNotFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertNull(dbManager.getPlayer(testUuid));
        verify(preparedStatement).setString(1, testUuid.toString());
    }

    @Test
    void testGetPlayerSQLException() throws SQLException {
        SQLException sqlException = new SQLException("Mock Error");
        when(preparedStatement.executeQuery()).thenThrow(sqlException);

        assertNull(dbManager.getPlayer(testUuid));

        verify(logger).log(eq(Level.WARNING), eq(sqlException), any(java.util.function.Supplier.class));
    }

    @Test
    void testGetPlayerStrictPropagatesSQLException() throws SQLException {
        // Death handling relies on telling a failed read apart from a missing record
        SQLException sqlException = new SQLException("Mock Error");
        when(preparedStatement.executeQuery()).thenThrow(sqlException);

        assertThrows(SQLException.class, () -> dbManager.getPlayerStrict(testUuid));
    }

    @Test
    void testGetPlayerStrictReturnsNullWhenMissing() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertNull(dbManager.getPlayerStrict(testUuid));
    }

    @Test
    void testIsPlayerDeadCacheHit() throws SQLException {
        dbManager.deathStatusCache.put(testUuid, false);

        assertFalse(dbManager.isPlayerDead(testUuid));
        verify(dataSource, never()).getConnection();
    }

    @Test
    void testIsPlayerDeadDatabaseHitNotDead() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean("is_dead")).thenReturn(false);

        assertFalse(dbManager.isPlayerDead(testUuid));
        verify(preparedStatement).setString(1, testUuid.toString());

        // Second call should hit cache without querying database connection again
        assertFalse(dbManager.isPlayerDead(testUuid));
        verify(dataSource, times(1)).getConnection();
    }

    @Test
    void testIsPlayerDeadDatabaseHitDead() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean("is_dead")).thenReturn(true);

        assertTrue(dbManager.isPlayerDead(testUuid));
        verify(preparedStatement).setString(1, testUuid.toString());

        // Second call should hit cache
        assertTrue(dbManager.isPlayerDead(testUuid));
        verify(dataSource, times(1)).getConnection();
    }

    @Test
    void testIsPlayerDeadNotFoundDefaultsToTrue() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(dbManager.isPlayerDead(testUuid));
        verify(preparedStatement).setString(1, testUuid.toString());
    }

    @Test
    void testIsPlayerDeadSQLExceptionDefaultsToTrue() throws SQLException {
        SQLException sqlException = new SQLException("Database connection error");
        when(preparedStatement.executeQuery()).thenThrow(sqlException);

        assertTrue(dbManager.isPlayerDead(testUuid));
        verify(logger).log(eq(Level.WARNING), eq(sqlException), any(java.util.function.Supplier.class));
    }

    @Test
    void testInvalidateDeathStatusCacheRemovesCachedValue() {
        dbManager.deathStatusCache.put(testUuid, true);
        assertEquals(Boolean.TRUE, dbManager.deathStatusCache.get(testUuid));

        dbManager.invalidateDeathStatusCache(testUuid);

        assertNull(dbManager.deathStatusCache.get(testUuid));
    }

    @Test
    void testInvalidateDeathStatusCacheUncachedOrNull() {
        assertDoesNotThrow(() -> dbManager.invalidateDeathStatusCache(UUID.randomUUID()));
        assertDoesNotThrow(() -> dbManager.invalidateDeathStatusCache(null));
    }

    @Test
    void testRevivePlayerSuccess() throws SQLException {
        when(plugin.isDebugMode()).thenReturn(true);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Pre-populate cache with true to verify cache update
        dbManager.deathStatusCache.put(testUuid, true);

        boolean result = dbManager.revivePlayer(testUuid, 3);

        assertTrue(result);
        assertFalse(dbManager.deathStatusCache.get(testUuid));
        verify(preparedStatement).setInt(1, 3);
        verify(preparedStatement).setString(2, testUuid.toString());
        verify(plugin).debug(contains("Revived player " + testUuid));
    }

    @Test
    void testRevivePlayerFailureNoRowsAffected() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(0);

        dbManager.deathStatusCache.put(testUuid, true);

        boolean result = dbManager.revivePlayer(testUuid, 3);

        assertFalse(result);
        assertTrue(dbManager.deathStatusCache.get(testUuid));
        verify(preparedStatement).setInt(1, 3);
        verify(preparedStatement).setString(2, testUuid.toString());
    }

    @Test
    void testRevivePlayerSQLException() throws SQLException {
        SQLException sqlException = new SQLException("Database connection error");
        when(preparedStatement.executeUpdate()).thenThrow(sqlException);

        boolean result = dbManager.revivePlayer(testUuid, 3);

        assertFalse(result);
        verify(logger).log(eq(Level.WARNING), eq(sqlException), any(java.util.function.Supplier.class));
    }

    @Test
    void testArePlayersDeadBatch() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        Set<UUID> uuids = Set.of(uuid1, uuid2);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("uuid")).thenReturn(uuid1.toString(), uuid2.toString());
        when(resultSet.getBoolean("is_dead")).thenReturn(true, false);

        Map<UUID, Boolean> result = dbManager.arePlayersDead(uuids);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(uuid1));
        assertFalse(result.get(uuid2));

        verify(connection).prepareStatement(contains("WHERE uuid IN (?,?)"));
    }

    @Test
    void testLoadMultipleBatch() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        Set<UUID> uuids = Set.of(uuid1, uuid2);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("uuid")).thenReturn(uuid1.toString(), uuid2.toString());
        when(resultSet.getString("username")).thenReturn("User1", "User2");
        when(resultSet.getInt("lives")).thenReturn(3, 2);
        when(resultSet.getBoolean("is_dead")).thenReturn(false, true);
        when(resultSet.getLong("first_join")).thenReturn(100L, 200L);
        when(resultSet.getLong("last_death")).thenReturn(0L, 250L);
        when(resultSet.getLong("last_seen")).thenReturn(300L, 400L);
        when(resultSet.getLong("grace_until")).thenReturn(0L, 0L);

        Map<UUID, PlayerData> result = dbManager.loadMultiple(uuids);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("User1", result.get(uuid1).getUsername());
        assertEquals("User2", result.get(uuid2).getUsername());
        assertFalse(result.get(uuid1).isDead());
        assertTrue(result.get(uuid2).isDead());

        verify(connection).prepareStatement(contains("WHERE uuid IN (?,?)"));
    }

    @Test
    void testLoadMultipleEmptyAndNull() {
        assertTrue(dbManager.loadMultiple(null).isEmpty());
        assertTrue(dbManager.loadMultiple(Set.of()).isEmpty());

        Set<UUID> onlyNull = new java.util.HashSet<>();
        onlyNull.add(null);
        assertTrue(dbManager.loadMultiple(onlyNull).isEmpty());
    }

    @Test
    void testLoadMultipleSQLException() throws SQLException {
        UUID uuid = UUID.randomUUID();
        SQLException sqlException = new SQLException("Connection failed");
        when(preparedStatement.executeQuery()).thenThrow(sqlException);

        Map<UUID, PlayerData> result = dbManager.loadMultiple(Set.of(uuid));

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(logger).log(eq(Level.WARNING), eq(sqlException), any(java.util.function.Supplier.class));
    }
}
