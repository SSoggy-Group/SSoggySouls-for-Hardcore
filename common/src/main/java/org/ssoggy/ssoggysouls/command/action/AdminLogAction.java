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
}
