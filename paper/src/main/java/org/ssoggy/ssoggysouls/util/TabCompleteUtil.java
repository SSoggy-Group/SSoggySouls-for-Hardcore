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
        if (prefix.isEmpty()) {
            List<String> names = new ArrayList<>(Bukkit.getOnlinePlayers().size());
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return names;
        }

        List<String> names = new ArrayList<>();
        String lower = prefix.toLowerCase();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String name = player.getName();
            if (name.length() >= lower.length() && name.regionMatches(true, 0, lower, 0, lower.length())) {
                names.add(name);
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
        if (prefix.isEmpty()) return new ArrayList<>(options);

        List<String> result = new ArrayList<>();
        String lower = prefix.toLowerCase();
        for (String option : options) {
            if (option.length() >= lower.length() && option.regionMatches(true, 0, lower, 0, lower.length())) {
                result.add(option);
            }
        }
        return result;
    }
}
