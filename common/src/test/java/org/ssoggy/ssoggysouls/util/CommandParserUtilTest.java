package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandParserUtilTest {

    @Test
    void testGetBaseCommand_NoSpaces() {
        assertEquals("help", CommandParserUtil.getBaseCommand("help"));
    }

    @Test
    void testGetBaseCommand_WithSpaces() {
        assertEquals("msg", CommandParserUtil.getBaseCommand("msg Notch hello"));
    }

    @Test
    void testGetBaseCommand_Uppercase() {
        assertEquals("list", CommandParserUtil.getBaseCommand("LIST"));
        assertEquals("reply", CommandParserUtil.getBaseCommand("Reply testing"));
    }

    @Test
    void testGetBaseCommand_LeadingSpaces() {
        assertEquals("tell", CommandParserUtil.getBaseCommand("   tell Notch hello"));
    }

    @Test
    void testGetBaseCommand_Empty() {
        assertEquals("", CommandParserUtil.getBaseCommand(""));
        assertEquals("", CommandParserUtil.getBaseCommand("   "));
    }

    @Test
    void testGetBaseCommand_Slash() {
        assertEquals("/help", CommandParserUtil.getBaseCommand("/help me"));
        assertEquals("/list", CommandParserUtil.getBaseCommand("/LIST "));
    }
}
