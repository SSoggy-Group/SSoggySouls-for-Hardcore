package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DlcStorageTest {

    @TempDir
    File tempFolder;

    private Logger mockLogger;
    private DlcStorage dlcStorage;
    private File storageFile;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        storageFile = new File(tempFolder, "test_dlc_data.properties");
        dlcStorage = new DlcStorage(tempFolder, "test_dlc_data.properties", mockLogger);
    }

    @AfterEach
    void tearDown() {
        if (storageFile.exists()) {
            storageFile.delete();
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testLoadIOException() {
        // Create a directory with the same name as the target file
        // This will cause a FileNotFoundException (which is an IOException) when FileInputStream tries to read it
        if (storageFile.exists()) {
            storageFile.delete();
        }
        storageFile.mkdir();

        // Attempt to load
        dlcStorage.load();

        // Verify that the error was logged correctly
        ArgumentCaptor<Supplier<String>> supplierCaptor = ArgumentCaptor.forClass((Class) Supplier.class);
        verify(mockLogger).log(eq(Level.SEVERE), any(IOException.class), supplierCaptor.capture());

        // Verify the message
        assertEquals("Could not load RevivalPlus storage " + storageFile.getPath(), supplierCaptor.getValue().get());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testSaveIOException() {
        // Create a directory with the same name as the target file
        // This will cause a FileNotFoundException (which is an IOException) when FileOutputStream tries to open it
        if (storageFile.exists()) {
            storageFile.delete();
        }
        storageFile.mkdir();

        // Attempt to save
        dlcStorage.save();

        // Verify that the error was logged correctly
        ArgumentCaptor<Supplier<String>> supplierCaptor = ArgumentCaptor.forClass((Class) Supplier.class);
        verify(mockLogger).log(eq(Level.SEVERE), any(IOException.class), supplierCaptor.capture());

        // Verify the message
        assertEquals("Could not save RevivalPlus storage " + storageFile.getPath(), supplierCaptor.getValue().get());
    }

    @Test
    void testFolderCreationFailure() {
        File readOnlyFolder = new File(tempFolder, "readonly");
        readOnlyFolder.mkdir();
        readOnlyFolder.setReadOnly();

        File nestedFolder = new File(readOnlyFolder, "nested");

        DlcStorage failingStorage = new DlcStorage(nestedFolder, "test.properties", mockLogger);
        assertNotNull(failingStorage);

        verify(mockLogger).warning("Could not create RevivalPlus data folder: " + nestedFolder.getPath());

        // Reset permissions so tempDir cleanup doesn't fail
        readOnlyFolder.setWritable(true);
    }
}
