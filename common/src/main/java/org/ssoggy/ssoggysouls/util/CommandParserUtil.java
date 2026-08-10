package org.ssoggy.ssoggysouls.util;

import java.util.Set;
import java.util.Locale;

public class CommandParserUtil {

    private static final Set<String> WHITELISTED_COMMANDS = Set.of(
            "/msg", "/tell", "/r", "/reply", "/help", "/list",
            "/pstatus", "/psadmin", "/psa", "/revive", "/psetlives"
    );

    public static boolean isLimboWhitelistedCommand(String fullCommand) {
        String trimmed = fullCommand.trim();
        int spaceIdx = -1;
        for (int i = 0; i < trimmed.length(); i++) {
            if (Character.isWhitespace(trimmed.charAt(i))) {
                spaceIdx = i;
                break;
            }
        }
        String command = (spaceIdx == -1 ? trimmed : trimmed.substring(0, spaceIdx)).toLowerCase(Locale.ROOT);
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }
}
