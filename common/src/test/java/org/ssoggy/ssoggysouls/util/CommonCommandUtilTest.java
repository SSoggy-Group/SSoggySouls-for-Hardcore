package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommonCommandUtilTest {
    @Test
    void testExtractCommand() {
        assertEquals("/help", CommonCommandUtil.extractCommand("/help"));
        assertEquals("/msg", CommonCommandUtil.extractCommand("/MSG player hello"));
        assertEquals("/tell", CommonCommandUtil.extractCommand("/tell	player	hello"));
        assertEquals("", CommonCommandUtil.extractCommand(""));
        assertEquals("", CommonCommandUtil.extractCommand(null));
    }
}
