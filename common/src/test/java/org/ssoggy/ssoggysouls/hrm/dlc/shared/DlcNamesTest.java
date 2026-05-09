package org.ssoggy.ssoggysouls.hrm.dlc.shared;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DlcNamesTest {

    private MockedStatic<DlcServices> dlcServicesMockedStatic;
    private DlcStorage mockStorage;
    private final UUID TEST_UUID = UUID.randomUUID();
    private static final String TABLE = "usernamecache";

    @BeforeEach
    void setUp() {
        mockStorage = mock(DlcStorage.class);
        dlcServicesMockedStatic = Mockito.mockStatic(DlcServices.class);
        dlcServicesMockedStatic.when(DlcServices::usernameStorage).thenReturn(mockStorage);
    }

    @AfterEach
    void tearDown() {
        dlcServicesMockedStatic.close();
    }

    // --- cache tests ---

    @Test
    void testCacheValid() {
        DlcNames.cache(TEST_UUID, "Notch");

        verify(mockStorage).setValue(TABLE, TEST_UUID.toString(), "Notch");
        verify(mockStorage).save();
    }

    @Test
    void testCacheNullUuid() {
        DlcNames.cache(null, "Notch");

        verify(mockStorage, never()).setValue(anyString(), anyString(), anyString());
        verify(mockStorage, never()).save();
    }

    @Test
    void testCacheNullUsername() {
        DlcNames.cache(TEST_UUID, null);

        verify(mockStorage, never()).setValue(anyString(), anyString(), anyString());
        verify(mockStorage, never()).save();
    }

    @Test
    void testCacheBlankUsername() {
        DlcNames.cache(TEST_UUID, "   ");

        verify(mockStorage, never()).setValue(anyString(), anyString(), anyString());
        verify(mockStorage, never()).save();
    }

    // --- get tests ---

    @Test
    void testGetValid() {
        when(mockStorage.getValue(TABLE, TEST_UUID.toString())).thenReturn("Notch");

        String result = DlcNames.get(TEST_UUID);

        assertEquals("Notch", result);
        verify(mockStorage).getValue(TABLE, TEST_UUID.toString());
    }

    @Test
    void testGetNullUuid() {
        String result = DlcNames.get(null);

        assertNull(result);
        verify(mockStorage, never()).getValue(anyString(), anyString());
    }

    // --- getOrDefault tests ---

    @Test
    void testGetOrDefaultFound() {
        when(mockStorage.getValue(TABLE, TEST_UUID.toString())).thenReturn("Notch");

        String result = DlcNames.getOrDefault(TEST_UUID, "Fallback");

        assertEquals("Notch", result);
    }

    @Test
    void testGetOrDefaultNotFound() {
        when(mockStorage.getValue(TABLE, TEST_UUID.toString())).thenReturn(null);

        String result = DlcNames.getOrDefault(TEST_UUID, "Fallback");

        assertEquals("Fallback", result);
    }

    @Test
    void testGetOrDefaultBlank() {
        when(mockStorage.getValue(TABLE, TEST_UUID.toString())).thenReturn("   ");

        String result = DlcNames.getOrDefault(TEST_UUID, "Fallback");

        assertEquals("Fallback", result);
    }

    // --- findUuidByName tests ---

    @Test
    void testFindUuidByNameNullOrBlank() {
        Optional<UUID> resultNull = DlcNames.findUuidByName(null);
        Optional<UUID> resultBlank = DlcNames.findUuidByName("   ");

        assertFalse(resultNull.isPresent());
        assertFalse(resultBlank.isPresent());
        verify(mockStorage, never()).getTable(anyString());
    }

    @Test
    void testFindUuidByNameFoundExact() {
        Map<String, String> table = new HashMap<>();
        table.put(TEST_UUID.toString(), "Notch");
        when(mockStorage.getTable(TABLE)).thenReturn(table);

        Optional<UUID> result = DlcNames.findUuidByName("Notch");

        assertTrue(result.isPresent());
        assertEquals(TEST_UUID, result.get());
    }

    @Test
    void testFindUuidByNameCaseInsensitiveAndTrimmed() {
        Map<String, String> table = new HashMap<>();
        table.put(TEST_UUID.toString(), "NoTcH");
        when(mockStorage.getTable(TABLE)).thenReturn(table);

        Optional<UUID> result = DlcNames.findUuidByName("  nOtCh  ");

        assertTrue(result.isPresent());
        assertEquals(TEST_UUID, result.get());
    }

    @Test
    void testFindUuidByNameInvalidUuidFormat() {
        Map<String, String> table = new HashMap<>();
        table.put("not-a-valid-uuid", "Notch");
        when(mockStorage.getTable(TABLE)).thenReturn(table);

        Optional<UUID> result = DlcNames.findUuidByName("Notch");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindUuidByNameWithNullValueInStorage() {
        Map<String, String> table = new HashMap<>();
        table.put(TEST_UUID.toString(), null);
        when(mockStorage.getTable(TABLE)).thenReturn(table);

        Optional<UUID> result = DlcNames.findUuidByName("Notch");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindUuidByNameNotFound() {
        Map<String, String> table = new HashMap<>();
        table.put(TEST_UUID.toString(), "Notch");
        when(mockStorage.getTable(TABLE)).thenReturn(table);

        Optional<UUID> result = DlcNames.findUuidByName("Jeb");

        assertFalse(result.isPresent());
    }
}
