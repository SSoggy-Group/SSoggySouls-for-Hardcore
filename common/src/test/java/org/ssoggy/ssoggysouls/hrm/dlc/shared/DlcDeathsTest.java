package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.PluginContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DlcDeathsTest {

    private File tempFolder;

    @BeforeEach
    void setup() throws IOException {
        tempFolder = Files.createTempDirectory("dlcdeaths_test").toFile();
        PluginContext context = mock(PluginContext.class);
        when(context.getLogger()).thenReturn(mock(Logger.class));
        when(context.getDataFolder()).thenReturn(tempFolder);
        DlcServices.init(context);

        // Clear existing deaths in static memory
        for (DlcDeathRecord record : DlcDeaths.allDeaths()) {
            DlcDeaths.clearDeath(record.uuid());
        }
    }

    @AfterEach
    void tearDown() {
        if (tempFolder.exists()) {
            File revivalPlusDir = new File(tempFolder, "revivalplus");
            if (revivalPlusDir.exists()) {
                File[] files = revivalPlusDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
                revivalPlusDir.delete();
            }
            tempFolder.delete();
        }
    }

    @Test
    void testRecordAndClearDeath() {
        UUID uuid = UUID.randomUUID();
        String username = "Alex";
        String world = "world_nether";
        int x = 100, y = 64, z = -200;

        DlcDeaths.recordDeath(uuid, username, world, x, y, z);

        List<DlcDeathRecord> deaths = DlcDeaths.allDeaths();
        assertEquals(1, deaths.size());
        DlcDeathRecord record = deaths.get(0);
        assertEquals(uuid, record.uuid());
        assertEquals(username, record.username());
        assertEquals(world, record.worldId());
        assertEquals(x, record.x());
        assertEquals(y, record.y());
        assertEquals(z, record.z());
        assertNull(record.holder());
        assertEquals(username, DlcNames.get(uuid));

        // Verify storage persistence
        DlcStorage storage = DlcServices.deathStorage();
        assertEquals(username, storage.getValue(uuid.toString(), "username"));
        assertEquals(world, storage.getValue(uuid.toString(), "world"));
        assertEquals(String.valueOf(x), storage.getValue(uuid.toString(), "x"));
        assertEquals(String.valueOf(y), storage.getValue(uuid.toString(), "y"));
        assertEquals(String.valueOf(z), storage.getValue(uuid.toString(), "z"));

        // Clear death
        DlcDeaths.clearDeath(uuid);
        assertTrue(DlcDeaths.allDeaths().isEmpty());
        assertNull(storage.getValue(uuid.toString(), "username"));
    }

    @Test
    void testSetHolder() {
        UUID deadUuid = UUID.randomUUID();
        UUID holderUuid = UUID.randomUUID();

        DlcDeaths.recordDeath(deadUuid, "Bob", "world", 10, 20, 30);

        // Set holder
        DlcDeaths.setHolder(deadUuid, holderUuid);
        DlcDeathRecord record = DlcDeaths.allDeaths().get(0);
        assertEquals(holderUuid, record.holder());
        assertEquals(holderUuid.toString(), DlcServices.deathStorage().getValue(deadUuid.toString(), "holder"));

        // Setting same holder should do nothing
        DlcDeaths.setHolder(deadUuid, holderUuid);
        assertEquals(holderUuid, DlcDeaths.allDeaths().get(0).holder());

        // Remove holder
        DlcDeaths.setHolder(deadUuid, null);
        assertNull(DlcDeaths.allDeaths().get(0).holder());
        assertNull(DlcServices.deathStorage().getValue(deadUuid.toString(), "holder"));

        // Setting holder for non-existent death record should be handled gracefully
        DlcDeaths.setHolder(UUID.randomUUID(), holderUuid);
    }

    @Test
    void testReloadFromStorage() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID holderUuid = UUID.randomUUID();
        Instant now = Instant.now();

        DlcStorage storage = DlcServices.deathStorage();
        storage.setValueIfChanged(uuid1.toString(), "username", "Player1");
        storage.setValueIfChanged(uuid1.toString(), "world", "world");
        storage.setValueIfChanged(uuid1.toString(), "x", "10");
        storage.setValueIfChanged(uuid1.toString(), "y", "64");
        storage.setValueIfChanged(uuid1.toString(), "z", "-10");
        storage.setValueIfChanged(uuid1.toString(), "time", now.toString());
        storage.setValueIfChanged(uuid1.toString(), "holder", holderUuid.toString());

        // Corrupt record (missing world/time)
        storage.setValueIfChanged(uuid2.toString(), "username", "Player2");
        storage.setValueIfChanged(uuid2.toString(), "x", "5");

        DlcDeaths.reloadFromStorage();

        List<DlcDeathRecord> deaths = DlcDeaths.allDeaths();
        assertEquals(1, deaths.size());
        DlcDeathRecord record = deaths.get(0);
        assertEquals(uuid1, record.uuid());
        assertEquals("Player1", record.username());
        assertEquals(holderUuid, record.holder());
    }

    @Test
    void testVisibleDeaths() {
        UUID viewerUuid = UUID.randomUUID();
        UUID deadOwnUuid = viewerUuid;
        UUID deadFriendUuid = UUID.randomUUID();
        UUID deadTrustedUuid = UUID.randomUUID();
        UUID deadUntrustedUuid = UUID.randomUUID();

        // Setup social relations
        DlcSocial friendSocial = new DlcSocial(deadFriendUuid);
        friendSocial.setRelationTo(viewerUuid, DlcRelation.FRIENDS);

        DlcSocial trustedSocial = new DlcSocial(deadTrustedUuid);
        trustedSocial.setRelationTo(viewerUuid, DlcRelation.TRUSTED);

        // Record deaths
        DlcDeaths.recordDeath(deadOwnUuid, "Viewer", "world", 0, 0, 0);
        DlcDeaths.recordDeath(deadFriendUuid, "Friend", "world", 0, 0, 0);
        DlcDeaths.recordDeath(deadTrustedUuid, "Trusted", "world", 0, 0, 0);
        DlcDeaths.recordDeath(deadUntrustedUuid, "Stranger", "world", 0, 0, 0);

        // All deaths recorded just now (0 seconds elapsed)
        // Thresholds: trustedAfter=60s, friendsAfter=120s, publicAfter=300s
        // Only viewer's own death should be visible right now because 0 seconds < thresholds
        List<DlcDeathRecord> visibleNow = DlcDeaths.visibleDeaths(viewerUuid, 60, 120, 300);
        assertEquals(1, visibleNow.size());
        assertEquals(deadOwnUuid, visibleNow.get(0).uuid());
    }

    @Test
    void testAllDeathsSorting() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        DlcDeaths.recordDeath(uuid1, "Player1", "world", 0, 0, 0);
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }
        DlcDeaths.recordDeath(uuid2, "Player2", "world", 0, 0, 0);

        List<DlcDeathRecord> all = DlcDeaths.allDeaths();
        assertEquals(2, all.size());
        assertEquals(uuid1, all.get(0).uuid());
        assertEquals(uuid2, all.get(1).uuid());
        assertTrue(all.get(0).time().isBefore(all.get(1).time()) || all.get(0).time().equals(all.get(1).time()));
    }
}
