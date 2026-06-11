package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DlcCommandResultTest {

    @Test
    void testSuccess() {
        DlcCommandResult result = DlcCommandResult.success("test success");
        assertEquals(DlcCommandResult.Status.TRUE, result.status());
        assertEquals("test success", result.message());
        assertNull(result.details());
    }

    @Test
    void testFail() {
        DlcCommandResult result = DlcCommandResult.fail("test fail");
        assertEquals(DlcCommandResult.Status.FALSE, result.status());
        assertEquals("test fail", result.message());
        assertNull(result.details());
    }

    @Test
    void testInfo() {
        DlcCommandResult result = DlcCommandResult.info("test info");
        assertEquals(DlcCommandResult.Status.INFO, result.status());
        assertEquals("test info", result.message());
        assertNull(result.details());
    }

    @Test
    void testRaw() {
        DlcCommandResult result = DlcCommandResult.raw("test raw");
        assertEquals(DlcCommandResult.Status.RAW, result.status());
        assertEquals("test raw", result.message());
        assertNull(result.details());
    }

    @Test
    void testInteractiveTimerError() {
        DlcCommandResult result = DlcCommandResult.interactiveTimerError("my-timer-key");
        assertEquals(DlcCommandResult.Status.INTERACTIVE_TIMER_ERROR, result.status());
        assertEquals("my-timer-key", result.message());
        assertNull(result.details());
    }
}
