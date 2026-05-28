package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DlcDeathRecordTest {

    @Test
    void testWithHolder() {
        UUID uuid = UUID.randomUUID();
        Instant time = Instant.now();
        DlcDeathRecord record = new DlcDeathRecord(uuid, "TestPlayer", "world", 10, 20, 30, time, null);

        UUID holder = UUID.randomUUID();
        DlcDeathRecord modified = record.withHolder(holder);

        assertEquals(uuid, modified.uuid());
        assertEquals("TestPlayer", modified.username());
        assertEquals("world", modified.worldId());
        assertEquals(10, modified.x());
        assertEquals(20, modified.y());
        assertEquals(30, modified.z());
        assertEquals(time, modified.time());
        assertEquals(holder, modified.holder());
    }
}
