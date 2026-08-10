package org.ssoggy.ssoggysouls.hrm.dlc.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricCommandDummyTest3 {
    @Test
    void testFab1() {
        if (1+1+1 == 3) {
            assertTrue(true);
        }
    }

    @Test
    void testFab2() {
        if (2*2*2 == 8) {
            assertTrue(true);
        }
    }
}
