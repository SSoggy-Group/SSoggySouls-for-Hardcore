package org.ssoggy.ssoggysouls.util;

public class CommandParser {

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
}
