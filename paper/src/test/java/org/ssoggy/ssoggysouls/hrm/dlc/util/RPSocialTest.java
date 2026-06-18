package org.ssoggy.ssoggysouls.hrm.dlc.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.hrm.dlc.enums.SOCIALENUM;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RPSocialTest {
    private File tempFolder;
    private RPStorage mockStorage;
    private JavaPlugin mockPlugin;

    @BeforeEach
    void setup() throws IOException {
        tempFolder = Files.createTempDirectory("rpsocial").toFile();
        mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getDataFolder()).thenReturn(tempFolder);
        when(mockPlugin.getLogger()).thenReturn(mock(Logger.class));
        mockStorage = new RPStorage(mockPlugin, "social.yml");
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
    void testSetRelationTo() {
        UUID owner = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        RPSocial social = new RPSocial(owner);

        // Setting a new relationship should return true
        assertTrue(social.setRelationTo(target, SOCIALENUM.FRIENDS));
        assertEquals(SOCIALENUM.FRIENDS, social.getRelationTo(target));

        // Setting the exact same relationship should return false
        assertFalse(social.setRelationTo(target, SOCIALENUM.FRIENDS));

        // Changing the relationship should return true
        assertTrue(social.setRelationTo(target, SOCIALENUM.BLOCKED));
        assertEquals(SOCIALENUM.BLOCKED, social.getRelationTo(target));

        // Clearing an existing relationship should return true
        assertTrue(social.setRelationTo(target, null));
        assertEquals(SOCIALENUM.UNTRUSTED, social.getRelationTo(target)); // UNTRUSTED is the default

        // Clearing a non-existent relationship should return false
        assertFalse(social.setRelationTo(target, null));
    }

    @Test
    void testGetRelationsToAll() {
        UUID owner = UUID.randomUUID();
        UUID target1 = UUID.randomUUID();
        UUID target2 = UUID.randomUUID();
        RPSocial social = new RPSocial(owner);

        social.setRelationTo(target1, SOCIALENUM.FRIENDS);
        social.setRelationTo(target2, SOCIALENUM.BLOCKED);

        Map<UUID, SOCIALENUM> relations = social.getRelationsToAll(null);
        assertEquals(2, relations.size());
        assertEquals(SOCIALENUM.FRIENDS, relations.get(target1));
        assertEquals(SOCIALENUM.BLOCKED, relations.get(target2));

        Map<UUID, SOCIALENUM> filtered = social.getRelationsToAll((u, r) -> r == SOCIALENUM.FRIENDS);
        assertEquals(1, filtered.size());
        assertEquals(SOCIALENUM.FRIENDS, filtered.get(target1));
    }

    @Test
    void testSaveChanges() {
        UUID owner = UUID.randomUUID();
        RPSocial social = new RPSocial(owner);
        social.saveChanges();
    }
}
