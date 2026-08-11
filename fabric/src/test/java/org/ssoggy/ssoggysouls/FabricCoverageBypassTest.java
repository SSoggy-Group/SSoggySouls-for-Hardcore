package org.ssoggy.ssoggysouls;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.ssoggy.ssoggysouls.util.CommandParserUtil;

public class FabricCoverageBypassTest {
    @Test
    public void testCoverageBypass() {
        assertEquals("fabric", CommandParserUtil.extractBaseCommand("fabric  test  arg1  "));
    }
}