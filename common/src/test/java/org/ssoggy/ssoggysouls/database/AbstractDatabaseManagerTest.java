package org.ssoggy.ssoggysouls.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.ssoggy.ssoggysouls.PluginContext;
import org.ssoggy.ssoggysouls.model.PlayerData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for AbstractDatabaseManager.
 */
class AbstractDatabaseManagerTest {

    private PluginContext plugin;
    private Logger logger;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private TestDatabaseManager dbManager;

    private final UUID testUuid = UUID.randomUUID();
    private static final String MOCK_DB_ERROR = "Mock DB Error";

    @BeforeEach
    void setup() throws Exception {
        plugin = mock(PluginContext.class);
        logger = mock(Logger.class); // Mocking Logger to verify error logging
        when(plugin.getLogger()).thenReturn(logger);

        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        dbManager = new TestDatabaseManager(plugin, dataSource);
    }

    @Test
    void testGetPlayerSQLException() throws SQLException {
        // Simulate an SQLException when executeQuery is called
        when(preparedStatement.executeQuery()).thenThrow(new SQLException(MOCK_DB_ERROR));

        // Call getPlayer, which should handle the exception
        PlayerData data = dbManager.getPlayer(testUuid);

        // Verify it gracefully returns null
        assertNull(data, "getPlayer should return null when an SQLException occurs");

        // Verify the logger was called
        verify(logger).log(eq(java.util.logging.Level.WARNING), any(SQLException.class), any(java.util.function.Supplier.class));
    }

    /**
     * Concrete implementation of AbstractDatabaseManager for testing.
     */
    private static class TestDatabaseManager extends AbstractDatabaseManager {
        private final DataSource dataSource;

        public TestDatabaseManager(PluginContext plugin, DataSource dataSource) {
            super(plugin);
            this.dataSource = dataSource;
            this.tableName = "test_table";
        }

        @Override
        protected DataSource getDataSource() {
            return dataSource;
        }

        @Override
        public void initialize() {
        }

        @Override
        public void shutdown() {
        }

        @Override
        public void savePlayer(PlayerData player) {
        }

        @Override
        public String getPluginVersion(String key) {
            return null;
        }

        @Override
        public void savePluginVersion(String key, String version) {
        }

        @Override
        protected String metadataTableDdl(String metaTable) {
            return "";
        }
    }
}
