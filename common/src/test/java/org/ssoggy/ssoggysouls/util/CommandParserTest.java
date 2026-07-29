package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandParserTest {

    @Test
    void testExtractBaseCommand() {
        assertEquals("/msg", CommandParser.extractBaseCommand("/msg player hello"));
        assertEquals("/help", CommandParser.extractBaseCommand("/help"));
        assertEquals("/tell", CommandParser.extractBaseCommand(" /tell player  "));
        assertEquals("/r", CommandParser.extractBaseCommand("/r\thello"));
        assertEquals("/pstatus", CommandParser.extractBaseCommand("/pStatus   something"));
    }
}
