package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TabCompleteUtilTest {

    @Test
    void testFilterStartsWith_NormalMatch() {
        List<String> options = List.of("apple", "banana", "apricot", "cherry");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "ap");

        assertEquals(List.of("apple", "apricot"), result);
    }

    @Test
    void testFilterStartsWith_CaseInsensitiveMatch() {
        List<String> options = List.of("Apple", "banana", "APRICOT", "cherry");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "aP");

        assertEquals(List.of("Apple", "APRICOT"), result);
    }

    @Test
    void testFilterStartsWith_EmptyPrefix() {
        List<String> options = List.of("apple", "banana", "apricot");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "");

        assertEquals(List.of("apple", "banana", "apricot"), result);
    }

    @Test
    void testFilterStartsWith_NoMatch() {
        List<String> options = List.of("apple", "banana", "apricot");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "z");

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_EmptyOptions() {
        List<String> options = List.of();
        List<String> result = TabCompleteUtil.filterStartsWith(options, "a");

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_ExactMatch() {
        List<String> options = List.of("apple", "banana");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "apple");

        assertEquals(List.of("apple"), result);
    }

    @Test
    void testFilterStartsWith_NullPrefix() {
        List<String> options = List.of("apple", "banana");
        assertThrows(NullPointerException.class, () -> TabCompleteUtil.filterStartsWith(options, null));
    }

    @Test
    void testFilterStartsWith_NullOptionsList() {
        assertThrows(NullPointerException.class, () -> TabCompleteUtil.filterStartsWith(null, "ap"));
    }

    @Test
    void testFilterStartsWith_OptionsContainsNull() {
        // List.of does not allow nulls, so we use Arrays.asList here
        List<String> options = java.util.Arrays.asList("apple", null, "apricot");
        assertThrows(NullPointerException.class, () -> TabCompleteUtil.filterStartsWith(options, "ap"));
    }
}
