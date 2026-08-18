package org.ssoggy.ssoggysouls.listener;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LimboServerListenerTest {
    @Test
    public void testIsWhitelistedCommand() {
        assertTrue(LimboServerListener.isWhitelistedCommand("/msg"));
        assertTrue(LimboServerListener.isWhitelistedCommand("/MSG"));
        assertTrue(LimboServerListener.isWhitelistedCommand("  /msg  "));
        assertTrue(LimboServerListener.isWhitelistedCommand("/tell username"));

        assertFalse(LimboServerListener.isWhitelistedCommand("/gamemode"));
        assertFalse(LimboServerListener.isWhitelistedCommand("/ban"));
        assertFalse(LimboServerListener.isWhitelistedCommand("random"));
    }
}
