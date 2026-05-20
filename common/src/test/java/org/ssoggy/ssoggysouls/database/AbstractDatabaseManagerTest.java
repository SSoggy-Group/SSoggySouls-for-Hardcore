package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.PluginContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AbstractDatabaseManagerTest {

    private PluginContext plugin;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private TestDatabaseManager dbManager;
    private final UUID testUuid = UUID.randomUUID();

    // A concrete subclass for testing the abstract class methods
    private static class TestDatabaseManager extends AbstractDatabaseManager {
        private final DataSource testDataSource;

        public TestDatabaseManager(PluginContext plugin, DataSource testDataSource) {
            super(plugin);
            this.testDataSource = testDataSource;
            this.tableName = "test_table";
        }

        @Override
        protected DataSource getDataSource() {
            return testDataSource;
        }

        @Override
        public void initialize() throws DatabaseInitializationException { /* No-op */ }

        @Override
        public void shutdown() { /* No-op */ }

        @Override
        public void savePlayer(org.ssoggy.ssoggysouls.model.PlayerData data) { /* No-op */ }

        @Override
        public void savePluginVersion(String key, String version) { /* No-op */ }

        @Override
        protected String metadataTableDdl(String metaTable) {
            return "MOCK DDL";
        }
    }

    @BeforeEach
    void setup() throws Exception {
        plugin = mock(PluginContext.class);
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(java.util.logging.Level.OFF);
        when(plugin.getLogger()).thenReturn(logger);

        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        dbManager = new TestDatabaseManager(plugin, dataSource);
    }

    @Test
    void testRevivePlayerSuccess() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Populate cache to check if it gets invalidated/updated
        dbManager.deathStatusCache.put(testUuid, true);

        // Act
        boolean result = dbManager.revivePlayer(testUuid, 3);

        // Assert
        assertTrue(result, "Should return true when update is successful");

        verify(preparedStatement).setInt(1, 3);
        verify(preparedStatement).setString(2, testUuid.toString());
        verify(preparedStatement).executeUpdate();

        // Cache should be updated to false
        assertFalse(dbManager.deathStatusCache.get(testUuid), "Cache should be updated to false");
    }

    @Test
    void testRevivePlayerFailureNoRowsAffected() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Populate cache
        dbManager.deathStatusCache.put(testUuid, true);

        // Act
        boolean result = dbManager.revivePlayer(testUuid, 3);

        // Assert
        assertFalse(result, "Should return false when no rows are affected");

        verify(preparedStatement).setInt(1, 3);
        verify(preparedStatement).setString(2, testUuid.toString());
        verify(preparedStatement).executeUpdate();

        // Cache should remain true
        assertTrue(dbManager.deathStatusCache.get(testUuid), "Cache should not be updated");
    }

    @Test
    void testRevivePlayerSQLException() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Mock DB Error"));

        // Populate cache
        dbManager.deathStatusCache.put(testUuid, true);

        // Act
        boolean result = dbManager.revivePlayer(testUuid, 3);

        // Assert
        assertFalse(result, "Should return false when an exception occurs");

        // Cache should remain true
        assertTrue(dbManager.deathStatusCache.get(testUuid), "Cache should not be updated");
    }
}
