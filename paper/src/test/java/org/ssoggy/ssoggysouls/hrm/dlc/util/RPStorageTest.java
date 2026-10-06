package org.ssoggy.ssoggysouls.hrm.dlc.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        if (tempFolder.exists() && tempFolder.listFiles() != null) {
            for (File f : tempFolder.listFiles()) {
                f.delete();
            }
        }
        tempFolder.delete();
    }

    @Test
    void testSetValueAndGetValue() {
        storage.setValue("user", "name", "Alex");
        assertEquals("Alex", storage.getValue("user", "name"));
    }

    @Test
    void testSetValueIfChanged() {
        // Test insert
        assertTrue(storage.setValueIfChanged("table", "key", "value1"));
        assertEquals("value1", storage.getValue("table", "key"));

        // Test identical update
        assertFalse(storage.setValueIfChanged("table", "key", "value1"));

        // Test different update
        assertTrue(storage.setValueIfChanged("table", "key", "value2"));
        assertEquals("value2", storage.getValue("table", "key"));
    }

    @Test
    void testRemoveValue() {
        storage.setValue("settings", "theme", "dark");
        assertEquals("dark", storage.getValue("settings", "theme"));

        storage.removeValue("settings", "theme");
        assertNull(storage.getValue("settings", "theme"));
        assertFalse(storage.hasValue("settings", "theme"));
    }

    @Test
    void testHasValue() {
        assertFalse(storage.hasValue("game", "mode"));
        assertFalse(storage.hasValue("game", "mode", "survival"));

        storage.setValue("game", "mode", "survival");
        assertTrue(storage.hasValue("game", "mode"));
        assertTrue(storage.hasValue("game", "mode", "survival"));
        assertFalse(storage.hasValue("game", "mode", "creative"));
    }

    @Test
    void testGetTable() {
        storage.setValue("playerData", "lives", 3);
        storage.setValue("playerData", "score", 100);

        Map<String, Object> table = storage.getTable("playerData");
        assertEquals(3, table.get("lives"));
        assertEquals(100, table.get("score"));
    }

    @Test
    void testSaveAndLoadConfig() throws Exception {
        storage.setValue("config", "version", "1.0");
        storage.saveConfig();

        File file = new File(tempFolder, "test.yml");
        assertTrue(file.exists());
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        String content = "";
        while (System.nanoTime() < deadline) {
            content = Files.readString(file.toPath());
            if (content.contains("version: '1.0'") || content.contains("version: 1.0")) {
                break;
            }
            Thread.sleep(10);
        }
        assertTrue(content.contains("version: '1.0'") || content.contains("version: 1.0"),
                "Expected saved config content within 5 seconds");
    }

    @Test
    void testSaveConfigOverwritesExistingAtomically() throws Exception {
        storage.setValue("key", "val", "initial");
        storage.saveConfig();

        storage.setValue("key", "val", "updated");
        storage.saveConfig();

        File file = new File(tempFolder, "test.yml");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        String content = "";
        while (System.nanoTime() < deadline) {
            content = Files.readString(file.toPath());
            if (content.contains("updated")) {
                break;
            }
            Thread.sleep(10);
        }
        assertTrue(content.contains("updated"), "Expected updated config content to be written atomically");
    }
}
