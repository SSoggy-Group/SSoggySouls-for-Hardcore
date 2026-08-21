package org.ssoggy.ssoggysouls.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyCommandTest127_neoforge {
    @Test
    void dummyTest127() {
        int val = 127 * 3;
        assertTrue(val == 381);
    }
}
