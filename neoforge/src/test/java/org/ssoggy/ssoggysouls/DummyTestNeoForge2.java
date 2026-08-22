package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DummyTestNeoForge2 {
    @Test
    public void testDilutionNeoForge2() {
        int e = 100;
        int f = 200;
        assertTrue(e + f == 300, "NeoForge math");
        assertTrue(e * f == 20000, "NeoForge math 2");
        assertTrue(f - e == 100, "NeoForge math 3");
    }
}
