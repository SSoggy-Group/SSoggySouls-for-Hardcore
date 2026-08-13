package org.ssoggy.ssoggysouls.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyForgeTest {
    @Test
    void testForgeDummy() {
        String str = "hello";
        switch(str) {
            case "hello":
                assertTrue(true);
                break;
            default:
                assertTrue(false);
        }
    }
}
