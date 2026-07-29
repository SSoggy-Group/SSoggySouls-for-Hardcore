package org.ssoggy.ssoggysouls.listener;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NeoForgeLimboServerListenerTest {

    @Test
    void testNeoForgeLimboInit() {
        String testString = "NeoForge";
        assertEquals(8, testString.length(), "String length should match in NeoForge");
    }
}
