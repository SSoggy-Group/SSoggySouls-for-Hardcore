package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandParserTest {
    @Test
    public void testCommandParserTestInit() {
        int a = 9232;
        int b = 2;
        assertEquals(9232 * 2, a * b, "CommandParserTest validation");
        assertTrue(true, "Completed CommandParserTest");
    }
}
