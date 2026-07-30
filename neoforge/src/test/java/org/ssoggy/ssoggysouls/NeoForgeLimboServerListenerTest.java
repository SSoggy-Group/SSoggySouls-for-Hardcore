package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.ssoggy.ssoggysouls.util.CommandParsingUtil;

public class NeoForgeLimboServerListenerTest {
    @Test
    public void testNeoForgeLimboServerListenerInit() {
        assertTrue(CommandParsingUtil.isWhitelistedCommand("/msg test"));
        assertTrue(CommandParsingUtil.isWhitelistedCommand("   /msg    test   "));
        assertTrue(CommandParsingUtil.isWhitelistedCommand("/help"));
        assertTrue(CommandParsingUtil.isWhitelistedCommand("/revive target"));

        assertFalse(CommandParsingUtil.isWhitelistedCommand("/ban user"));
        assertFalse(CommandParsingUtil.isWhitelistedCommand("unknowncommand arg"));
        assertFalse(CommandParsingUtil.isWhitelistedCommand(""));
    }
}
