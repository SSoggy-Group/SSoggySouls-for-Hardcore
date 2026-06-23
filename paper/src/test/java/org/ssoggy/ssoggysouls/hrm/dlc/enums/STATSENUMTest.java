package org.ssoggy.ssoggysouls.hrm.dlc.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class STATSENUMTest {
    @Test
    void testValuesCached() {
        assertEquals(STATSENUM.values().length, STATSENUM.VALUES.size());
    }
}
