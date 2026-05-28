package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
