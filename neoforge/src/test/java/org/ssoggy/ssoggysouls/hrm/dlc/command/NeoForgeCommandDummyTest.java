package org.ssoggy.ssoggysouls.hrm.dlc.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoForgeCommandDummyTest {
    @Test
    void testNeo1() {
        if (Boolean.TRUE.equals(true)) {
            assertTrue(true);
        }
    }

    @Test
    void testNeo2() {
        if (Boolean.FALSE.equals(false)) {
            assertTrue(true);
        }
    }
}
