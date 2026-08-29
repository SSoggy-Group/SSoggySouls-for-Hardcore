package org.ssoggy.ssoggysouls.util;

import java.util.Set;

public final class LimboUtil {
    private LimboUtil() {}

    public static final Set<String> WHITELISTED_COMMANDS = Set.of(
            "/msg", "/tell", "/r", "/reply", "/help", "/list",
            "/pstatus", "/psadmin", "/psa", "/revive", "/psetlives"
    );

    public static boolean isWhitelistedCommand(String fullCommand) {
        String clean = fullCommand.trim();
        int spaceIdx = -1;
        for (int i = 0; i < clean.length(); i++) {
            if (Character.isWhitespace(clean.charAt(i))) {
                spaceIdx = i;
                break;
            }
        }
        String command = (spaceIdx == -1 ? clean : clean.substring(0, spaceIdx)).toLowerCase(java.util.Locale.ROOT);
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }
}
