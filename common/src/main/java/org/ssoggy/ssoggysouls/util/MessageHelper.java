package org.ssoggy.ssoggysouls.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Platform-agnostic message utility.
 * <p>
 * Provides raw string substitution and basic color-code translation.
 * Platform-specific wrappers (Paper, Fabric, Forge) extend this with
 * their own {@code get()} methods that return the appropriate chat type
 * (Kyori Component, Minecraft Text, Minecraft Component).
 */
public class MessageHelper {
    public static final String COMMAND_ONLY_PLAYERS_KEY = "command-only-players";

    protected static String prefix = "\u00a78[\u00a74\u2620\u00a78] \u00a7r";
    protected static final Map<String, String> messages = new HashMap<>();

    protected MessageHelper() {}

    /**
     * Performs placeholder substitution on a message key.
     *
     * @param key          the message key
     * @param replacements alternating key-value pairs: "player", "SSoggy", "lives", 3
     * @return the substituted raw string, or an error string if the key is missing
     */
    public static String getRaw(String key, Object... replacements) {
        String messageContent = messages.getOrDefault(key, "&cMissing message: " + key);

        for (int i = 0; i < replacements.length - 1; i += 2) {
            String placeholder = "%" + replacements[i] + "%";
            String value = String.valueOf(replacements[i + 1]);
            messageContent = messageContent.replace(placeholder, value);
        }
        return messageContent;
    }

    /**
     * Translates ampersand color codes (&amp;) to section-sign (§) color codes.
     *
     * @param text the input text
     * @return the text with color codes translated
     */
    public static String translateAlternateColorCodes(String text) {
        if (text == null) return "";
        return text.replace('&', '\u00a7');
    }

    /**
     * Returns the prefixed, colorized raw string for a message key.
     *
     * @param key          the message key
     * @param replacements alternating key-value pairs
     * @return the formatted string
     */
    public static String getFormatted(String key, Object... replacements) {
        return translateAlternateColorCodes(prefix + getRaw(key, replacements));
    }

    /**
     * Returns the colorized raw string without prefix.
     *
     * @param key          the message key
     * @param replacements alternating key-value pairs
     * @return the formatted string without prefix
     */
    public static String getFormattedNoPrefix(String key, Object... replacements) {
        return translateAlternateColorCodes(getRaw(key, replacements));
    }
}
