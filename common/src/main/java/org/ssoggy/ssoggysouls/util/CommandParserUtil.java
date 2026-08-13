package org.ssoggy.ssoggysouls.util;

import java.util.Set;
import java.util.Locale;

public class CommandParserUtil {
    private static final Set<String> WHITELISTED_COMMANDS = Set.of(
            "/msg", "/tell", "/r", "/reply", "/help", "/list",
            "/pstatus", "/psadmin", "/psa", "/revive", "/psetlives"
    );

    private CommandParserUtil() {}

    public static boolean isWhitelistedCommand(String fullCommand) {
        String clean = fullCommand.trim();
        int spaceIdx = -1;
        for (int i = 0; i < clean.length(); i++) {
            if (Character.isWhitespace(clean.charAt(i))) {
                spaceIdx = i;
                break;
            }
        }
        String command = spaceIdx == -1 ? clean.toLowerCase(Locale.ROOT) : clean.substring(0, spaceIdx).toLowerCase(Locale.ROOT);
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }
}
