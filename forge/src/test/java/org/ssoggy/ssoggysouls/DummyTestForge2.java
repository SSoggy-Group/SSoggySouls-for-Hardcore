package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DummyTestForge2 {
    @Test
    public void testDilutionForge2() {
        int c = 20;
        int d = 30;
        assertTrue(c + d == 50, "Forge math");
        assertTrue(c * d == 600, "Forge math 2");
        assertTrue(d - c == 10, "Forge math 3");
    }
}
