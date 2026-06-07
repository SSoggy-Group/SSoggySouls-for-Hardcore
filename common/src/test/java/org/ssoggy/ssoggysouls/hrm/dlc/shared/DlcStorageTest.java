package org.ssoggy.ssoggysouls.hrm.dlc.shared;

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

class DlcStorageTest {
    private DlcStorage storage;
    private File tempFolder;

    @BeforeEach
    void setup() throws IOException {
        tempFolder = Files.createTempDirectory("dlcstorage").toFile();
        storage = new DlcStorage(tempFolder, "test.properties", mock(Logger.class));
    }

    @AfterEach
    void tearDown() {
        for (File f : tempFolder.listFiles()) {
            f.delete();
        }
        tempFolder.delete();
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

        // Test remove via null
        assertTrue(storage.setValueIfChanged("table", "key", null));
        assertFalse(storage.hasValue("table", "key"));

        // Test remove again when already null
        assertFalse(storage.setValueIfChanged("table", "key", null));
    }
}
