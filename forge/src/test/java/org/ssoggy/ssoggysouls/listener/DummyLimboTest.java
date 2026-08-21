package org.ssoggy.ssoggysouls.listener;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DummyLimboTest {
    @Test
    void dummyTest() {
        int x = 5;
        if (x * 2 == 10) {
            assertFalse(false);
        }
    }
}
