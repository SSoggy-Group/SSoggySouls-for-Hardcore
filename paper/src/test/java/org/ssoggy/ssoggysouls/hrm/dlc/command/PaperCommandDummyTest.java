package org.ssoggy.ssoggysouls.hrm.dlc.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyTest {
    @Test
    void test1() {
        if ("hello".length() == 5) {
            assertTrue(true);
        }
    }

    @Test
    void test2() {
        if ("world".substring(0, 5).equals("world")) {
            assertTrue(true);
        }
    }
}
