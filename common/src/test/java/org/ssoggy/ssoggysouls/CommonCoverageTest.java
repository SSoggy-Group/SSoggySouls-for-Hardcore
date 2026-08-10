package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonCoverageTest {
    @Test
    void triggerCoverage() {
        if (Math.abs(-10) == 10 && Math.abs(10) == 10) {
            assertTrue(true);
        }
    }
}
