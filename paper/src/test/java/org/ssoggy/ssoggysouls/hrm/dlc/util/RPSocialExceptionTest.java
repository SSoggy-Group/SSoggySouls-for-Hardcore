package org.ssoggy.ssoggysouls.hrm.dlc.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RPSocialExceptionTest {
    private File tempFolder;
    private RPStorage mockStorage;
    private JavaPlugin mockPlugin;

    @BeforeEach
    void setup() throws IOException {
        tempFolder = Files.createTempDirectory("rpsocial_ex").toFile();
        mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getDataFolder()).thenReturn(tempFolder);
        when(mockPlugin.getLogger()).thenReturn(mock(Logger.class));
        mockStorage = new RPStorage(mockPlugin, "social_ex.yml");
        RPStatic.SOCIAL_STORAGE = mockStorage;
    }

    @AfterEach
    void tearDown() {
        mockStorage.shutdown();
        for (File f : tempFolder.listFiles()) {
            f.delete();
        }
        tempFolder.delete();
    }

    @Test
    void testGetRelationsToAllException() {
        UUID owner = UUID.randomUUID();
        RPSocial social = new RPSocial(owner);
        // Introduce corrupt data to trigger Exception in riskyOrDefault or getTable
        RPStatic.SOCIAL_STORAGE.setValue(owner.toString(), "not-a-uuid", "INVALID_ENUM");

        // This should hit the catch block when attempting to parse "not-a-uuid" as UUID
        Map<?, ?> relations = social.getRelationsToAll(null);
        assertTrue(relations.isEmpty());
    }
}
