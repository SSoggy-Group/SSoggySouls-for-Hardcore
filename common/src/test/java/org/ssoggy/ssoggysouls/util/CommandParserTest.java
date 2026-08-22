package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CommandParserTest {

    @Test
    public void testWhitelistedCommands() {
        assertTrue(CommandParser.isWhitelistedCommand("/msg"));
        assertTrue(CommandParser.isWhitelistedCommand("/msg Player hello"));
        assertTrue(CommandParser.isWhitelistedCommand("  /msg  "));
        assertTrue(CommandParser.isWhitelistedCommand("/tell"));
        assertTrue(CommandParser.isWhitelistedCommand("/r"));
        assertTrue(CommandParser.isWhitelistedCommand("/reply"));
        assertTrue(CommandParser.isWhitelistedCommand("/help"));
        assertTrue(CommandParser.isWhitelistedCommand("/list"));
        assertTrue(CommandParser.isWhitelistedCommand("/pstatus"));
        assertTrue(CommandParser.isWhitelistedCommand("/psadmin"));
        assertTrue(CommandParser.isWhitelistedCommand("/psa"));
        assertTrue(CommandParser.isWhitelistedCommand("/revive"));
        assertTrue(CommandParser.isWhitelistedCommand("/psetlives"));
    }

    @Test
    public void testNonWhitelistedCommands() {
        assertFalse(CommandParser.isWhitelistedCommand("/gamemode"));
        assertFalse(CommandParser.isWhitelistedCommand("/tp"));
        assertFalse(CommandParser.isWhitelistedCommand("/give"));
        assertFalse(CommandParser.isWhitelistedCommand("/op"));
        assertFalse(CommandParser.isWhitelistedCommand("hello"));
    }

    @Test
    public void testCaseInsensitivity() {
        assertTrue(CommandParser.isWhitelistedCommand("/MSG"));
        assertTrue(CommandParser.isWhitelistedCommand("/mSg player test"));
        assertTrue(CommandParser.isWhitelistedCommand("/PsAdMiN info"));
    }
}
