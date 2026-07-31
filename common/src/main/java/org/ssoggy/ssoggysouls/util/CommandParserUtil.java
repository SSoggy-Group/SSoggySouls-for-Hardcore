package org.ssoggy.ssoggysouls.util;

import java.util.Locale;

public class CommandParserUtil {
    private CommandParserUtil() {}

    public static String extractCommand(String rawMessage) {
        String trimmed = rawMessage.trim();
        int spaceIdx = -1;
        for (int i = 0; i < trimmed.length(); i++) {
            if (Character.isWhitespace(trimmed.charAt(i))) {
                spaceIdx = i;
                break;
            }
        }
        return (spaceIdx == -1 ? trimmed : trimmed.substring(0, spaceIdx)).toLowerCase(Locale.ROOT);
    }
}
