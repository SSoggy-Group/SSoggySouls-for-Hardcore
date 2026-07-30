package org.ssoggy.ssoggysouls.util;

import java.util.Locale;
import java.util.Set;

public class CommandParsingUtil {
    private static final Set<String> WHITELISTED_COMMANDS = Set.of(
            "/msg", "/tell", "/r", "/reply", "/help", "/list",
            "/pstatus", "/psadmin", "/psa", "/revive", "/psetlives"
    );

    public static boolean isWhitelistedCommand(String fullCommand) {
        String clean = fullCommand.trim().toLowerCase(Locale.ROOT);
        int spaceIdx = -1;
        for (int i = 0; i < clean.length(); i++) {
            if (Character.isWhitespace(clean.charAt(i))) {
                spaceIdx = i;
                break;
            }
        }
        String command = spaceIdx == -1 ? clean : clean.substring(0, spaceIdx);
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }
}
