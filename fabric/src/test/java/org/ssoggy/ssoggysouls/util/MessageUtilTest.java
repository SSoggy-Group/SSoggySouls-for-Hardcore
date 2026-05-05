package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageUtilTest {

    @BeforeEach
    void setUp() throws Exception {
        // Use reflection to bypass config loading since we only want to test substitution logic
        Field messagesField = MessageUtil.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> messages = (Map<String, String>) messagesField.get(null);
        messages.clear();
        messages.put("test_msg", "Hello %player%!");
        messages.put("multi_replace", "%prefix% You have %amount% %item%.");

        Field prefixField = MessageUtil.class.getDeclaredField("prefix");
        prefixField.setAccessible(true);
        prefixField.set(null, "§8[§4☠§8] §r");
    }

    @Test
    void testGetRawString_ExistingKey() {
        String result = MessageUtil.getRawString("test_msg", "player", "SSoggy");
        assertEquals("Hello SSoggy!", result);
    }

    @Test
    void testGetRawString_MissingKey() {
        String result = MessageUtil.getRawString("missing_key", "player", "SSoggy");
        assertEquals("§cMissing message: missing_key", result);
    }

    @Test
    void testGetRawString_MultipleReplacements() {
        String result = MessageUtil.getRawString("multi_replace", "prefix", "§a[Info]", "amount", 5, "item", "apples");
        assertEquals("§a[Info] You have 5 apples.", result);
    }

    @Test
    void testGetRawString_NoReplacements() {
        String result = MessageUtil.getRawString("test_msg");
        assertEquals("Hello %player%!", result);
    }

    @Test
    void testGetRawString_MissingReplacementValue() {
        String result = MessageUtil.getRawString("test_msg", "player");
        assertEquals("Hello %player%!", result);
    }
}
