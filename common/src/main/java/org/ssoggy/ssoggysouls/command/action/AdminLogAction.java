package org.ssoggy.ssoggysouls.command.action;

import org.ssoggy.ssoggysouls.util.LogReaderUtil;

import java.io.File;
import java.io.IOException;
import java.util.Deque;

public class AdminLogAction {

    public enum ResultType {
        FILE_NOT_FOUND,
        READ_ERROR,
        SUCCESS
    }

    public static class AdminLogResult {
        public final ResultType type;
        public final Deque<String> lines;

        public AdminLogResult(ResultType type, Deque<String> lines) {
            this.type = type;
            this.lines = lines;
        }
    }

    private AdminLogAction() {
        // Utility class
    }

    public static AdminLogResult execute(File logFile, int linesToRead) {
        if (!logFile.exists()) {
            return new AdminLogResult(ResultType.FILE_NOT_FOUND, null);
        }

        try {
            Deque<String> lines = LogReaderUtil.readLastLines(logFile, linesToRead);
            return new AdminLogResult(ResultType.SUCCESS, lines);
        } catch (IOException | java.io.UncheckedIOException e) {
            return new AdminLogResult(ResultType.READ_ERROR, null);
        }
    }

    public static String formatLogLine(String line) {
        if (!line.contains("ADMIN ACTION - ")) {
            return "&7" + line;
        }
        String[] parts = line.split("ADMIN ACTION - ", 2);
        String timestamp = parts[0].replace("[", "&8[").replace("]", "&8]");
        String[] detailParts = parts[1].split(":", 2);
        if (detailParts.length == 2) {
            return timestamp + " &c" + detailParts[0].trim() + " &7- &e" + detailParts[1].trim();
        }
        return "&7" + line;
    }
}
