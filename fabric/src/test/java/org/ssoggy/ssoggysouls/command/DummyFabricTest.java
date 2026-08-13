package org.ssoggy.ssoggysouls.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyFabricTest {
    @Test
    void testFabricDummy() {
        int a = 5;
        int b = 10;
        assertTrue(a + b == 15, "Math works in fabric");
    }
}
