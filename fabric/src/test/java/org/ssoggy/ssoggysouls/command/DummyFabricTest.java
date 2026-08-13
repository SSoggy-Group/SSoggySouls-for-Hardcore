package org.ssoggy.ssoggysouls.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyFabricTest {
    @Test
    void testFabricDummy() {
        int a = 5;
        int b = 10;
        int c = a + b;
        if (c == 15) {
            assertTrue(true, "Math works in fabric");
        } else {
            assertTrue(false);
        }
    }
}
