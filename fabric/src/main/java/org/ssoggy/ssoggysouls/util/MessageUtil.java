package org.ssoggy.ssoggysouls.util;

import net.minecraft.text.Text;

/**
 * Fabric-specific MessageUtil.
 * <p>
 * Delegates raw substitution to the common {@link MessageHelper} and wraps
 * results in Minecraft {@link Text} for use with Fabric's chat API.
 */
public final class MessageUtil extends MessageHelper {

    private MessageUtil() {}

    // Visible for testing
    static void setMessages(java.util.Map<String, String> testMessages) {
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
        return getRaw(key, replacements);
    }

    public static Text get(String key, Object... replacements) {
        return colorizeText(prefix + getRaw(key, replacements));
    }

    public static Text getNoPrefix(String key, Object... replacements) {
        return colorizeText(getRaw(key, replacements));
    }

    public static Text colorizeText(String text) {
        if (text == null) return Text.empty();
        return Text.literal(text.replace('&', '\u00a7'));
    }
}
