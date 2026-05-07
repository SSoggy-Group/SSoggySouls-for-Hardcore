package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Platform-agnostic MessageHelper test.
 * Uses Java Reflection to inject test data, bypassing any platform-specific
 * config loading (e.g. Bukkit YamlConfiguration, Fabric ConfigManager).
 */
class MessageHelperTest {

    private static final String TEST_MSG = "test_msg";
    private static final String HELLO_PLAYER = "Hello %player%!";
    private static final String PLAYER = "player";
    private static final String SSOGGY = "SSoggy";

    @BeforeEach
    @SuppressWarnings("java:S3011") // Suppress accessibility bypass warning
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Use reflection to bypass config loading since we only want to test substitution logic
        Field messagesField = MessageHelper.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> messages = (Map<String, String>) messagesField.get(null);
        messages.clear();
        messages.put(TEST_MSG, HELLO_PLAYER);
        messages.put("multi_replace", "%prefix% You have %amount% %item%.");

        Field prefixField = MessageHelper.class.getDeclaredField("prefix");
        prefixField.setAccessible(true);
        prefixField.set(null, "\u00a78[\u00a74\u2620\u00a78] \u00a7r");
    }

    @Test
    void testGetRawExistingKey() {
        String result = MessageHelper.getRaw(TEST_MSG, PLAYER, SSOGGY);
        assertEquals("Hello SSoggy!", result);
    }

    @Test
    void testGetRawMissingKey() {
        String result = MessageHelper.getRaw("missing_key", PLAYER, SSOGGY);
        assertEquals("&cMissing message: missing_key", result);
    }

    @Test
    void testGetRawMultipleReplacements() {
        String result = MessageHelper.getRaw("multi_replace", "prefix", "&a[Info]", "amount", 5, "item", "apples");
        assertEquals("&a[Info] You have 5 apples.", result);
    }

    @Test
    void testGetRawNoReplacements() {
        String result = MessageHelper.getRaw(TEST_MSG);
        assertEquals(HELLO_PLAYER, result);
    }

    @Test
    void testGetRawMissingReplacementValue() {
        // If uneven replacements are provided, the last key is ignored
        String result = MessageHelper.getRaw(TEST_MSG, PLAYER);
        assertEquals(HELLO_PLAYER, result);
    }

    @Test
    void testColorizeAmperstandToSection() {
        String result = MessageHelper.colorize("&aGreen &cRed");
        assertEquals("\u00a7aGreen \u00a7cRed", result);
    }

    @Test
    void testColorizeNull() {
        String result = MessageHelper.colorize(null);
        assertEquals("", result);
    }

    @Test
    void testGetFormatted() {
        String result = MessageHelper.getFormatted(TEST_MSG, PLAYER, SSOGGY);
        // Should contain the prefix (colorized) + the message
        assertEquals("\u00a78[\u00a74\u2620\u00a78] \u00a7rHello SSoggy!", result);
    }
}
