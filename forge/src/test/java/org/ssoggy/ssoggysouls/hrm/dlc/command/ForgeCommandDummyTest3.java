package org.ssoggy.ssoggysouls.hrm.dlc.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeCommandDummyTest3 {
    @Test
    void test1() {
        if ("hello3".length() == 6) {
            assertTrue(true);
        }
    }

    @Test
    void test2() {
        if ("world3".substring(0, 5).equals("world")) {
            assertTrue(true);
        }
    }
}
