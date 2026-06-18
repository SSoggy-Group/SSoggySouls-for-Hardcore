package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogReaderUtilTest {

    @TempDir
    File tempDir;

    @Test
    void testReadLastLines_EmptyFile() throws IOException {
        File file = new File(tempDir, "empty.log");
        file.createNewFile();

        Deque<String> lines = LogReaderUtil.readLastLines(file, 5);
        assertTrue(lines.isEmpty());
    }

    @Test
    void testReadLastLines_FewerLinesThanMax() throws IOException {
        File file = new File(tempDir, "fewer.log");
        Files.write(file.toPath(), List.of("line 1", "line 2", "line 3"));

        Deque<String> lines = LogReaderUtil.readLastLines(file, 5);
        assertEquals(3, lines.size());
        assertEquals("line 1", lines.pollFirst());
        assertEquals("line 2", lines.pollFirst());
        assertEquals("line 3", lines.pollFirst());
    }

    @Test
    void testReadLastLines_MoreLinesThanMax() throws IOException {
        File file = new File(tempDir, "more.log");
        Files.write(file.toPath(), List.of("line 1", "line 2", "line 3", "line 4", "line 5"));

        Deque<String> lines = LogReaderUtil.readLastLines(file, 3);
        assertEquals(3, lines.size());
        assertEquals("line 3", lines.pollFirst());
        assertEquals("line 4", lines.pollFirst());
        assertEquals("line 5", lines.pollFirst());
    }

    @Test
    void testReadLastLines_ZeroMaxLines() throws IOException {
        File file = new File(tempDir, "zero.log");
        Files.write(file.toPath(), List.of("line 1", "line 2"));

        Deque<String> lines = LogReaderUtil.readLastLines(file, 0);
        assertTrue(lines.isEmpty());
    }

    @Test
    void testReadLastLines_NegativeMaxLines() throws IOException {
        File file = new File(tempDir, "negative.log");
        Files.write(file.toPath(), List.of("line 1", "line 2"));

        Deque<String> lines = LogReaderUtil.readLastLines(file, -1);
        assertTrue(lines.isEmpty());
    }
}
