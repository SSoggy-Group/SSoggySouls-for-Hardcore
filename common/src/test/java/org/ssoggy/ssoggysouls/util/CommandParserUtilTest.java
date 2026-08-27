package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandParserUtilTest {

    @Test
    void testGetFirstWord() {
        assertEquals("/test", CommandParserUtil.getFirstWord("/test with args"));
        assertEquals("/test", CommandParserUtil.getFirstWord("  /test  "));
        assertEquals("/test", CommandParserUtil.getFirstWord("/test"));
        assertEquals("", CommandParserUtil.getFirstWord(""));
    }
}
