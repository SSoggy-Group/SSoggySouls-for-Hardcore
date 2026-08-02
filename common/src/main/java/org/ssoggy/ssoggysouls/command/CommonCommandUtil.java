package org.ssoggy.ssoggysouls.command;

import java.util.Arrays;
import java.util.List;

public class CommonCommandUtil {
    private static final List<String> WHITELISTED_COMMANDS = Arrays.asList(
            "/msg", "/tell", "/r", "/reply", "/help", "/list",
            "/pstatus", "/psadmin", "/psa", "/revive", "/psetlives"
    );

    private CommonCommandUtil() {}

    public static boolean isWhitelistedCommand(String fullCommand) {
        String trimmed = fullCommand.trim();
        int spaceIdx = trimmed.indexOf(' ');
        String command = (spaceIdx == -1 ? trimmed : trimmed.substring(0, spaceIdx)).toLowerCase(java.util.Locale.ROOT);
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }
}
