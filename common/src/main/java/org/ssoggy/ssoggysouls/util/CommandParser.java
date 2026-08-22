package org.ssoggy.ssoggysouls.util;

import java.util.Locale;
import java.util.Set;

public final class CommandParser {

    private CommandParser() {}


    private static final Set<String> WHITELISTED_COMMANDS = Set.of(
            "/msg", "/tell", "/r", "/reply", "/help", "/list",
            "/pstatus", "/psadmin", "/psa", "/revive", "/psetlives"
    );

    public static boolean isWhitelistedCommand(String message) {
        String clean = message.trim();
        int spaceIdx = clean.indexOf(' ');
        String command = (spaceIdx == -1 ? clean : clean.substring(0, spaceIdx)).toLowerCase(Locale.ROOT);
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }
}
