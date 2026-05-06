package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Platform-agnostic MessageUtil test.
 * Uses Java Reflection to inject test data, bypassing any platform-specific
 * config loading (e.g. Bukkit YamlConfiguration, Fabric ConfigManager).
 */
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
        prefixField.set(null, "\u00a78[\u00a74\u2620\u00a78] \u00a7r");
    }

    @Test
    void testGetRaw_ExistingKey() {
        String result = MessageUtil.getRaw("test_msg", "player", "SSoggy");
        assertEquals("Hello SSoggy!", result);
    }

    @Test
    void testGetRaw_MissingKey() {
        String result = MessageUtil.getRaw("missing_key", "player", "SSoggy");
        assertEquals("\u0026cMissing message: missing_key", result);
    }

    @Test
    void testGetRaw_MultipleReplacements() {
        String result = MessageUtil.getRaw("multi_replace", "prefix", "\u0026a[Info]", "amount", 5, "item", "apples");
        assertEquals("\u0026a[Info] You have 5 apples.", result);
    }

    @Test
    void testGetRaw_NoReplacements() {
        String result = MessageUtil.getRaw("test_msg");
        assertEquals("Hello %player%!", result);
    }

    @Test
    void testGetRaw_MissingReplacementValue() {
        // If uneven replacements are provided, the last key is ignored
        String result = MessageUtil.getRaw("test_msg", "player");
        assertEquals("Hello %player%!", result);
    }
}
