package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageUtilTest {
    private static final String TEST_MSG_KEY = "test_msg";
    private static final String TEST_MSG_CONTENT = "Hello %player%!";
    private static final String PLAYER_PLACEHOLDER = "player";
    private static final String PLAYER_NAME = "SSoggy";

    @BeforeEach
    void setUp() {
        // Use direct methods to inject test data instead of reflection
        Map<String, String> messages = new HashMap<>();
        messages.put(TEST_MSG_KEY, TEST_MSG_CONTENT);
        messages.put("multi_replace", "%prefix% You have %amount% %item%.");
        MessageUtil.setMessages(messages);

        MessageUtil.setPrefix("§8[§4☠§8] §r");
    }

    @Test
    void testGetRawStringExistingKey() {
        String result = MessageUtil.getRawString(TEST_MSG_KEY, PLAYER_PLACEHOLDER, PLAYER_NAME);
        assertEquals("Hello SSoggy!", result);
    }

    @Test
    void testGetRawStringMissingKey() {
        String result = MessageUtil.getRawString("missing_key", PLAYER_PLACEHOLDER, PLAYER_NAME);
        assertEquals("§cMissing message: missing_key", result);
    }

    @Test
    void testGetRawStringMultipleReplacements() {
        String result = MessageUtil.getRawString("multi_replace", "prefix", "§a[Info]", "amount", 5, "item", "apples");
        assertEquals("§a[Info] You have 5 apples.", result);
    }

    @Test
    void testGetRawStringNoReplacements() {
        String result = MessageUtil.getRawString(TEST_MSG_KEY);
        assertEquals(TEST_MSG_CONTENT, result);
    }

    @Test
    void testGetRawStringMissingReplacementValue() {
        String result = MessageUtil.getRawString(TEST_MSG_KEY, PLAYER_PLACEHOLDER);
        assertEquals(TEST_MSG_CONTENT, result);
    }
}
