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
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SQLiteManagerTest {

    private PreparedStatement preparedStatement;
    private SQLiteManager sqliteManager;
    private final UUID testUuid = UUID.randomUUID();

    private static final String TEST_USER = "TestUser";

    @BeforeEach
    void setup() throws Exception {
        PluginContext plugin = mock(PluginContext.class);

        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(java.util.logging.Level.OFF);
        when(plugin.getLogger()).thenReturn(logger);

        Connection connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        Statement statement = mock(Statement.class);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(statement);

        javax.sql.DataSource dataSource = new SimpleTestDataSource(connection);

        sqliteManager = new SQLiteManager(plugin, dataSource, "hardcore_players");
    }

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
    void testSavePlayer() throws SQLException {
        PlayerData data = new PlayerData(testUuid, TEST_USER, 3, false, 1000L, 2000L, 3000L, 4000L);

        sqliteManager.savePlayer(data);

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
    void testSavePlayerDebugMode() throws SQLException {
        PluginContext plugin = sqliteManager.plugin;
        when(plugin.isDebugMode()).thenReturn(true);

        PlayerData data = new PlayerData(testUuid, TEST_USER, 3, false, 1000L, 2000L, 3000L, 4000L);
        sqliteManager.savePlayer(data);

        verify(plugin).debug("Saved player data for UUID: " + testUuid);
    }
}
