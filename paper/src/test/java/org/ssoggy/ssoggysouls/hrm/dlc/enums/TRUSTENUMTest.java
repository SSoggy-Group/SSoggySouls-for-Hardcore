package org.ssoggy.ssoggysouls.hrm.dlc.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TRUSTENUMTest {
    @Test
    void testValuesCached() {
        assertEquals(TRUSTENUM.values().length, TRUSTENUM.VALUES.size());
    }
}
