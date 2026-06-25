package org.ssoggy.ssoggysouls.hrm.dlc.shared;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
class DlcTrustActionTest {
    @Test
    void testValuesCached() {
        assertEquals(DlcTrustAction.values().length, DlcTrustAction.VALUES.size());
    }
}
