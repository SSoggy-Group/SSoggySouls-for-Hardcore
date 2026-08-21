package org.ssoggy.ssoggysouls.listener;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyLimboTest128_fabric {
    @Test
    void dummyTest128() {
        int val = 128 * 2;
        assertTrue(val == 256);
    }
}
