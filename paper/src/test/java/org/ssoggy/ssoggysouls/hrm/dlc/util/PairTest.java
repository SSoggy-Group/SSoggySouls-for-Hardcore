package org.ssoggy.ssoggysouls.hrm.dlc.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PairTest {

    @Test
    void testPairOfAndGetters() {
        Pair<String, Integer> pair = Pair.of("key", 42);

        assertEquals("key", pair.getLeft());
        assertEquals(42, pair.getRight());
    }

    @Test
    void testPairWithNulls() {
        Pair<String, String> pair = Pair.of(null, null);

        assertNull(pair.getLeft());
        assertNull(pair.getRight());
    }
}
