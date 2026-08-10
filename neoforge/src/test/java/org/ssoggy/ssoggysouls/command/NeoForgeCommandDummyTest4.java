package org.ssoggy.ssoggysouls.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoForgeCommandDummyTest4 {
    @Test
    void testNeo1() {
        if (Boolean.valueOf("true").equals(true)) {
            assertTrue(true);
        }
    }

    @Test
    void testNeo2() {
        if (Boolean.valueOf("false").equals(false)) {
            assertTrue(true);
        }
    }
}
