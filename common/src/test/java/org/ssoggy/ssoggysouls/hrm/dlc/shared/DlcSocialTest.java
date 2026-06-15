package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.PluginContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DlcSocialTest {

    private File tempFolder;
    private UUID owner;
    private UUID target;

    @BeforeEach
    void setup() throws IOException {
        tempFolder = Files.createTempDirectory("dlcservices").toFile();
        PluginContext context = mock(PluginContext.class);
        when(context.getLogger()).thenReturn(mock(Logger.class));
        when(context.getDataFolder()).thenReturn(tempFolder);
        DlcServices.init(context);

        owner = UUID.randomUUID();
        target = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        for (File f : new File(tempFolder, "revivalplus").listFiles()) {
            f.delete();
        }
        new File(tempFolder, "revivalplus").delete();
        tempFolder.delete();
    }

    @Test
    void testSetRelationTo() {
        DlcSocial social = new DlcSocial(owner);

        // Initial state should be untrusted
        assertEquals(DlcRelation.UNTRUSTED, social.getRelationTo(target));

        // Setting relation should return true and update relation
        assertTrue(social.setRelationTo(target, DlcRelation.FRIENDS));
        assertEquals(DlcRelation.FRIENDS, social.getRelationTo(target));

        // Setting same relation again should return false
        assertFalse(social.setRelationTo(target, DlcRelation.FRIENDS));

        // Changing relation should return true
        assertTrue(social.setRelationTo(target, DlcRelation.BLOCKED));
        assertEquals(DlcRelation.BLOCKED, social.getRelationTo(target));

        // Removing relation (setting to null) should return true
        assertTrue(social.setRelationTo(target, null));
        assertEquals(DlcRelation.UNTRUSTED, social.getRelationTo(target));

        // Removing again should return false
        assertFalse(social.setRelationTo(target, null));
    }
}
