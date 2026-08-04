package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandParseUtilTest {
    @Test
    void testIsWhitelistedCommand() {
        assertTrue(CommandParseUtil.isWhitelistedCommand("/msg something"));
        assertFalse(CommandParseUtil.isWhitelistedCommand("/ban player"));
    }
}
