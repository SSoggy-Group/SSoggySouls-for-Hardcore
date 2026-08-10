package org.ssoggy.ssoggysouls.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricCommandDummyTest {
    @Test
    void testFab1() {
        if (1+1 == 2) {
            assertTrue(true);
        }
    }

    @Test
    void testFab2() {
        if (2*2 == 4) {
            assertTrue(true);
        }
    }
}
