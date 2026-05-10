package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DlcStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void testLoadLogsSevereWhenStoragePathIsDirectory() throws IOException {
        Logger logger = mock(Logger.class);
        Path folder = tempDir.resolve("data");
        String fileName = "revivalplus.properties";
        Path storagePath = folder.resolve(fileName);
        Files.createDirectories(folder);
        Files.createDirectory(storagePath);

        DlcStorage storage = new DlcStorage(folder.toFile(), fileName, logger);

        assertTrue(storage.getTable("any").isEmpty());
        verify(logger).log(
                eq(Level.SEVERE),
                isA(IOException.class),
                argThat((Supplier<String> messageSupplier) -> {
                    String message = messageSupplier.get();
                    return message.contains("Could not load RevivalPlus storage")
                            && message.contains(storagePath.toString());
                })
        );
        verifyNoMoreInteractions(logger);
    }

    @Test
    void testSaveLogsSevereWhenStoragePathIsDirectory() throws IOException {
        Logger logger = mock(Logger.class);
        Path folder = tempDir.resolve("data");
        String fileName = "revivalplus.properties";
        Path storagePath = folder.resolve(fileName);
        Files.createDirectories(folder);
        Files.writeString(storagePath, "social.uuid=true\n");

        DlcStorage storage = new DlcStorage(folder.toFile(), fileName, logger);
        Files.delete(storagePath);
        Files.createDirectory(storagePath);

        storage.save();

        verify(logger).log(
                eq(Level.SEVERE),
                isA(IOException.class),
                argThat((Supplier<String> messageSupplier) -> {
                    String message = messageSupplier.get();
                    return message.contains("Could not save RevivalPlus storage")
                            && message.contains(storagePath.toString());
                })
        );
        verifyNoMoreInteractions(logger);
    }

    @Test
    void testLoadClearsExistingValuesWhenReloadFails() throws IOException {
        Logger logger = mock(Logger.class);
        Path folder = tempDir.resolve("data");
        String fileName = "revivalplus.properties";
        Path storagePath = folder.resolve(fileName);
        Files.createDirectories(folder);
        Files.writeString(storagePath, "stats.deaths=5\n");

        DlcStorage storage = new DlcStorage(folder.toFile(), fileName, logger);
        storage.setValue("stats", "kills", 2);
        Files.delete(storagePath);
        Files.createDirectory(storagePath);

        storage.load();

        Map<String, String> table = storage.getTable("stats");
        assertTrue(table.isEmpty(), "properties should be cleared before loading");
        verify(logger).log(
                eq(Level.SEVERE),
                isA(IOException.class),
                argThat((Supplier<String> messageSupplier) -> messageSupplier.get().contains("Could not load RevivalPlus storage"))
        );
    }
}
