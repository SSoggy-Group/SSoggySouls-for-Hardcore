package org.ssoggy.ssoggysouls.util;

import java.util.Locale;

public class CommandParserUtil {

    private CommandParserUtil() {}

    public static String isolateCommand(String rawMessage) {
        String trimmed = rawMessage.trim();
        int endIdx = -1;
        for (int i = 0; i < trimmed.length(); i++) {
            if (Character.isWhitespace(trimmed.charAt(i))) {
                endIdx = i;
                break;
            }
        }
        return endIdx == -1 ? trimmed.toLowerCase(Locale.ROOT) : trimmed.substring(0, endIdx).toLowerCase(Locale.ROOT);
    }
}
