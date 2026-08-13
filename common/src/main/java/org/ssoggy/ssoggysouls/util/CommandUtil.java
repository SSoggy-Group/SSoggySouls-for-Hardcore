package org.ssoggy.ssoggysouls.util;

import java.util.Collection;

public class CommandUtil {

    private CommandUtil() {}

    /**
     * Efficiently extracts the first word from a command string.
     * Replaces regex splitting to avoid string array allocations.
     */
    public static boolean isWhitelistedCommand(String fullCommand, Collection<String> whitelistedCommands) {
        if (fullCommand == null || fullCommand.isBlank()) return false;
        String cleanMessage = fullCommand.trim();
        int spaceIdx = -1;
        for (int i = 0; i < cleanMessage.length(); i++) {
            if (Character.isWhitespace(cleanMessage.charAt(i))) {
                spaceIdx = i;
                break;
            }
        }
        String command = (spaceIdx == -1 ? cleanMessage : cleanMessage.substring(0, spaceIdx)).toLowerCase(java.util.Locale.ROOT);
        return whitelistedCommands.contains(command) || whitelistedCommands.contains("/" + command);
    }
}
