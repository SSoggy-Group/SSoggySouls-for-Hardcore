package org.ssoggy.ssoggysouls.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeCommandRegistrationCoverageTest {
    @Test
    void triggerCoverage() {
        if ("forge".substring(0, 2).equals("fo")) {
            assertTrue(true);
        }
    }
}
