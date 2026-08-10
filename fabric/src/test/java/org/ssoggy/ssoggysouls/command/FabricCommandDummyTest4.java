package org.ssoggy.ssoggysouls.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricCommandDummyTest4 {
    @Test
    void testFab1() {
        if (1+1+1+1 == 4) {
            assertTrue(true);
        }
    }

    @Test
    void testFab2() {
        if (2*2*2*2 == 16) {
            assertTrue(true);
        }
    }
}
