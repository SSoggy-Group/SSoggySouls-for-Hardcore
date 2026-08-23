package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DlcCommandResultTest {

    @Test
    void testMissingArgsStatus() {
        DlcCommandResult result = DlcCommandResult.missingArgs("test command", "/cmd");
        assertEquals(DlcCommandResult.Status.MISSING_ARGS, result.status());
        assertEquals("test command", result.message());
        assertEquals("/cmd", result.details());
    }

    @Test
    void testOtherStatuses() {
        assertEquals(DlcCommandResult.Status.TRUE, DlcCommandResult.success("msg").status());
        assertEquals(DlcCommandResult.Status.FALSE, DlcCommandResult.fail("msg").status());
        assertEquals(DlcCommandResult.Status.INFO, DlcCommandResult.info("msg").status());
        assertEquals(DlcCommandResult.Status.RAW, DlcCommandResult.raw("msg").status());
    }
}
