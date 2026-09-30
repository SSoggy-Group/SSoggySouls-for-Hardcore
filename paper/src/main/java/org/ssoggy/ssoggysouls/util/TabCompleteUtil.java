package org.ssoggy.ssoggysouls.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class TabCompleteUtil {

    private TabCompleteUtil() {
    }

    /**
     * gets online player names matching the given prefix.
     *
     * @param prefix the prefix to filter by (case-insensitive)
     * @return list of matching player names
     */
    public static List<String> getOnlinePlayerNames(String prefix) {
        String safePrefix = prefix == null ? "" : prefix;
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && player.getName() != null && player.getName().regionMatches(true, 0, safePrefix, 0, safePrefix.length())) {
                names.add(player.getName());
            }
        }
        return names;
    }

    /**
     * filters options by prefix (case-insensitive).
     *
     * @param options list of options to filter
     * @param prefix  the prefix to match
     * @return list of matching options
     */
    public static List<String> filterStartsWith(Iterable<String> options, String prefix) {
        if (options == null) {
            return new ArrayList<>();
        }
        String safePrefix = prefix == null ? "" : prefix;
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option != null && option.regionMatches(true, 0, safePrefix, 0, safePrefix.length())) {
                result.add(option);
            }
        }
        return result;
    }
}
