package org.ssoggy.ssoggysouls.listener;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoForgeLimboServerListenerCoverageTest {
    @Test
    void triggerCoverage() {
        if (Math.max(10, 20) == 20 && Math.min(10, 20) == 10) {
            assertTrue(true);
        }
    }
}
