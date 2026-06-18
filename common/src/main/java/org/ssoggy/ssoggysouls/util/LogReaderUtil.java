package org.ssoggy.ssoggysouls.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

public class LogReaderUtil {
    private LogReaderUtil() {
    }

    /**
     * Reads only the last N lines from a file using a streaming approach.
     * This avoids loading the entire file into memory, preventing OOM issues.
     */
    public static Deque<String> readLastLines(File file, int maxLines) throws IOException {
        if (maxLines <= 0) return new ArrayDeque<>();
        Deque<String> lastLines = new ArrayDeque<>(maxLines);
        try (Stream<String> lines = Files.lines(file.toPath())) {
            lines.forEach(line -> {
                if (lastLines.size() >= maxLines) {
                    lastLines.pollFirst();
                }
                lastLines.addLast(line);
            });
        }
        return lastLines;
    }
}
