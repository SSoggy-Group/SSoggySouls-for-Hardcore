package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.model.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Logger;
import com.zaxxer.hikari.HikariDataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class MySQLManagerTest {

    @Mock
    private SSoggySouls plugin;

    @Mock
    private FileConfiguration config;

    @Mock
    private Logger logger;

    @Mock
    private HikariDataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    private MySQLManager mySQLManager;
    private final UUID testUuid = UUID.randomUUID();

    @BeforeEach
    public void setup() throws Exception {
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getLogger()).thenReturn(logger);

        // Mock connection setup
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(statement);

        mySQLManager = new MySQLManager(plugin, dataSource, "hardcore_players");
    }

    @Test
    public void testGetPlayer_Found() throws SQLException {
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

        PlayerData data = mySQLManager.getPlayer(testUuid);

        assertNotNull(data);
        assertEquals(testUuid, data.getUuid());
        assertEquals("TestUser", data.getUsername());
        assertEquals(3, data.getLives());
        assertFalse(data.isDead());
        assertEquals(1000L, data.getFirstJoin());
        assertEquals(2000L, data.getLastDeath());
        assertEquals(3000L, data.getLastSeen());
        assertEquals(4000L, data.getGraceUntil());

        verify(preparedStatement).setString(1, testUuid.toString());
    }

    @Test
    public void testGetPlayer_NotFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        PlayerData data = mySQLManager.getPlayer(testUuid);

        assertNull(data);
        verify(preparedStatement).setString(1, testUuid.toString());
    }

    @Test
    public void testSavePlayer() throws SQLException {
        PlayerData data = new PlayerData(testUuid, "TestUser", 3, false, 1000L, 2000L, 3000L, 4000L);

        mySQLManager.savePlayer(data);

        verify(preparedStatement).setString(1, testUuid.toString());
        verify(preparedStatement).setString(2, "TestUser");
        verify(preparedStatement).setInt(3, 3);
        verify(preparedStatement).setBoolean(4, false);
        verify(preparedStatement).setLong(5, 1000L);
        verify(preparedStatement).setLong(6, 2000L);
        verify(preparedStatement).setLong(7, 3000L);
        verify(preparedStatement).setLong(8, 4000L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void testIsPlayerDead_CacheMiss() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean("is_dead")).thenReturn(true);

        boolean isDead = mySQLManager.isPlayerDead(testUuid);

        assertTrue(isDead);
        verify(preparedStatement).setString(1, testUuid.toString());
        verify(preparedStatement).executeQuery();
    }

    @Test
    public void testIsPlayerDead_CacheHit() throws SQLException {
        // First call to populate cache
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean("is_dead")).thenReturn(true);

        assertTrue(mySQLManager.isPlayerDead(testUuid));

        // Reset mock
        reset(preparedStatement);

        // Second call should use cache
        assertTrue(mySQLManager.isPlayerDead(testUuid));
        verify(preparedStatement, never()).executeQuery();
    }

    @Test
    public void testRevivePlayer_Success() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = mySQLManager.revivePlayer(testUuid, 3);

        assertTrue(result);
        verify(preparedStatement).setInt(1, 3);
        verify(preparedStatement).setString(2, testUuid.toString());
    }

    @Test
    public void testRevivePlayer_Failure() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = mySQLManager.revivePlayer(testUuid, 3);

        assertFalse(result);
        verify(preparedStatement).setInt(1, 3);
        verify(preparedStatement).setString(2, testUuid.toString());
    }

    @Test
    public void testGetPlayerByName_Found() throws SQLException {
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

        PlayerData data = mySQLManager.getPlayerByName("TestUser");

        assertNotNull(data);
        assertEquals("TestUser", data.getUsername());
        verify(preparedStatement).setString(1, "TestUser");
    }

    @Test
    public void testGetPlayerByName_NotFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        PlayerData data = mySQLManager.getPlayerByName("UnknownUser");

        assertNull(data);
        verify(preparedStatement).setString(1, "UnknownUser");
    }

    @Test
    public void testSetLives() throws SQLException {
        mySQLManager.setLives(testUuid, 5);

        verify(preparedStatement).setInt(1, 5);
        verify(preparedStatement).setBoolean(2, false);
        verify(preparedStatement).setString(3, testUuid.toString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void testSetLives_Dead() throws SQLException {
        mySQLManager.setLives(testUuid, 0);

        verify(preparedStatement).setInt(1, 0);
        verify(preparedStatement).setBoolean(2, true);
        verify(preparedStatement).setString(3, testUuid.toString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void testSetFirstJoin() throws SQLException {
        mySQLManager.setFirstJoin(testUuid, 12345L);

        verify(preparedStatement).setLong(1, 12345L);
        verify(preparedStatement).setString(2, testUuid.toString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void testSetLastSeen() throws SQLException {
        mySQLManager.setLastSeen(testUuid, 54321L);

        verify(preparedStatement).setLong(1, 54321L);
        verify(preparedStatement).setString(2, testUuid.toString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void testSetGraceUntil() throws SQLException {
        mySQLManager.setGraceUntil(testUuid, 99999L);

        verify(preparedStatement).setLong(1, 99999L);
        verify(preparedStatement).setString(2, testUuid.toString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void testInvalidateDeathStatusCache() throws SQLException {
        // First make them dead to put them in cache
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean("is_dead")).thenReturn(true);

        mySQLManager.isPlayerDead(testUuid); // should cache

        // Invalidate
        mySQLManager.invalidateDeathStatusCache(testUuid);

        // Next check should query again
        mySQLManager.isPlayerDead(testUuid);

        verify(preparedStatement, times(2)).executeQuery();
    }

    @Test
    public void testGetDeadPlayers() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        // Two rows
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        when(resultSet.getString("uuid")).thenReturn(testUuid.toString()).thenReturn(UUID.randomUUID().toString());
        when(resultSet.getString("username")).thenReturn("Dead1").thenReturn("Dead2");
        when(resultSet.getInt("lives")).thenReturn(0);
        when(resultSet.getBoolean("is_dead")).thenReturn(true);

        java.util.List<PlayerData> deadPlayers = mySQLManager.getDeadPlayers();

        assertEquals(2, deadPlayers.size());
        assertEquals("Dead1", deadPlayers.get(0).getUsername());
        assertEquals("Dead2", deadPlayers.get(1).getUsername());
    }

    @Test
    public void testGetPluginVersion_Found() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("version")).thenReturn("1.0.0");

        String version = mySQLManager.getPluginVersion("main");

        assertEquals("1.0.0", version);
        verify(preparedStatement).setString(1, "main");
        // Verify metadata table was created
        verify(statement).execute(anyString());
    }

    @Test
    public void testGetPluginVersion_NotFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        String version = mySQLManager.getPluginVersion("main");

        assertNull(version);
        verify(preparedStatement).setString(1, "main");
    }

    @Test
    public void testSavePluginVersion() throws SQLException {
        mySQLManager.savePluginVersion("main", "2.0.0");

        verify(preparedStatement).setString(1, "main");
        verify(preparedStatement).setString(2, "2.0.0");
        verify(preparedStatement).executeUpdate();
        // Verify metadata table was created
        verify(statement).execute(anyString());
    }

    @Test
    public void testGetPlayer_SQLException() throws SQLException {
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Mock DB Error"));

        PlayerData data = mySQLManager.getPlayer(testUuid);

        assertNull(data);
        verify(logger).log(eq(java.util.logging.Level.WARNING), any(Throwable.class), any(java.util.function.Supplier.class));
    }

    @Test
    public void testSavePlayer_SQLException() throws SQLException {
        PlayerData data = new PlayerData(testUuid, "TestUser", 3, false, 1000L, 2000L, 3000L, 4000L);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Mock DB Error"));

        mySQLManager.savePlayer(data);

        verify(logger).log(eq(java.util.logging.Level.WARNING), any(Throwable.class), any(java.util.function.Supplier.class));
    }

    @Test
    public void testIsPlayerDead_SQLException() throws SQLException {
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Mock DB Error"));

        boolean isDead = mySQLManager.isPlayerDead(testUuid);

        assertTrue(isDead); // Should default to true on error
        verify(logger).log(eq(java.util.logging.Level.WARNING), any(Throwable.class), any(java.util.function.Supplier.class));
    }

    @Test
    public void testGetPluginVersion_SQLException() throws SQLException {
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Mock DB Error"));

        String version = mySQLManager.getPluginVersion("main");

        assertNull(version);
        verify(logger).log(eq(java.util.logging.Level.WARNING), any(Throwable.class), any(java.util.function.Supplier.class));
    }
}
