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

    private static final String KEY_TEST_MSG = "test_msg";
    private static final String TEMPLATE_HELLO_PLAYER = "Hello %player%!";
    private static final String PLACEHOLDER_PLAYER = "player";
    private static final String TEST_PLAYER_NAME = "SSoggy";

    @BeforeEach
    void setUp() {
        // Since MessageHelper's messages map and prefix are protected and in the same package,
        // we can access and modify them directly without reflection.
        MessageHelper.messages.clear();
        MessageHelper.messages.put(KEY_TEST_MSG, TEMPLATE_HELLO_PLAYER);
        MessageHelper.messages.put("multi_replace", "%prefix% You have %amount% %item%.");

        MessageHelper.prefix = "\u00a78[\u00a74\u2620\u00a78] \u00a7r";
    }

    @Test
    void testGetRawExistingKey() {
        String result = MessageHelper.getRaw(KEY_TEST_MSG, PLACEHOLDER_PLAYER, TEST_PLAYER_NAME);
        assertEquals("Hello SSoggy!", result);
    }

    @Test
    void testGetRawMissingKey() {
        String result = MessageHelper.getRaw("missing_key", PLACEHOLDER_PLAYER, TEST_PLAYER_NAME);
        assertEquals("&cMissing message: missing_key", result);
    }

    @Test
    void testGetRawMultipleReplacements() {
        String result = MessageHelper.getRaw("multi_replace", "prefix", "&a[Info]", "amount", 5, "item", "apples");
        assertEquals("&a[Info] You have 5 apples.", result);
    }

    @Test
    void testGetRawNoReplacements() {
        String result = MessageHelper.getRaw(KEY_TEST_MSG);
        assertEquals(TEMPLATE_HELLO_PLAYER, result);
    }

    @Test
    void testGetRawMissingReplacementValue() {
        // If uneven replacements are provided, the last key is ignored
        String result = MessageHelper.getRaw(KEY_TEST_MSG, PLACEHOLDER_PLAYER);
        assertEquals(TEMPLATE_HELLO_PLAYER, result);
    }

    @Test
    void testColorizeAmpersandToSection() {
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
        String result = MessageHelper.getFormatted(KEY_TEST_MSG, PLACEHOLDER_PLAYER, TEST_PLAYER_NAME);
        // Should contain the prefix (colorized) + the message
        assertEquals("\u00a78[\u00a74\u2620\u00a78] \u00a7rHello SSoggy!", result);
    }
}
