package org.ssoggy.ssoggysouls.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class TabCompleteUtilTest {

    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    private Player createMockPlayer(String name) {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn(name);
        return player;
    }

    @Test
    void testGetOnlinePlayerNames_EmptyPrefix() {
        // Setup
        List<Player> players = List.of(
                createMockPlayer("Alice"),
                createMockPlayer("Bob")
        );
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(players);

        // Execute
        List<String> result = TabCompleteUtil.getOnlinePlayerNames("");

        // Verify
        assertEquals(List.of("Alice", "Bob"), result);
    }

    @Test
    void testGetOnlinePlayerNames_WithPrefix() {
        // Setup
        Collection<? extends Player> players = Arrays.asList(
                createMockPlayer("Alice"),
                createMockPlayer("Alex"),
                createMockPlayer("Bob")
        );
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn((Collection) players);

        // Execute
        List<String> result = TabCompleteUtil.getOnlinePlayerNames("al");

        // Verify
        assertEquals(2, result.size());
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("Alex"));
    }

    @Test
    void testGetOnlinePlayerNames_CaseInsensitive() {
        // Setup
        Collection<? extends Player> players = Arrays.asList(
                createMockPlayer("Charlie"),
                createMockPlayer("david")
        );
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn((Collection) players);

        // Execute
        List<String> result1 = TabCompleteUtil.getOnlinePlayerNames("CH");
        List<String> result2 = TabCompleteUtil.getOnlinePlayerNames("Da");

        // Verify
        assertEquals(1, result1.size());
        assertEquals("Charlie", result1.get(0));

        assertEquals(1, result2.size());
        assertEquals("david", result2.get(0));
    }

    @Test
    void testGetOnlinePlayerNames_NoMatches() {
        // Setup
        Collection<? extends Player> players = Arrays.asList(
                createMockPlayer("Eve"),
                createMockPlayer("Frank")
        );
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn((Collection) players);

        // Execute
        List<String> result = TabCompleteUtil.getOnlinePlayerNames("z");

        // Verify
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOnlinePlayerNames_NoPlayersOnline() {
        // Setup
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(List.of());

        // Execute
        List<String> result = TabCompleteUtil.getOnlinePlayerNames("a");

        // Verify
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_EmptyPrefix() {
        List<String> options = List.of("apple", "banana", "cherry");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "");
        assertEquals(List.of("apple", "banana", "cherry"), result);
    }

    @Test
    void testFilterStartsWith_WithPrefix() {
        List<String> options = List.of("apple", "apricot", "banana");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "ap");
        assertEquals(List.of("apple", "apricot"), result);
    }

    @Test
    void testFilterStartsWith_CaseInsensitive() {
        List<String> options = List.of("Apple", "apricot", "BANANA");
        List<String> result1 = TabCompleteUtil.filterStartsWith(options, "AP");
        List<String> result2 = TabCompleteUtil.filterStartsWith(options, "ba");

        assertEquals(List.of("Apple", "apricot"), result1);
        assertEquals(List.of("BANANA"), result2);
    }

    @Test
    void testFilterStartsWith_NoMatches() {
        List<String> options = List.of("apple", "banana");
        List<String> result = TabCompleteUtil.filterStartsWith(options, "z");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_EmptyOptions() {
        List<String> options = List.of();
        List<String> result = TabCompleteUtil.filterStartsWith(options, "a");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterStartsWith_NullPrefix() {
        List<String> options = List.of("apple", "banana");
        assertThrows(NullPointerException.class, () -> TabCompleteUtil.filterStartsWith(options, null));
    }

    @Test
    void testFilterStartsWith_NullOptionsList() {
        assertThrows(NullPointerException.class, () -> TabCompleteUtil.filterStartsWith(null, "ap"));
    }

    @Test
    void testFilterStartsWith_OptionsContainsNull() {
        List<String> options = java.util.Arrays.asList("apple", null, "apricot");
        assertThrows(NullPointerException.class, () -> TabCompleteUtil.filterStartsWith(options, "ap"));
    }
}
