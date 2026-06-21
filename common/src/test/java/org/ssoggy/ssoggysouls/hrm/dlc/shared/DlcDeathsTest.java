package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.Test;
import java.util.UUID;

public class DlcDeathsTest {
    @Test
    public void testRecordDeath() {
        // Simple sanity check that the class can be loaded
        UUID testUuid = UUID.randomUUID();
        DlcDeaths.clearDeath(testUuid);
    }
}
