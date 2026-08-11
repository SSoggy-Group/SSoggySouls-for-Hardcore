package org.ssoggy.ssoggysouls.util;

import java.util.Locale;

public class CommandParserUtil {

    private CommandParserUtil() {}

    /**
     * Optimally extracts the base command string from a full chat message
     * by scanning for the first whitespace instead of compiling regex and allocating arrays.
     */
    public static String extractBaseCommand(String fullCommand) {
        if (fullCommand == null) return "";
        String clean = fullCommand.trim();
        int spaceIdx = -1;
        for (int i = 0; i < clean.length(); i++) {
            if (Character.isWhitespace(clean.charAt(i))) {
                spaceIdx = i;
                break;
            }
        }
        return (spaceIdx == -1 ? clean : clean.substring(0, spaceIdx)).toLowerCase(Locale.ROOT);
    }
}
