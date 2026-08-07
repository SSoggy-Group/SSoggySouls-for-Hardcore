package org.ssoggy.ssoggysouls.util;

import java.util.Locale;

public class CommonCommandUtil {

    private CommonCommandUtil() {}

    /**
     * Extracts the command string (the first word) from a raw chat message,
     * converting it to lowercase safely without allocating string arrays via split().
     * Correctly handles tabs and arbitrary whitespace.
     */
    public static String extractCommand(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            return "";
        }

        rawMessage = rawMessage.trim();
        int spaceIdx = -1;
        for (int i = 0; i < rawMessage.length(); i++) {
            if (Character.isWhitespace(rawMessage.charAt(i))) {
                spaceIdx = i;
                break;
            }
        }

        String command = (spaceIdx == -1 ? rawMessage : rawMessage.substring(0, spaceIdx));
        return command.toLowerCase(Locale.ROOT);
    }
}
