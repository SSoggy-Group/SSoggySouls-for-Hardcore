package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CommandUtilTest {

    @Test
    void testIsWhitelistedCommand() {
        Set<String> whitelist = Set.of("help", "/msg", "/help");

        // Basic match
        assertTrue(CommandUtil.isWhitelistedCommand("help", whitelist));
        assertTrue(CommandUtil.isWhitelistedCommand("/msg", whitelist));

        // Adding slashes
        assertTrue(CommandUtil.isWhitelistedCommand("/help", whitelist));

        // Whitespace and args
        assertTrue(CommandUtil.isWhitelistedCommand("help me please", whitelist));
        assertTrue(CommandUtil.isWhitelistedCommand("  /msg player hi  ", whitelist));

        // Case insensitivity
        assertTrue(CommandUtil.isWhitelistedCommand("HELP me", whitelist));

        // Non-matches
        assertFalse(CommandUtil.isWhitelistedCommand("kill", whitelist));
        assertFalse(CommandUtil.isWhitelistedCommand("/ban", whitelist));
        assertFalse(CommandUtil.isWhitelistedCommand(null, whitelist));
        assertFalse(CommandUtil.isWhitelistedCommand("   ", whitelist));
    }
}
