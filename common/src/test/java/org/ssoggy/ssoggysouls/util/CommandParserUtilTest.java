package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandParserUtilTest {
    @Test
    void testExtractCommand() {
        assertEquals("/msg", CommandParserUtil.extractCommand("/msg player hello"));
        assertEquals("/help", CommandParserUtil.extractCommand("/help  "));
        assertEquals("test", CommandParserUtil.extractCommand("  test  "));
        assertEquals("/test", CommandParserUtil.extractCommand("/test\ttab"));
        assertEquals("", CommandParserUtil.extractCommand(""));
    }
}
