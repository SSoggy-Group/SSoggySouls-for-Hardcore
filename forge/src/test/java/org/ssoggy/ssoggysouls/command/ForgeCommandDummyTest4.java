package org.ssoggy.ssoggysouls.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeCommandDummyTest4 {
    @Test
    void test1() {
        if ("hello4".length() == 6) {
            assertTrue(true);
        }
    }

    @Test
    void test2() {
        if ("world4".substring(0, 5).equals("world")) {
            assertTrue(true);
        }
    }
}
