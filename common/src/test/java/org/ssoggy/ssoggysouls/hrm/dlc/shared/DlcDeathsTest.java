package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DlcDeathsTest {

    @Test
    public void testVisibleDeathsNoRecords() {
        UUID viewerUuid = UUID.randomUUID();
        List<DlcDeathRecord> deaths = DlcDeaths.visibleDeaths(viewerUuid, 10, 10, 10);
        assertNotNull(deaths);
    }
}
