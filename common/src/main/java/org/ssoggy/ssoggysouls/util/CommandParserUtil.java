package org.ssoggy.ssoggysouls.util;

public final class CommandParserUtil {
    private CommandParserUtil() {}

    public static String getBaseCommand(String fullCommand) {
        String trimmed = fullCommand.trim();
        int spaceIdx = -1;
        for (int i = 0; i < trimmed.length(); i++) {
            if (Character.isWhitespace(trimmed.charAt(i))) {
                spaceIdx = i;
                break;
            }
        }
        return (spaceIdx == -1 ? trimmed : trimmed.substring(0, spaceIdx)).toLowerCase(java.util.Locale.ROOT);
    }
}
