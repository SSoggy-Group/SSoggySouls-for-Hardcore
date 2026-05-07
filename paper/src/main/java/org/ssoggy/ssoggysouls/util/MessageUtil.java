package org.ssoggy.ssoggysouls.util;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Paper/Bukkit-specific MessageUtil.
 * <p>
 * Delegates raw substitution to the common {@link MessageHelper} and provides
 * Bukkit-specific config loading and Kyori Adventure color serialization.
 */
public final class MessageUtil extends MessageHelper {

    private MessageUtil() {}

    public static void loadMessages(FileConfiguration config) {
        prefix = config.getString("messages.prefix", "&8[&4\u2620&8] &r");

        ConfigurationSection section = config.getConfigurationSection("messages");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (key.equals("prefix")) continue;
                messages.put(key, config.getString("messages." + key, ""));
            }
        }
    }

    public static String get(String key, Object... replacements) {
        return colorize(prefix + getRaw(key, replacements));
    }

    public static String getNoPrefix(String key, Object... replacements) {
        return colorize(getRaw(key, replacements));
    }

    /**
     * Translates ampersand color codes via Kyori Adventure's legacy serializer.
     * This produces proper section-sign color codes for Paper/Bukkit.
     */
    public static String colorize(String text) {
        if (text == null) return "";
        return LegacyComponentSerializer.legacySection().serialize(
                LegacyComponentSerializer.legacyAmpersand().deserialize(text));
    }
}
