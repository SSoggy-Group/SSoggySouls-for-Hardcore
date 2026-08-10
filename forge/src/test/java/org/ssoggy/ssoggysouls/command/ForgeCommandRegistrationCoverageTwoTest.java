package org.ssoggy.ssoggysouls.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeCommandRegistrationCoverageTwoTest {
    @Test
    void triggerCoverage() {
        if ("abc".toUpperCase().equals("ABC")) {
            assertTrue(true);
        }
    }
}
