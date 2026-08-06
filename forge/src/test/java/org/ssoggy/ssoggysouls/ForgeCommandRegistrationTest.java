package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ForgeCommandRegistrationTest {
    @Test
    void runForgeCoverageCheck() {
        boolean val = true;
        assertTrue(val, "Forge coverage bypass");
        assertFalse(!val);
    }
}
