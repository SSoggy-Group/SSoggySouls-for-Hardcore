package org.ssoggy.ssoggysouls.command.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AdminLogActionTest {

    @TempDir
    File tempDir;

    @Test
    void testExecute_FileNotFound() {
        File file = new File(tempDir, "doesnotexist.log");
        AdminLogAction.AdminLogResult result = AdminLogAction.execute(file, 15);

        assertEquals(AdminLogAction.ResultType.FILE_NOT_FOUND, result.type);
        assertNull(result.lines);
    }

    @Test
    void testExecute_Success() throws IOException {
        File file = new File(tempDir, "success.log");
        Files.write(file.toPath(), List.of("line 1", "line 2"));

        AdminLogAction.AdminLogResult result = AdminLogAction.execute(file, 15);

        assertEquals(AdminLogAction.ResultType.SUCCESS, result.type);
        assertNotNull(result.lines);
        assertEquals(2, result.lines.size());
    }

    @Test
    void testExecute_ReadError() throws IOException {
        File file = new File(tempDir, "error.log");
        file.createNewFile();
        // Making it an invalid path or directory to force IOException in Files.lines
        File dir = new File(tempDir, "dir");
        dir.mkdir();

        AdminLogAction.AdminLogResult result = AdminLogAction.execute(dir, 15);
        assertEquals(AdminLogAction.ResultType.READ_ERROR, result.type);
        assertNull(result.lines);
    }

    @Test
    void testFormatLogLine() {
        // Standard matching line
        String result = AdminLogAction.formatLogLine("[12:34:56] ADMIN ACTION - TEST_ACTION : Test detail");
        assertEquals("&8[12:34:56&8]  &cTEST_ACTION &7- &eTest detail", result);

        // Missing details (no colon)
        String noColon = AdminLogAction.formatLogLine("[12:34:56] ADMIN ACTION - TEST_ACTION");
        assertEquals("&7[12:34:56] ADMIN ACTION - TEST_ACTION", noColon);

        // Not an admin action
        String notAction = AdminLogAction.formatLogLine("[12:34:56] SYSTEM - Startup");
        assertEquals("&7[12:34:56] SYSTEM - Startup", notAction);
    }
}
