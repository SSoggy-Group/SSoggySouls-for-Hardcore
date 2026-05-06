package org.ssoggy.ssoggysouls.util;

import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public final class MessageUtil {

    private static String prefix = "§8[§4☠§8] §r";
    private static final Map<String, String> messages = new HashMap<>();

    private MessageUtil() {}

    // Visible for testing
    static void setMessages(Map<String, String> testMessages) {
        messages.clear();
        messages.putAll(testMessages);
    }

    static void setPrefix(String testPrefix) {
        prefix = testPrefix;
    }

    // Load from Fabric config
    public static void loadMessages() {
        ConfigManager.ModConfig cfg = ConfigManager.getConfig();
        prefix = cfg.getMessagePrefix();
        messages.clear();
        messages.putAll(cfg.getMessages());
    }

    public static String getRawString(String key, Object... replacements) {
        String messageContent = messages.getOrDefault(key, "§cMissing message: " + key);

        for (int i = 0; i < replacements.length - 1; i += 2) {
            String placeholder = "%" + replacements[i] + "%";
            String value = String.valueOf(replacements[i + 1]);
            messageContent = messageContent.replace(placeholder, value);
        }
        return messageContent;
    }

    public static Text get(String key, Object... replacements) {
        return colorize(prefix + getRawString(key, replacements));
    }

    public static Text getNoPrefix(String key, Object... replacements) {
        return colorize(getRawString(key, replacements));
    }

    public static Text colorize(String text) {
        if (text == null) return Text.empty();
        // Basic translation for ampersand color codes to section symbol
        return Text.literal(text.replace('&', '§'));
    }
}
