package org.ssoggy.ssoggysouls.database;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ssoggy.ssoggysouls.PluginContext;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLiteManagerTest {

    @Mock
    private PluginContext plugin;

    @Mock
    private Logger logger;

    @Mock
    private HikariDataSource dataSource;

    private SQLiteManager sqLiteManager;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        when(plugin.getLogger()).thenReturn(logger);

        // Bypass constructor logic to test shutdown in isolation
        sqLiteManager = mock(SQLiteManager.class, CALLS_REAL_METHODS);

        // Manually inject the plugin context since we bypassed the constructor
        Field pluginField = AbstractDatabaseManager.class.getDeclaredField("plugin");
        pluginField.setAccessible(true);
        pluginField.set(sqLiteManager, plugin);

        // Inject the mocked dataSource using reflection
        Field dataSourceField = SQLiteManager.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(sqLiteManager, dataSource);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void testShutdownClosesDataSource() {
        when(dataSource.isClosed()).thenReturn(false);

        sqLiteManager.shutdown();

        verify(dataSource).close();
        verify(logger).info("SQLite connection pool closed.");
    }

    @Test
    void testShutdownDoesNotCloseIfAlreadyClosed() {
        when(dataSource.isClosed()).thenReturn(true);

        sqLiteManager.shutdown();

        verify(dataSource, never()).close();
        verify(logger, never()).info(anyString());
    }

    @Test
    void testShutdownHandlesNullDataSource() throws Exception {
        // Set dataSource to null
        Field dataSourceField = SQLiteManager.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(sqLiteManager, null);

        assertDoesNotThrow(() -> sqLiteManager.shutdown());
        verify(logger, never()).info(anyString());
    }
}
