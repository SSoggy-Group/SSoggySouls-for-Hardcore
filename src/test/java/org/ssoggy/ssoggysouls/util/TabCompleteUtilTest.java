package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabCompleteUtilTest {

    @Test
    void testFilterStartsWith_NormalMatch() {
        List<String> options = List.of("apple", "banana", "apricot", "cherry");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "ap");

        assertEquals(List.of("apple", "apricot"), result);
    }

    @Test
    void testFilterStartsWith_CaseInsensitiveMatch() {
        List<String> options = Arrays.asList("Apple", "banana", "APRICOT", "cherry");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "aP");

        assertEquals(2, result.size());
        assertTrue(result.contains("Apple"));
        assertTrue(result.contains("APRICOT"));
    }

    @Test
    void testFilterStartsWith_EmptyPrefix() {
        List<String> options = Arrays.asList("apple", "banana", "apricot");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "");

        assertEquals(3, result.size());
        assertTrue(result.containsAll(options));
    }

    @Test
    void testFilterStartsWith_NoMatch() {
        List<String> options = Arrays.asList("apple", "banana", "apricot");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "z");

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_EmptyOptions() {
        List<String> options = Collections.emptyList();
        List<String> result = TabCompleteUtil.filterStartsWith(options, "a");

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_ExactMatch() {
        List<String> options = Arrays.asList("apple", "banana");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "apple");

        assertEquals(1, result.size());
        assertEquals("apple", result.get(0));
    }
}
