package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageUtilTest {

    @BeforeEach
    void setUp() {
        FileConfiguration config = new YamlConfiguration();
        config.set("messages.prefix", "&8[&4☠&8] &r");
        config.set("messages.test_msg", "Hello %player%!");
        config.set("messages.multi_replace", "%prefix% You have %amount% %item%.");
        MessageUtil.loadMessages(config);
    }

    @Test
    void testGetRaw_ExistingKey() {
        String result = MessageUtil.getRaw("test_msg", "player", "SSoggy");
        assertEquals("Hello SSoggy!", result);
    }

    @Test
    void testGetRaw_MissingKey() {
        String result = MessageUtil.getRaw("missing_key", "player", "SSoggy");
        assertEquals("&cMissing message: missing_key", result);
    }

    @Test
    void testGetRaw_MultipleReplacements() {
        String result = MessageUtil.getRaw("multi_replace", "prefix", "&a[Info]", "amount", 5, "item", "apples");
        assertEquals("&a[Info] You have 5 apples.", result);
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
