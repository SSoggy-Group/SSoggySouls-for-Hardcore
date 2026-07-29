package org.ssoggy.ssoggysouls.util;

public class CommandParser {

    private static final java.util.Set<String> WHITELISTED_COMMANDS = java.util.Set.of(
            "/msg", "/tell", "/r", "/reply", "/help", "/list",
            "/pstatus", "/psadmin", "/psa", "/revive", "/psetlives"
    );

    private CommandParser() {}

    public static String extractBaseCommand(String fullCommand) {
        String clean = fullCommand.trim();
        int spaceIdx = -1;
        for (int i = 0; i < clean.length(); i++) {
            if (Character.isWhitespace(clean.charAt(i))) {
                spaceIdx = i;
                break;
            }
        }
        return (spaceIdx == -1 ? clean : clean.substring(0, spaceIdx)).toLowerCase(java.util.Locale.ROOT);
    }

    public static boolean isWhitelistedCommand(String command) {
        return WHITELISTED_COMMANDS.contains(command) || WHITELISTED_COMMANDS.contains("/" + command);
    }
}
