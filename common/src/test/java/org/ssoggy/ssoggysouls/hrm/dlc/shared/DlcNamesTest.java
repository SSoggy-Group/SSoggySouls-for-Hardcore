package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.PluginContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DlcNamesTest {

    private File tempFolder;

    @BeforeEach
    void setup() throws IOException {
        tempFolder = Files.createTempDirectory("dlcnames").toFile();
        PluginContext context = mock(PluginContext.class);
        when(context.getLogger()).thenReturn(mock(Logger.class));
        when(context.getDataFolder()).thenReturn(tempFolder);
        DlcServices.init(context);
    }

    @AfterEach
    void tearDown() {
        File rplus = new File(tempFolder, "revivalplus");
        if (rplus.exists()) {
            File[] files = rplus.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            rplus.delete();
        }
        tempFolder.delete();
    }

    @Test
    void testCacheAndGet() {
        UUID uuid = UUID.randomUUID();

        // Null checks
        DlcNames.cache(null, "Steve");
        DlcNames.cache(uuid, null);
        DlcNames.cache(uuid, "   ");
        assertNull(DlcNames.get(uuid));
        assertNull(DlcNames.get(null));

        // Valid cache
        DlcNames.cache(uuid, "Steve");
        assertEquals("Steve", DlcNames.get(uuid));
    }

    @Test
    void testGetOrDefault() {
        UUID uuid = UUID.randomUUID();
        assertEquals("Fallback", DlcNames.getOrDefault(uuid, "Fallback"));
        assertEquals("Fallback", DlcNames.getOrDefault(null, "Fallback"));

        DlcNames.cache(uuid, "Alex");
        assertEquals("Alex", DlcNames.getOrDefault(uuid, "Fallback"));
    }

    @Test
    void testFindUuidByName() {
        UUID uuid = UUID.randomUUID();
        DlcNames.cache(uuid, "Steve");

        // Null or blank
        assertFalse(DlcNames.findUuidByName(null).isPresent());
        assertFalse(DlcNames.findUuidByName("").isPresent());
        assertFalse(DlcNames.findUuidByName("   ").isPresent());

        // Case-insensitivity and trim
        Optional<UUID> found = DlcNames.findUuidByName("  sTeVe  ");
        assertTrue(found.isPresent());
        assertEquals(uuid, found.get());

        // Not found
        assertFalse(DlcNames.findUuidByName("UnknownPlayer").isPresent());
    }

    @Test
    void testFindUuidByNameWithInvalidUuidInStorage() {
        // Manually inject an invalid UUID key into username storage
        DlcServices.usernameStorage().setValueIfChanged("usernamecache", "not-a-valid-uuid", "InvalidUser");
        Optional<UUID> found = DlcNames.findUuidByName("InvalidUser");
        assertFalse(found.isPresent());
    }
}
