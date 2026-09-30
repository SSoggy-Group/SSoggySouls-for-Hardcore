package org.ssoggy.ssoggysouls.util;

import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageUtilTest {

    private static final String PREFIX_KEY = "messages.prefix";
    private static final String MESSAGES_SECTION = "messages";
    private static final String DEFAULT_PREFIX = "&8[&4\u2620&8] &r";
    private static final String TEST_PREFIX = "&b[Test] ";
    private static final String PLAYER_KEY = "player";
    private static final String STEVE = "Steve";

    @Test
    void testColorizeNull() {
        assertEquals("", MessageUtil.colorize(null));
    }

    @Test
    void testColorizeAmpersand() {
        assertEquals("\u00a7aHello \u00a7cWorld", MessageUtil.colorize("&aHello &cWorld"));
    }

    @Test
    void testColorizePlainText() {
        assertEquals("Hello World", MessageUtil.colorize("Hello World"));
    }

    @Test
    void testLoadMessagesWithValidConfig() {
        FileConfiguration config = mock(FileConfiguration.class);
        ConfigurationSection section = mock(ConfigurationSection.class);

        when(config.getString(PREFIX_KEY, DEFAULT_PREFIX)).thenReturn(TEST_PREFIX);
        when(config.getConfigurationSection(MESSAGES_SECTION)).thenReturn(section);
        when(section.getKeys(false)).thenReturn(Set.of("prefix", "welcome", "goodbye"));
        when(config.getString("messages.welcome", "")).thenReturn("&aWelcome %player%!");
        when(config.getString("messages.goodbye", "")).thenReturn("&cGoodbye %player%!");

        MessageUtil.loadMessages(config);

        assertEquals("\u00a7b[Test] \u00a7aWelcome Steve!", MessageUtil.get("welcome", PLAYER_KEY, STEVE));
        assertEquals("\u00a7cGoodbye Alex!", MessageUtil.getNoPrefix("goodbye", PLAYER_KEY, "Alex"));
    }

    @Test
    void testLoadMessagesWithNullSection() {
        FileConfiguration config = mock(FileConfiguration.class);

        when(config.getString(PREFIX_KEY, DEFAULT_PREFIX)).thenReturn("&e[Default] ");
        when(config.getConfigurationSection(MESSAGES_SECTION)).thenReturn(null);

        MessageUtil.loadMessages(config);

        assertEquals("\u00a7e[Default] \u00a7cMissing message: missing_key", MessageUtil.get("missing_key"));
    }
}
