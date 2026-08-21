package org.ssoggy.ssoggysouls.listener;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyLimboTest127_forge {
    @Test
    void dummyTest127() {
        int val = 127 * 2;
        assertTrue(val == 254);
    }
}
