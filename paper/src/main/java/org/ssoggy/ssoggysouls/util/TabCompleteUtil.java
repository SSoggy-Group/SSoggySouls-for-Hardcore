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
        return getOnlinePlayerNames(prefix, null);
    }

    /**
     * gets online player names matching the given prefix, excluding a specific name.
     *
     * @param prefix      the prefix to filter by (case-insensitive)
     * @param excludeName a name to exclude from the results (case-insensitive), or null
     * @return list of matching player names
     */
    public static List<String> getOnlinePlayerNames(String prefix, String excludeName) {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (excludeName != null && player.getName().equalsIgnoreCase(excludeName)) {
                continue;
            }
            if (player.getName().regionMatches(true, 0, prefix, 0, prefix.length())) {
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
    public static List<String> filterStartsWith(List<String> options, String prefix) {
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.regionMatches(true, 0, prefix, 0, prefix.length())) {
                result.add(option);
            }
        }
        return result;
    }
}
