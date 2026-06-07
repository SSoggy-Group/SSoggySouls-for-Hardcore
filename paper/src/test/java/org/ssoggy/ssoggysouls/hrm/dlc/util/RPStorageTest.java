package org.ssoggy.ssoggysouls.hrm.dlc.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RPStorageTest {
    private RPStorage storage;
    private File tempFolder;

    @BeforeEach
    void setup() throws IOException {
        tempFolder = Files.createTempDirectory("rpstorage").toFile();
        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getDataFolder()).thenReturn(tempFolder);
        when(mockPlugin.getLogger()).thenReturn(mock(Logger.class));
        storage = new RPStorage(mockPlugin, "test.yml");
    }

    @AfterEach
    void tearDown() {
        storage.shutdown();
        for (File f : tempFolder.listFiles()) {
            f.delete();
        }
        tempFolder.delete();
    }

    @Test
    public void testSetValueIfChanged() {
        // Test insert
        assertTrue(storage.setValueIfChanged("table", "key", "value1"));
        assertEquals("value1", storage.getValue("table", "key"));

        // Test identical update
        assertFalse(storage.setValueIfChanged("table", "key", "value1"));

        // Test different update
        assertTrue(storage.setValueIfChanged("table", "key", "value2"));
        assertEquals("value2", storage.getValue("table", "key"));
    }
}
