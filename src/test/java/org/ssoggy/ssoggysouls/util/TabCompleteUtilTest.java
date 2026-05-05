package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabCompleteUtilTest {

    @Test
    void testFilterStartsWith_StandardMatch() {
        List<String> options = List.of("Apple", "Banana", "Apricot", "Cherry");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "Ap");
        assertEquals(List.of("Apple", "Apricot"), result);
    }

    @Test
    void testFilterStartsWith_CaseInsensitive() {
        List<String> options = Arrays.asList("apple", "Banana", "APRICOT", "Cherry");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "ap");
        assertEquals(Arrays.asList("apple", "APRICOT"), result);
    }

    @Test
    void testFilterStartsWith_NoMatch() {
        List<String> options = Arrays.asList("Apple", "Banana", "Apricot", "Cherry");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "Zebra");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_EmptyOptions() {
        List<String> options = Collections.emptyList();
        List<String> result = TabCompleteUtil.filterStartsWith(options, "Ap");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_EmptyPrefix() {
        List<String> options = Arrays.asList("Apple", "Banana");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "");
        assertEquals(Arrays.asList("Apple", "Banana"), result);
    }

    @Test
    void testFilterStartsWith_ExactMatch() {
        List<String> options = Arrays.asList("Apple", "Banana");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "Apple");
        assertEquals(Collections.singletonList("Apple"), result);
    }
}
