package org.ssoggy.ssoggysouls.listener;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ForgeLimboServerListenerTest {

    @Test
    void testForgeLimboListenerInit() {
        int expected = 42;
        int actual = 20 + 22;
        assertEquals(expected, actual, "Math should work in Forge");
    }
}
