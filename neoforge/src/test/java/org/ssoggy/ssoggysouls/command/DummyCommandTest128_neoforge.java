package org.ssoggy.ssoggysouls.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyCommandTest128_neoforge {
    @Test
    void dummyTest128() {
        int val = 128 * 3;
        assertTrue(val == 384);
    }
}
