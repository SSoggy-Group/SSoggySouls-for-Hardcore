package org.ssoggy.ssoggysouls.util;

import net.minecraft.network.chat.Component;

/**
 * Fabric-specific MessageUtil.
 * <p>
 * Delegates raw substitution to the common {@link MessageHelper} and wraps
 * results in Minecraft {@link Component} for use with Fabric's chat API.
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

    public static Component get(String key, Object... replacements) {
        return colorizeComponent(prefix + getRaw(key, replacements));
    }

    public static Component getNoPrefix(String key, Object... replacements) {
        return colorizeComponent(getRaw(key, replacements));
    }

    public static Component colorizeComponent(String text) {
        if (text == null) return Component.empty();
        return Component.literal(text.replace('&', '\u00a7'));
    }
}
