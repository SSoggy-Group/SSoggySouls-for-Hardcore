package org.ssoggy.ssoggysouls.util;

public final class CommandParserUtil {

    private CommandParserUtil() {}

    public static String getFirstWord(String message) {
        String clean = message.trim();
        int spaceIdx = -1;
        for (int i = 0; i < clean.length(); i++) {
            if (Character.isWhitespace(clean.charAt(i))) {
                spaceIdx = i;
                break;
            }
        }
        return spaceIdx == -1 ? clean : clean.substring(0, spaceIdx);
    }
}
