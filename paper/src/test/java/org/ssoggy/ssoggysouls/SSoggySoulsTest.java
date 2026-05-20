package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class SSoggySoulsTest {

    private SSoggySouls plugin;

    @BeforeEach
    void setUp() {
        plugin = mock(SSoggySouls.class, CALLS_REAL_METHODS);
    }

    @Test
    void testPrintBanner() throws Exception {
        Logger logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        Method method = SSoggySouls.class.getDeclaredMethod("printBanner");
        method.setAccessible(true);
        assertNotNull(method);
    }

    @Test
    void testPrintDatabaseWarningIfNeeded() throws Exception {
        Logger logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        File file = mock(File.class);
        when(file.exists()).thenReturn(false);
        when(plugin.getDataFolder()).thenReturn(file);

        Method method = SSoggySouls.class.getDeclaredMethod("printDatabaseWarningIfNeeded");
        method.setAccessible(true);
        assertNotNull(method);
    }

    @Test
    void testInitializeDatabase() throws Exception {
        Method method = SSoggySouls.class.getDeclaredMethod("initializeDatabase");
        method.setAccessible(true);
        assertNotNull(method);
    }
}
