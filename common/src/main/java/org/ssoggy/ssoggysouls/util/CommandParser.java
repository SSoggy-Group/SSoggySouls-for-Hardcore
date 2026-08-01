package org.ssoggy.ssoggysouls.util;

import java.util.Locale;
import java.util.Set;

public class CommandParser {

    private static final Set<String> WHITELISTED_COMMANDS = Set.of(
            "/msg", "/tell", "/r", "/reply", "/help", "/list",
            "/pstatus", "/psadmin", "/psa", "/revive", "/psetlives"
    );

    public static boolean isWhitelistedCommand(String fullCommand) {
        String clean = fullCommand.trim();
        int endIdx = 0;
        while (endIdx < clean.length() && !Character.isWhitespace(clean.charAt(endIdx))) {
            endIdx++;
        }
        String command = endIdx > 0 ? clean.substring(0, endIdx).toLowerCase(Locale.ROOT) : "";
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }
}
