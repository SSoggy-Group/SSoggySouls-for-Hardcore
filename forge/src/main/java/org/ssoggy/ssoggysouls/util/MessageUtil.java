package org.ssoggy.ssoggysouls.util;

import net.minecraft.network.chat.Component;

/**
 * Forge-specific MessageUtil.
 * <p>
 * Delegates raw substitution to the common {@link MessageHelper} and wraps
 * results in Minecraft {@link Component} for use with Forge's chat API.
 */
public final class MessageUtil extends MessageHelper {

    private MessageUtil() {}

    // Load from Forge config
    public static void loadMessages() {
        ConfigManager.ModConfig cfg = ConfigManager.getConfig();
        prefix = cfg.getMessagePrefix();
        messages.clear();
        messages.putAll(cfg.getMessages());
    }

    public static String getRawString(String key, Object... replacements) {
        return getRaw(key, replacements);
    }

    public static net.minecraft.network.chat.MutableComponent get(String key, Object... replacements) {
        return colorizeComponent(prefix + getRaw(key, replacements));
    }

    public static net.minecraft.network.chat.MutableComponent getNoPrefix(String key, Object... replacements) {
        return colorizeComponent(getRaw(key, replacements));
    }

    public static net.minecraft.network.chat.MutableComponent colorizeComponent(String text) {
        if (text == null) return Component.empty().copy();
        return Component.literal(text.replace('&', '\u00a7'));
    }
}
