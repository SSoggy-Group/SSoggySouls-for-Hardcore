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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for MySQLManager.
 * <p>
 * Uses manual test doubles for JDK module classes (DataSource, Connection, etc.)
 * since Mockito's inline mock maker cannot instrument classes from the java.sql
 * module on Java 21+ without explicit --add-opens / javaagent configuration.
 * Only the PluginContext interface (our own code) is mocked via Mockito.
 */
class MySQLManagerTest {

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private MySQLManager mySQLManager;
    private PluginContext mockPluginContext;
    private final UUID testUuid = UUID.randomUUID();

    private static final String TEST_USER = "TestUser";
    private static final String COL_USERNAME = "username";
    private static final String COL_LIVES = "lives";
    private static final String COL_IS_DEAD = "is_dead";
    private static final String MOCK_DB_ERROR = "Mock DB Error";

    @BeforeEach
    void setup() throws Exception {
        // Use Mockito only for our own interfaces (PluginContext)
        mockPluginContext = mock(PluginContext.class);

        // Use a real anonymous logger
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.OFF);
        when(mockPluginContext.getLogger()).thenReturn(logger);

        // Use Mockito for JDBC interfaces via mock() calls instead of @Mock annotations
        // This avoids the MockitoExtension's field injection which triggers module checks
        Connection connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        Statement statement = mock(Statement.class);
        resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(statement);

        // Create a simple DataSource wrapper that returns our mocked connection
        javax.sql.DataSource dataSource = new SimpleTestDataSource(connection);

        mySQLManager = new MySQLManager(mockPluginContext, dataSource, "hardcore_players");
    }

    /**
     * Simple DataSource implementation for tests that always returns the provided Connection.
     * This avoids needing to mock DataSource (which is in the java.sql module).
     */
    private static class SimpleTestDataSource implements javax.sql.DataSource {
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

    @Test
    void testInitializeThrowsIllegalArgumentExceptionOnInvalidJdbcParams() {
        MySQLManager manager = new MySQLManager(mockPluginContext);

        when(mockPluginContext.getConfigString(eq("database.host"), anyString())).thenReturn("localhost?autoDeserialize=true");
        when(mockPluginContext.getConfigInt(eq("database.port"), anyInt())).thenReturn(3306);
        when(mockPluginContext.getConfigString(eq("database.name"), anyString())).thenReturn("minecraft");
        when(mockPluginContext.getConfigString(eq("database.username"), anyString())).thenReturn("minecraft");
        when(mockPluginContext.getConfigString(eq("database.password"), anyString())).thenReturn("changeme");
        when(mockPluginContext.getConfigInt(eq("database.pool-size"), anyInt())).thenReturn(5);
        when(mockPluginContext.getConfigString(eq("database.table-name"), anyString())).thenReturn("hardcore_players");
        when(mockPluginContext.getConfigString(eq("database.ssl-mode"), anyString())).thenReturn("VERIFY_IDENTITY");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, manager::initialize);
        assertTrue(exception.getMessage().contains("must contain only alphanumeric characters"));
    }

    @Test
    void testGetPlayerFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        when(resultSet.getString("uuid")).thenReturn(testUuid.toString());
        when(resultSet.getString(COL_USERNAME)).thenReturn(TEST_USER);
        when(resultSet.getInt(COL_LIVES)).thenReturn(3);
        when(resultSet.getBoolean(COL_IS_DEAD)).thenReturn(false);
        when(resultSet.getLong("first_join")).thenReturn(1000L);
        when(resultSet.getLong("last_death")).thenReturn(2000L);
        when(resultSet.getLong("last_seen")).thenReturn(3000L);
        when(resultSet.getLong("grace_until")).thenReturn(4000L);

        PlayerData data = mySQLManager.getPlayer(testUuid);

        assertNotNull(data);
        assertEquals(testUuid, data.getUuid());
        assertEquals(TEST_USER, data.getUsername());
        assertEquals(3, data.getLives());
        assertFalse(data.isDead());
        assertEquals(1000L, data.getFirstJoin());
        assertEquals(2000L, data.getLastDeath());
        assertEquals(3000L, data.getLastSeen());
        assertEquals(4000L, data.getGraceUntil());

        verify(preparedStatement).setString(1, testUuid.toString());
    }

    @Test
    void testGetPlayerNotFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        PlayerData data = mySQLManager.getPlayer(testUuid);

        assertNull(data);
        verify(preparedStatement).setString(1, testUuid.toString());
    }

    @Test
    void testGetPlayerNullUuid() {
        PlayerData data = mySQLManager.getPlayer(null);
        assertNull(data);
    }

    @Test
    void testSavePlayer() throws SQLException {
        PlayerData data = new PlayerData(testUuid, TEST_USER, 3, false, 1000L, 2000L, 3000L, 4000L);

        mySQLManager.savePlayer(data);

        verify(preparedStatement).setString(1, testUuid.toString());
        verify(preparedStatement).setString(2, TEST_USER);
        verify(preparedStatement).setInt(3, 3);
        verify(preparedStatement).setBoolean(4, false);
        verify(preparedStatement).setLong(5, 1000L);
        verify(preparedStatement).setLong(6, 2000L);
        verify(preparedStatement).setLong(7, 3000L);
        verify(preparedStatement).setLong(8, 4000L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testIsPlayerDeadCacheMiss() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean(COL_IS_DEAD)).thenReturn(true);

        boolean isDead = mySQLManager.isPlayerDead(testUuid);

        assertTrue(isDead);
        verify(preparedStatement).setString(1, testUuid.toString());
        verify(preparedStatement).executeQuery();
    }

    @Test
    void testIsPlayerDeadCacheHit() throws SQLException {
        // First call to populate cache
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean(COL_IS_DEAD)).thenReturn(true);

        assertTrue(mySQLManager.isPlayerDead(testUuid));

        // Reset mock
        reset(preparedStatement);

        // Second call should use cache
        assertTrue(mySQLManager.isPlayerDead(testUuid));
        verify(preparedStatement, never()).executeQuery();
    }

    @Test
    void testRevivePlayerSuccess() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = mySQLManager.revivePlayer(testUuid, 3);

        assertTrue(result);
        verify(preparedStatement).setInt(1, 3);
        verify(preparedStatement).setString(2, testUuid.toString());
    }

    @Test
    void testRevivePlayerFailure() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = mySQLManager.revivePlayer(testUuid, 3);

        assertFalse(result);
        verify(preparedStatement).setInt(1, 3);
        verify(preparedStatement).setString(2, testUuid.toString());
    }

    @Test
    void testGetPlayerByNameFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        when(resultSet.getString("uuid")).thenReturn(testUuid.toString());
        when(resultSet.getString(COL_USERNAME)).thenReturn(TEST_USER);
        when(resultSet.getInt(COL_LIVES)).thenReturn(3);
        when(resultSet.getBoolean(COL_IS_DEAD)).thenReturn(false);
        when(resultSet.getLong("first_join")).thenReturn(1000L);
        when(resultSet.getLong("last_death")).thenReturn(2000L);
        when(resultSet.getLong("last_seen")).thenReturn(3000L);
        when(resultSet.getLong("grace_until")).thenReturn(4000L);

        PlayerData data = mySQLManager.getPlayerByName(TEST_USER);

        assertNotNull(data);
        assertEquals(TEST_USER, data.getUsername());
        verify(preparedStatement).setString(1, TEST_USER);
    }

    @Test
    void testGetPlayerByNameNotFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        PlayerData data = mySQLManager.getPlayerByName("UnknownUser");

        assertNull(data);
        verify(preparedStatement).setString(1, "UnknownUser");
    }

    @Test
    void testSetLives() throws SQLException {
        mySQLManager.setLives(testUuid, 5);

        verify(preparedStatement).setInt(1, 5);
        verify(preparedStatement).setBoolean(2, false);
        verify(preparedStatement).setString(3, testUuid.toString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testSetLivesDead() throws SQLException {
        mySQLManager.setLives(testUuid, 0);

        verify(preparedStatement).setInt(1, 0);
        verify(preparedStatement).setBoolean(2, true);
        verify(preparedStatement).setString(3, testUuid.toString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testSetFirstJoin() throws SQLException {
        mySQLManager.setFirstJoin(testUuid, 12345L);

        verify(preparedStatement).setLong(1, 12345L);
        verify(preparedStatement).setString(2, testUuid.toString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testSetLastSeen() throws SQLException {
        mySQLManager.setLastSeen(testUuid, 54321L);

        verify(preparedStatement).setLong(1, 54321L);
        verify(preparedStatement).setString(2, testUuid.toString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testSetGraceUntil() throws SQLException {
        mySQLManager.setGraceUntil(testUuid, 99999L);

        verify(preparedStatement).setLong(1, 99999L);
        verify(preparedStatement).setString(2, testUuid.toString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testInvalidateDeathStatusCache() throws SQLException {
        // First make them dead to put them in cache
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean(COL_IS_DEAD)).thenReturn(true);

        mySQLManager.isPlayerDead(testUuid); // should cache

        // Invalidate
        mySQLManager.invalidateDeathStatusCache(testUuid);

        // Next check should query again
        mySQLManager.isPlayerDead(testUuid);

        verify(preparedStatement, times(2)).executeQuery();
    }

    @Test
    void testGetDeadPlayers() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        // Two rows
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        when(resultSet.getString("uuid")).thenReturn(testUuid.toString()).thenReturn(UUID.randomUUID().toString());
        when(resultSet.getString(COL_USERNAME)).thenReturn("Dead1").thenReturn("Dead2");
        when(resultSet.getInt(COL_LIVES)).thenReturn(0);
        when(resultSet.getBoolean(COL_IS_DEAD)).thenReturn(true);

        List<PlayerData> deadPlayers = mySQLManager.getDeadPlayers();

        assertEquals(2, deadPlayers.size());
        assertEquals("Dead1", deadPlayers.get(0).getUsername());
        assertEquals("Dead2", deadPlayers.get(1).getUsername());
    }

    @Test
    void testGetPluginVersionFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("version")).thenReturn("1.0.0");

        String version = mySQLManager.getPluginVersion("main");

        assertEquals("1.0.0", version);
        verify(preparedStatement).setString(1, "main");
        // Verify metadata table was created
        verify(preparedStatement).execute();
    }

    @Test
    void testGetPluginVersionNotFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        String version = mySQLManager.getPluginVersion("main");

        assertNull(version);
        verify(preparedStatement).setString(1, "main");
    }

    @Test
    void testSavePluginVersion() throws SQLException {
        mySQLManager.savePluginVersion("main", "2.0.0");

        verify(preparedStatement).setString(1, "main");
        verify(preparedStatement).setString(2, "2.0.0");
        verify(preparedStatement).executeUpdate();
        // Verify metadata table was created
        verify(preparedStatement).execute();
    }

    @Test
    void testGetPlayerSQLException() throws SQLException {
        when(preparedStatement.executeQuery()).thenThrow(new SQLException(MOCK_DB_ERROR));

        PlayerData data = mySQLManager.getPlayer(testUuid);

        assertNull(data); // Graceful error handling: returns null instead of throwing
    }

    @Test
    void testSavePlayerSQLException() throws SQLException {
        PlayerData data = new PlayerData(testUuid, TEST_USER, 3, false, 1000L, 2000L, 3000L, 4000L);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException(MOCK_DB_ERROR));

        // Should not throw — error is logged and swallowed
        assertDoesNotThrow(() -> mySQLManager.savePlayer(data));
    }

    @Test
    void testIsPlayerDeadSQLException() throws SQLException {
        when(preparedStatement.executeQuery()).thenThrow(new SQLException(MOCK_DB_ERROR));

        boolean isDead = mySQLManager.isPlayerDead(testUuid);

        assertTrue(isDead); // Should default to true on error (fail-safe)
    }

    @Test
    void testGetPluginVersionSQLException() throws SQLException {
        when(preparedStatement.executeQuery()).thenThrow(new SQLException(MOCK_DB_ERROR));

        String version = mySQLManager.getPluginVersion("main");

        assertNull(version); // Graceful error handling: returns null
    }

    @Test
    void testArePlayersDeadEmptyOrNull() {
        Map<UUID, Boolean> resultNull = mySQLManager.arePlayersDead(null);
        assertTrue(resultNull.isEmpty());

        Map<UUID, Boolean> resultEmpty = mySQLManager.arePlayersDead(Collections.emptySet());
        assertTrue(resultEmpty.isEmpty());
    }

    @Test
    void testArePlayersDeadAllCached() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        // Populate cache first
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean(COL_IS_DEAD)).thenReturn(false);
        mySQLManager.isPlayerDead(uuid1);

        when(resultSet.getBoolean(COL_IS_DEAD)).thenReturn(true);
        mySQLManager.isPlayerDead(uuid2);

        reset(preparedStatement); // Reset mock so we can verify it's not called

        Map<UUID, Boolean> results = mySQLManager.arePlayersDead(Set.of(uuid1, uuid2));

        assertEquals(2, results.size());
        assertFalse(results.get(uuid1));
        assertTrue(results.get(uuid2));

        // DB should not be queried since both were in cache
        verify(preparedStatement, never()).executeQuery();
    }

    @Test
    void testArePlayersDeadMixedCacheAndDb() throws SQLException {
        UUID uuidCached = UUID.randomUUID();
        UUID uuidDb1 = UUID.randomUUID();
        UUID uuidDb2 = UUID.randomUUID();

        // Populate cache for one
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean(COL_IS_DEAD)).thenReturn(false);
        mySQLManager.isPlayerDead(uuidCached);

        reset(preparedStatement);

        // Mock DB result for the other two
        // The IN clause has 2 placeholders. We'll return 1 dead, and the other missing (which defaults to true)
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        // DB only returns data for uuidDb1
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString("uuid")).thenReturn(uuidDb1.toString());
        when(resultSet.getBoolean(COL_IS_DEAD)).thenReturn(false);

        Map<UUID, Boolean> results = mySQLManager.arePlayersDead(Set.of(uuidCached, uuidDb1, uuidDb2));

        assertEquals(3, results.size());
        assertFalse(results.get(uuidCached)); // From cache
        assertFalse(results.get(uuidDb1)); // From DB
        assertTrue(results.get(uuidDb2)); // Default to true (missing from DB result)

        // Verify that parameters were set for the 2 UUIDs that were fetched
        verify(preparedStatement).setString(anyInt(), eq(uuidDb1.toString()));
        verify(preparedStatement).setString(anyInt(), eq(uuidDb2.toString()));
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    void testArePlayersDeadSqlException() throws SQLException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        when(preparedStatement.executeQuery()).thenThrow(new SQLException(MOCK_DB_ERROR));

        Map<UUID, Boolean> results = mySQLManager.arePlayersDead(Set.of(uuid1, uuid2));

        assertEquals(2, results.size());
        // Should default to true on error (fail-safe) for all missing/failed items
        assertTrue(results.get(uuid1));
        assertTrue(results.get(uuid2));
    }
}
