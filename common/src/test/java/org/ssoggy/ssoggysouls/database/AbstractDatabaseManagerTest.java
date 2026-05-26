package org.ssoggy.ssoggysouls.database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AbstractDatabaseManagerTest {

    @Mock private PluginContext pluginContext;
    @Mock private Logger logger;
    @Mock private DataSource dataSource;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;

    private TestDatabaseManager manager;
    private final UUID testUuid = UUID.randomUUID();

    private static class TestDatabaseManager extends AbstractDatabaseManager {
        private final DataSource testDataSource;

        public TestDatabaseManager(PluginContext plugin, DataSource dataSource) {
            super(plugin);
            this.tableName = "test_table";
            this.testDataSource = dataSource;
        }

        @Override
        public void initialize() throws DatabaseInitializationException {
            // no-op for tests
        }

        @Override
        public void shutdown() {
            // no-op for tests
        }

        @Override
        public void savePlayer(PlayerData data) {
            // no-op for tests
        }

        @Override
        public void savePluginVersion(String key, String version) {
            // no-op for tests
        }

        @Override
        protected DataSource getDataSource() {
            return testDataSource;
        }

        @Override
        protected String metadataTableDdl(String metaTable) {
            return "CREATE TABLE IF NOT EXISTS " + metaTable + " (key VARCHAR(50), version VARCHAR(50))";
        }
    }

    @BeforeEach
    void setUp() {
        when(pluginContext.getLogger()).thenReturn(logger);
        manager = new TestDatabaseManager(pluginContext, dataSource);
    }

    @Test
    void testSetLives() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        manager.setLives(testUuid, 5);

        verify(preparedStatement).setInt(1, 5);
        verify(preparedStatement).setBoolean(2, false);
        verify(preparedStatement).setString(3, testUuid.toString());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testRevivePlayerSuccess() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        manager.deathStatusCache.put(testUuid, true);

        boolean result = manager.revivePlayer(testUuid, 3);

        assertTrue(result, "Should return true when update is successful");
        verify(preparedStatement).setInt(1, 3);
        verify(preparedStatement).setString(2, testUuid.toString());
        verify(preparedStatement).executeUpdate();
        assertFalse(manager.deathStatusCache.get(testUuid), "Cache should be updated to false");
    }

    @Test
    void testRevivePlayerFailureNoRowsAffected() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        manager.deathStatusCache.put(testUuid, true);

        boolean result = manager.revivePlayer(testUuid, 3);

        assertFalse(result, "Should return false when no rows are affected");
        verify(preparedStatement).setInt(1, 3);
        verify(preparedStatement).setString(2, testUuid.toString());
        verify(preparedStatement).executeUpdate();
        assertTrue(manager.deathStatusCache.get(testUuid), "Cache should not be updated");
    }

    @Test
    void testRevivePlayerSQLException() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Mock DB Error"));

        manager.deathStatusCache.put(testUuid, true);

        boolean result = manager.revivePlayer(testUuid, 3);

        assertFalse(result, "Should return false when an exception occurs");
        assertTrue(manager.deathStatusCache.get(testUuid), "Cache should not be updated");
    }
}
